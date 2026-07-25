package com.cptgummiball.shulkaero;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ShulkerBoxBlock;

/**
 * Tracks shulker boxes placed by the local player and keeps their Xaero
 * waypoints in sync:
 * <ul>
 *   <li>on placement: creates a waypoint named and colored like the box</li>
 *   <li>on removal of the box (by any means - mining, explosion, piston...):
 *       removes the waypoint again, unless the player already deleted or
 *       renamed it manually</li>
 * </ul>
 * Tracked positions are persisted to config/shulkaero/tracked_waypoints.json,
 * keyed by Xaero's per-world path, so breaking a box after a relog still cleans
 * up its waypoint.
 */
public final class ShulkerWaypointManager {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type STORE_TYPE = new TypeToken<Map<String, List<TrackedEntry>>>() {
    }.getType();
    private static final int CHECK_INTERVAL_TICKS = 10;

    /** worldKey (Xaero world path) -> tracked shulker box positions */
    private static Map<String, List<TrackedEntry>> tracked;
    private static int tickCounter;

    public static final class TrackedEntry {
        int x;
        int y;
        int z;
        String name;

        TrackedEntry(int x, int y, int z, String name) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.name = name;
        }
    }

    private ShulkerWaypointManager() {
    }

    /**
     * Called from the placement hook after the client placed a shulker box.
     *
     * @param stack a copy of the item stack taken before placement (still
     *              carrying the custom name component)
     */
    public static void onShulkerPlaced(Level level, BlockPos pos, ItemStack stack, ShulkerBoxBlock block) {
        if (!Shulkaero.isXaeroAvailable()) {
            return;
        }
        String name = stack.getHoverName().getString().trim();
        if (name.isEmpty()) {
            name = "Shulker Box";
        }
        ShulkerColors.ColorRef color = ShulkerColors.forDye(block.getColor());

        ensureLoaded();
        // If a stale entry exists at this position, clean it (and its waypoint) up first.
        String currentKey = XaeroIntegration.currentWorldKey();
        if (currentKey != null) {
            removeEntryAt(currentKey, pos.getX(), pos.getY(), pos.getZ(), true);
        }

        String worldKey = XaeroIntegration.addWaypoint(
                pos.getX(), pos.getY(), pos.getZ(), name, initialsOf(name), color);
        if (worldKey == null) {
            Shulkaero.LOGGER.debug("No Xaero minimap world available, skipping waypoint for '{}'", name);
            return;
        }
        tracked.computeIfAbsent(worldKey, k -> new ArrayList<>())
                .add(new TrackedEntry(pos.getX(), pos.getY(), pos.getZ(), name));
        saveStore();
    }

    /**
     * Called every client tick (from the Minecraft tick mixin). Periodically
     * checks all tracked positions in the current world: if a tracked block is
     * loaded and no longer a shulker box, the corresponding waypoint is removed.
     */
    public static void clientTick(Minecraft minecraft) {
        if (minecraft.level == null || minecraft.player == null || !Shulkaero.isXaeroAvailable()) {
            return;
        }
        if (++tickCounter < CHECK_INTERVAL_TICKS) {
            return;
        }
        tickCounter = 0;

        String worldKey = XaeroIntegration.currentWorldKey();
        if (worldKey == null) {
            return;
        }
        ensureLoaded();
        List<TrackedEntry> entries = tracked.get(worldKey);
        if (entries == null || entries.isEmpty()) {
            return;
        }
        Level level = minecraft.level;
        boolean changed = false;
        Iterator<TrackedEntry> iterator = entries.iterator();
        while (iterator.hasNext()) {
            TrackedEntry entry = iterator.next();
            BlockPos pos = new BlockPos(entry.x, entry.y, entry.z);
            if (!level.hasChunkAt(pos)) {
                continue; // can't verify unloaded positions
            }
            if (level.getBlockState(pos).getBlock() instanceof ShulkerBoxBlock) {
                continue; // box still there
            }
            // Box is gone: remove its waypoint (no-op if the player already deleted it).
            XaeroIntegration.removeWaypointAt(entry.x, entry.y, entry.z, entry.name);
            iterator.remove();
            changed = true;
        }
        if (changed) {
            saveStore();
        }
    }

    private static void removeEntryAt(String worldKey, int x, int y, int z, boolean removeWaypoint) {
        List<TrackedEntry> entries = tracked.get(worldKey);
        if (entries == null) {
            return;
        }
        Iterator<TrackedEntry> iterator = entries.iterator();
        while (iterator.hasNext()) {
            TrackedEntry entry = iterator.next();
            if (entry.x == x && entry.y == y && entry.z == z) {
                if (removeWaypoint) {
                    XaeroIntegration.removeWaypointAt(entry.x, entry.y, entry.z, entry.name);
                }
                iterator.remove();
            }
        }
    }

    static String initialsOf(String name) {
        StringBuilder initials = new StringBuilder();
        for (String word : name.split("\\s+")) {
            for (int i = 0; i < word.length(); i++) {
                char c = word.charAt(i);
                if (Character.isLetterOrDigit(c)) {
                    initials.append(Character.toUpperCase(c));
                    break;
                }
            }
            if (initials.length() >= 2) {
                break;
            }
        }
        return initials.isEmpty() ? "S" : initials.toString();
    }

    // ------------------------------------------------------------------ store

    private static Path storeFile() {
        return Minecraft.getInstance().gameDirectory.toPath()
                .resolve("config").resolve(Shulkaero.MOD_ID).resolve("tracked_waypoints.json");
    }

    private static void ensureLoaded() {
        if (tracked != null) {
            return;
        }
        tracked = new HashMap<>();
        Path file = storeFile();
        if (!Files.exists(file)) {
            return;
        }
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            Map<String, List<TrackedEntry>> loaded = GSON.fromJson(reader, STORE_TYPE);
            if (loaded != null) {
                tracked = loaded;
            }
        } catch (IOException | RuntimeException e) {
            Shulkaero.LOGGER.warn("Could not read {}, starting with an empty tracking store", file, e);
        }
    }

    private static void saveStore() {
        Path file = storeFile();
        try {
            Files.createDirectories(file.getParent());
            try (Writer writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
                GSON.toJson(tracked, STORE_TYPE, writer);
            }
        } catch (IOException e) {
            Shulkaero.LOGGER.warn("Could not save {}", file, e);
        }
    }
}
