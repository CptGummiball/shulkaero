package com.cptgummiball.shulkaero;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Tracks shulker boxes placed by the local player and keeps their Xaero
 * waypoints in sync. Entry lifecycle:
 * <ol>
 *   <li><b>pending</b> (in-memory): placement detected client-side; after a short
 *       confirmation delay the box must still exist (server may roll the placement
 *       back), only then the waypoint is created</li>
 *   <li><b>TRACKED</b> (persisted): box exists, waypoint exists</li>
 *   <li><b>DROPPED</b> (persisted): box was broken; the waypoint stays at the break
 *       position while a matching shulker box item lies nearby. Once the item is
 *       gone (picked up, despawned, burned - or never dropped, e.g. creative),
 *       the waypoint is removed - unless the player already deleted or renamed it</li>
 * </ol>
 * Tracked positions are persisted to config/shulkaero/tracked_waypoints.json,
 * keyed by Xaero's per-world path (see {@link TrackingStore}).
 */
public final class ShulkerWaypointManager {

    /** ticks between placement detection and waypoint creation (server confirmation window) */
    private static final int PLACE_CONFIRM_TICKS = 10;
    /** failed item searches (at check interval) before a DROPPED entry counts as collected */
    private static final int DROP_GRACE_CHECKS = 3;
    private static final long VISIT_STAMP_INTERVAL_MS = 60_000;

    private static TrackingStore.Store store;
    private static int tickCounter;
    private static long lastVisitStamp;
    private static final List<PendingPlacement> pending = new ArrayList<>();

    private static final class PendingPlacement {
        final BlockPos pos;
        final ItemStack stack;
        int ticksLeft = PLACE_CONFIRM_TICKS;

        PendingPlacement(BlockPos pos, ItemStack stack) {
            this.pos = pos;
            this.stack = stack;
        }
    }

    private ShulkerWaypointManager() {
    }

    // ------------------------------------------------------------- placement

    /**
     * Called from the placement hook after the client placed a shulker box.
     * The waypoint is not created yet - only after {@link #PLACE_CONFIRM_TICKS}
     * the placement counts as confirmed (protection plugins may roll it back).
     *
     * @param stack a copy of the item stack taken before placement (still
     *              carrying the custom name component)
     */
    public static void onShulkerPlaceDetected(BlockPos pos, ItemStack stack) {
        if (!Shulkaero.isXaeroAvailable()) {
            return;
        }
        ShulkaeroConfig config = ShulkaeroConfig.get();
        if (!config.enabled) {
            return;
        }
        if (config.onlyNamedBoxes && !isCustomNamed(stack)) {
            return;
        }
        pending.removeIf(p -> p.pos.equals(pos));
        pending.add(new PendingPlacement(pos.immutable(), stack));
    }

    // ------------------------------------------------------------------ tick

    /** Called every client tick (from the Minecraft tick mixin). */
    public static void clientTick(Minecraft minecraft) {
        if (minecraft.level == null || minecraft.player == null || !Shulkaero.isXaeroAvailable()) {
            pending.clear();
            return;
        }
        ShulkaeroConfig config = ShulkaeroConfig.get();
        if (!config.enabled) {
            pending.clear();
            return;
        }
        processPending(minecraft.level, config);
        if (++tickCounter < config.checkIntervalTicks) {
            return;
        }
        tickCounter = 0;
        checkTracked(minecraft.level, config);
    }

    private static void processPending(Level level, ShulkaeroConfig config) {
        if (pending.isEmpty()) {
            return;
        }
        Iterator<PendingPlacement> iterator = pending.iterator();
        while (iterator.hasNext()) {
            PendingPlacement placement = iterator.next();
            if (--placement.ticksLeft > 0) {
                continue;
            }
            iterator.remove();
            BlockState state = level.getBlockState(placement.pos);
            if (state.getBlock() instanceof ShulkerBoxBlock box) {
                createWaypoint(placement.pos, placement.stack, box, config);
            }
        }
    }

    private static void createWaypoint(BlockPos pos, ItemStack stack, ShulkerBoxBlock box, ShulkaeroConfig config) {
        String rawName = stack.getHoverName().getString().trim();
        if (rawName.isEmpty()) {
            rawName = "Shulker Box";
        }
        String strippedName = NameColorCodes.strip(rawName);
        String waypointName = config.namePrefix + strippedName;

        ShulkerColors.ColorRef color = ShulkerColors.forDye(box.getColor());
        if (config.colorFromNameCodes) {
            Integer chatIndex = NameColorCodes.chatIndexOf(rawName);
            if (chatIndex != null) {
                color = new ShulkerColors.ColorRef(null, chatIndex);
            }
        }

        ensureLoaded();
        // If a stale entry exists at this position, clean it (and its waypoint) up first.
        String currentKey = XaeroIntegration.currentWorldKey();
        if (currentKey != null) {
            removeEntriesAt(currentKey, pos, true);
        }

        String worldKey = XaeroIntegration.addWaypoint(
                pos.getX(), pos.getY(), pos.getZ(), waypointName, NameColorCodes.initialsOf(strippedName), color);
        if (worldKey == null) {
            Shulkaero.LOGGER.debug("No Xaero minimap world available, skipping waypoint for '{}'", waypointName);
            return;
        }

        TrackingStore.Entry entry = new TrackingStore.Entry();
        entry.x = pos.getX();
        entry.y = pos.getY();
        entry.z = pos.getZ();
        entry.anchorX = pos.getX();
        entry.anchorY = pos.getY();
        entry.anchorZ = pos.getZ();
        entry.boxName = rawName;
        entry.waypointName = waypointName;
        entry.customNamed = isCustomNamed(stack);
        entry.color = box.getColor() == null ? "default" : box.getColor().getName();
        entry.state = TrackingStore.STATE_TRACKED;
        bucketFor(worldKey).entries.add(entry);
        saveStore();
        Feedback.waypointAdded(waypointName);
    }

    private static void checkTracked(Level level, ShulkaeroConfig config) {
        String worldKey = XaeroIntegration.currentWorldKey();
        if (worldKey == null) {
            return;
        }
        ensureLoaded();
        TrackingStore.WorldBucket bucket = store.worlds.get(worldKey);
        if (bucket == null) {
            return;
        }
        long now = System.currentTimeMillis();
        boolean changed = false;
        if (now - lastVisitStamp > VISIT_STAMP_INTERVAL_MS) {
            lastVisitStamp = now;
            bucket.lastVisited = now;
            changed = true;
        }

        Iterator<TrackingStore.Entry> iterator = bucket.entries.iterator();
        while (iterator.hasNext()) {
            TrackingStore.Entry entry = iterator.next();
            if (entry.isDropped()) {
                changed |= checkDropped(level, config, entry, iterator);
            } else {
                changed |= checkPlaced(level, config, entry, iterator);
            }
        }
        if (changed) {
            saveStore();
        }
    }

    /** @return true if the store changed */
    private static boolean checkPlaced(Level level, ShulkaeroConfig config,
                                       TrackingStore.Entry entry, Iterator<TrackingStore.Entry> iterator) {
        BlockPos pos = new BlockPos(entry.x, entry.y, entry.z);
        if (!level.hasChunkAt(pos)) {
            return false;
        }
        if (level.getBlockState(pos).getBlock() instanceof ShulkerBoxBlock) {
            return false;
        }
        // Box is gone.
        if (config.waitForPickup) {
            entry.state = TrackingStore.STATE_DROPPED;
            entry.anchorX = entry.x;
            entry.anchorY = entry.y;
            entry.anchorZ = entry.z;
            entry.graceLeft = DROP_GRACE_CHECKS;
            Feedback.boxDropped(entry.waypointName);
            return true;
        }
        XaeroIntegration.removeWaypointAt(entry.x, entry.y, entry.z, entry.waypointName);
        iterator.remove();
        Feedback.waypointRemoved(entry.waypointName);
        return true;
    }

    /** @return true if the store changed */
    private static boolean checkDropped(Level level, ShulkaeroConfig config,
                                        TrackingStore.Entry entry, Iterator<TrackingStore.Entry> iterator) {
        BlockPos anchor = new BlockPos(entry.anchorX, entry.anchorY, entry.anchorZ);
        if (!level.hasChunkAt(anchor)) {
            return false;
        }
        int radius = config.pickupSearchRadius;
        AABB searchBox = new AABB(
                anchor.getX() - radius, anchor.getY() - radius, anchor.getZ() - radius,
                anchor.getX() + radius + 1, anchor.getY() + radius + 1, anchor.getZ() + radius + 1);
        List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, searchBox,
                item -> matchesDroppedBox(entry, item.getItem()));
        if (!items.isEmpty()) {
            entry.graceLeft = 0;
            BlockPos itemPos = items.get(0).blockPosition();
            if (!itemPos.equals(anchor)) {
                entry.anchorX = itemPos.getX();
                entry.anchorY = itemPos.getY();
                entry.anchorZ = itemPos.getZ();
                return true;
            }
            return false;
        }
        if (entry.graceLeft > 0) {
            entry.graceLeft--;
            return false;
        }
        // Item collected, despawned or never dropped: remove the waypoint
        // (no-op if the player already deleted or renamed it).
        XaeroIntegration.removeWaypointAt(entry.x, entry.y, entry.z, entry.waypointName);
        iterator.remove();
        Feedback.waypointRemoved(entry.waypointName);
        return true;
    }

    private static boolean matchesDroppedBox(TrackingStore.Entry entry, ItemStack stack) {
        if (!(stack.getItem() instanceof BlockItem blockItem)
                || !(blockItem.getBlock() instanceof ShulkerBoxBlock box)) {
            return false;
        }
        if (!TrackingStore.COLOR_ANY.equals(entry.color)) {
            String dye = box.getColor() == null ? "default" : box.getColor().getName();
            if (!entry.color.equals(dye)) {
                return false;
            }
        }
        boolean named = isCustomNamed(stack);
        if (entry.customNamed) {
            return named && stack.getHoverName().getString().trim().equals(entry.boxName);
        }
        return !named;
    }

    // -------------------------------------------------------------- commands

    /** Snapshot of the tracked entries for the world the player is currently in. */
    public static List<TrackingStore.Entry> entriesForCurrentWorld() {
        String worldKey = Shulkaero.isXaeroAvailable() ? XaeroIntegration.currentWorldKey() : null;
        if (worldKey == null) {
            return List.of();
        }
        ensureLoaded();
        TrackingStore.WorldBucket bucket = store.worlds.get(worldKey);
        return bucket == null ? List.of() : List.copyOf(bucket.entries);
    }

    /**
     * Forgets all tracked boxes of the current world (waypoints are kept).
     *
     * @return number of forgotten entries
     */
    public static int clearCurrentWorld() {
        String worldKey = Shulkaero.isXaeroAvailable() ? XaeroIntegration.currentWorldKey() : null;
        if (worldKey == null) {
            return 0;
        }
        ensureLoaded();
        TrackingStore.WorldBucket bucket = store.worlds.get(worldKey);
        if (bucket == null || bucket.entries.isEmpty()) {
            return 0;
        }
        int count = bucket.entries.size();
        bucket.entries.clear();
        saveStore();
        return count;
    }

    // --------------------------------------------------------------- helpers

    /** True if the stack's display name differs from the item's default name. */
    static boolean isCustomNamed(ItemStack stack) {
        String defaultName = new ItemStack(stack.getItem()).getHoverName().getString();
        return !stack.getHoverName().getString().equals(defaultName);
    }

    private static void removeEntriesAt(String worldKey, BlockPos pos, boolean removeWaypoint) {
        TrackingStore.WorldBucket bucket = store.worlds.get(worldKey);
        if (bucket == null) {
            return;
        }
        Iterator<TrackingStore.Entry> iterator = bucket.entries.iterator();
        while (iterator.hasNext()) {
            TrackingStore.Entry entry = iterator.next();
            if (entry.x == pos.getX() && entry.y == pos.getY() && entry.z == pos.getZ()) {
                if (removeWaypoint) {
                    XaeroIntegration.removeWaypointAt(entry.x, entry.y, entry.z, entry.waypointName);
                }
                iterator.remove();
            }
        }
    }

    // ------------------------------------------------------------------ store

    private static Path storeFile() {
        return Minecraft.getInstance().gameDirectory.toPath()
                .resolve("config").resolve(Shulkaero.MOD_ID).resolve("tracked_waypoints.json");
    }

    private static TrackingStore.WorldBucket bucketFor(String worldKey) {
        return store.worlds.computeIfAbsent(worldKey, k -> {
            TrackingStore.WorldBucket bucket = new TrackingStore.WorldBucket();
            bucket.lastVisited = System.currentTimeMillis();
            return bucket;
        });
    }

    private static void ensureLoaded() {
        if (store != null) {
            return;
        }
        store = new TrackingStore.Store();
        Path file = storeFile();
        if (!Files.exists(file)) {
            return;
        }
        try {
            String json = Files.readString(file, StandardCharsets.UTF_8);
            store = TrackingStore.fromJson(json, System.currentTimeMillis());
        } catch (IOException | RuntimeException e) {
            Shulkaero.LOGGER.warn("Could not read {}, starting with an empty tracking store", file, e);
        }
    }

    private static void saveStore() {
        Path file = storeFile();
        try {
            Files.createDirectories(file.getParent());
            Files.writeString(file, TrackingStore.toJson(store), StandardCharsets.UTF_8);
        } catch (IOException e) {
            Shulkaero.LOGGER.warn("Could not save {}", file, e);
        }
    }
}
