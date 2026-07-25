package com.cptgummiball.shulkaero;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Persistence model for tracked shulker boxes (format v2) including migration
 * from the v1 format and pruning of long-unvisited worlds.
 * Pure JSON logic, unit-testable without Minecraft.
 *
 * v2 layout:
 * {"formatVersion":2,"worlds":{"<xaeroPath>":{"lastVisited":123,"entries":[{...}]}}}
 *
 * v1 layout (Shulkaero 1.0.0):
 * {"<xaeroPath>":[{"x":1,"y":2,"z":3,"name":"..."}]}
 */
public final class TrackingStore {

    public static final int FORMAT_VERSION = 2;
    /** Worlds not visited for this long are dropped on load. */
    public static final long PRUNE_AGE_MS = 90L * 24 * 60 * 60 * 1000;

    public static final String STATE_TRACKED = "TRACKED";
    public static final String STATE_DROPPED = "DROPPED";
    /** color value that matches any box color (used for migrated v1 entries) */
    public static final String COLOR_ANY = "any";

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static final class Store {
        public int formatVersion = FORMAT_VERSION;
        public Map<String, WorldBucket> worlds = new HashMap<>();
    }

    public static final class WorldBucket {
        public long lastVisited;
        public List<Entry> entries = new ArrayList<>();
    }

    public static final class Entry {
        /** block position of the (former) shulker box = waypoint position */
        public int x;
        public int y;
        public int z;
        /** raw box name (hover name, may contain § codes) — used to match the dropped item */
        public String boxName;
        /** display name of the created waypoint (prefix + stripped box name) */
        public String waypointName;
        public boolean customNamed;
        /** dye color name ("purple", ...), "default" for the undyed box, or "any" */
        public String color = COLOR_ANY;
        public String state = STATE_TRACKED;
        /** last known search anchor for the dropped item (defaults to the box position) */
        public int anchorX;
        public int anchorY;
        public int anchorZ;
        /** remaining no-item checks before a DROPPED entry is considered collected (not persisted) */
        public transient int graceLeft;

        public boolean isDropped() {
            return STATE_DROPPED.equals(state);
        }
    }

    private TrackingStore() {
    }

    public static String toJson(Store store) {
        return GSON.toJson(store);
    }

    /**
     * Parses v2 (with pruning) or migrates v1 content. Never returns null;
     * unreadable content yields an empty store.
     */
    public static Store fromJson(String json, long now) {
        Store store = new Store();
        JsonElement root;
        try {
            root = JsonParser.parseString(json);
        } catch (RuntimeException e) {
            return store;
        }
        if (!root.isJsonObject()) {
            return store;
        }
        JsonObject obj = root.getAsJsonObject();
        try {
            if (obj.has("formatVersion")) {
                Store parsed = GSON.fromJson(obj, Store.class);
                if (parsed != null && parsed.worlds != null) {
                    store = parsed;
                    store.formatVersion = FORMAT_VERSION;
                    store.worlds.values().removeIf(bucket -> bucket == null || bucket.entries == null
                            || (bucket.lastVisited > 0 && now - bucket.lastVisited > PRUNE_AGE_MS));
                    for (WorldBucket bucket : store.worlds.values()) {
                        bucket.entries.removeIf(entry -> entry == null || entry.waypointName == null);
                        for (Entry entry : bucket.entries) {
                            sanitize(entry);
                        }
                    }
                }
            } else {
                migrateV1(obj, store, now);
            }
        } catch (RuntimeException e) {
            return new Store();
        }
        return store;
    }

    private static void migrateV1(JsonObject obj, Store store, long now) {
        for (String worldKey : obj.keySet()) {
            JsonElement listElement = obj.get(worldKey);
            if (!listElement.isJsonArray()) {
                continue;
            }
            WorldBucket bucket = new WorldBucket();
            bucket.lastVisited = now;
            for (JsonElement element : (JsonArray) listElement) {
                if (!element.isJsonObject()) {
                    continue;
                }
                JsonObject old = element.getAsJsonObject();
                if (!old.has("x") || !old.has("y") || !old.has("z") || !old.has("name")) {
                    continue;
                }
                Entry entry = new Entry();
                entry.x = old.get("x").getAsInt();
                entry.y = old.get("y").getAsInt();
                entry.z = old.get("z").getAsInt();
                entry.boxName = old.get("name").getAsString();
                entry.waypointName = entry.boxName;
                // v1 did not record whether the name was custom; assume custom unless
                // it looks like the vanilla default name.
                entry.customNamed = !"Shulker Box".equals(entry.boxName);
                entry.color = COLOR_ANY;
                entry.state = STATE_TRACKED;
                sanitize(entry);
                bucket.entries.add(entry);
            }
            if (!bucket.entries.isEmpty()) {
                store.worlds.put(worldKey, bucket);
            }
        }
    }

    private static void sanitize(Entry entry) {
        if (entry.boxName == null) {
            entry.boxName = entry.waypointName;
        }
        if (entry.color == null) {
            entry.color = COLOR_ANY;
        }
        if (!STATE_DROPPED.equals(entry.state)) {
            entry.state = STATE_TRACKED;
        }
        if (entry.anchorX == 0 && entry.anchorY == 0 && entry.anchorZ == 0) {
            entry.anchorX = entry.x;
            entry.anchorY = entry.y;
            entry.anchorZ = entry.z;
        }
    }
}
