package com.cptgummiball.shulkaero;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class TrackingStoreTest {

    private static final long NOW = 1_753_000_000_000L;

    @Test
    void migratesV1Format() {
        String v1 = """
                {
                  "Multiplayer_server.example.com/dim%overworld": [
                    {"x": 10, "y": 64, "z": -5, "name": "Diamanten"},
                    {"x": 1, "y": 2, "z": 3, "name": "Shulker Box"}
                  ]
                }
                """;
        TrackingStore.Store store = TrackingStore.fromJson(v1, NOW);
        assertEquals(TrackingStore.FORMAT_VERSION, store.formatVersion);
        TrackingStore.WorldBucket bucket = store.worlds.get("Multiplayer_server.example.com/dim%overworld");
        assertEquals(2, bucket.entries.size());
        assertEquals(NOW, bucket.lastVisited);

        TrackingStore.Entry named = bucket.entries.get(0);
        assertEquals("Diamanten", named.boxName);
        assertEquals("Diamanten", named.waypointName);
        assertTrue(named.customNamed);
        assertEquals(TrackingStore.COLOR_ANY, named.color);
        assertEquals(TrackingStore.STATE_TRACKED, named.state);
        assertEquals(10, named.anchorX);

        assertFalse(bucket.entries.get(1).customNamed);
    }

    @Test
    void prunesLongUnvisitedWorlds() {
        long old = NOW - TrackingStore.PRUNE_AGE_MS - 1;
        String v2 = String.format("""
                {
                  "formatVersion": 2,
                  "worlds": {
                    "old_world": {"lastVisited": %d, "entries": [{"x":1,"y":2,"z":3,"waypointName":"A","boxName":"A"}]},
                    "fresh_world": {"lastVisited": %d, "entries": [{"x":4,"y":5,"z":6,"waypointName":"B","boxName":"B"}]}
                  }
                }
                """, old, NOW);
        TrackingStore.Store store = TrackingStore.fromJson(v2, NOW);
        assertFalse(store.worlds.containsKey("old_world"));
        assertTrue(store.worlds.containsKey("fresh_world"));
    }

    @Test
    void roundTripKeepsData() {
        TrackingStore.Store store = new TrackingStore.Store();
        TrackingStore.WorldBucket bucket = new TrackingStore.WorldBucket();
        bucket.lastVisited = NOW;
        TrackingStore.Entry entry = new TrackingStore.Entry();
        entry.x = 7;
        entry.y = 70;
        entry.z = -7;
        entry.boxName = "Kisten";
        entry.waypointName = "[Box] Kisten";
        entry.customNamed = true;
        entry.color = "red";
        entry.state = TrackingStore.STATE_DROPPED;
        entry.anchorX = 8;
        entry.anchorY = 70;
        entry.anchorZ = -7;
        bucket.entries.add(entry);
        store.worlds.put("w", bucket);

        TrackingStore.Store reloaded = TrackingStore.fromJson(TrackingStore.toJson(store), NOW);
        TrackingStore.Entry restored = reloaded.worlds.get("w").entries.get(0);
        assertEquals("[Box] Kisten", restored.waypointName);
        assertEquals("red", restored.color);
        assertTrue(restored.isDropped());
        assertEquals(8, restored.anchorX);
        assertEquals(0, restored.graceLeft, "graceLeft is transient and must not be persisted");
    }

    @Test
    void garbageYieldsEmptyStore() {
        assertTrue(TrackingStore.fromJson("not json at all", NOW).worlds.isEmpty());
        assertTrue(TrackingStore.fromJson("[1,2,3]", NOW).worlds.isEmpty());
        assertTrue(TrackingStore.fromJson("{}", NOW).worlds.isEmpty());
    }
}
