package com.cptgummiball.shulkaero;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import xaero.common.minimap.waypoints.Waypoint;
import xaero.hud.minimap.BuiltInHudModules;
import xaero.hud.minimap.module.MinimapSession;
import xaero.hud.minimap.waypoint.WaypointColor;
import xaero.hud.minimap.waypoint.WaypointPurpose;
import xaero.hud.minimap.waypoint.set.WaypointSet;
import xaero.hud.minimap.world.MinimapWorld;

/**
 * The only class that touches Xaero's Minimap API. Must never be loaded unless
 * {@link Shulkaero#isXaeroAvailable()} returned true (all callers guard this),
 * so the mod also works (as a no-op) without Xaero installed.
 * <p>
 * Waypoints created here are added to the currently active waypoint set of the
 * current Xaero minimap world and saved immediately, so they survive relogs and
 * also show up on Xaero's World Map (which renders minimap waypoints).
 */
final class XaeroIntegration {

    private XaeroIntegration() {
    }

    /**
     * @return the stable key of the Xaero minimap world the player is currently
     *         in (used to bucket tracked shulker positions), or null if no
     *         minimap session/world is available yet.
     */
    static String currentWorldKey() {
        MinimapWorld world = currentWorld();
        return world == null ? null : world.getFullPath().toString();
    }

    private static MinimapWorld currentWorld() {
        MinimapSession session = BuiltInHudModules.MINIMAP.getCurrentSession();
        return session == null ? null : session.getWorldManager().getCurrentWorld();
    }

    /**
     * Adds a waypoint at the given block position to the current waypoint set.
     *
     * @return the world key the waypoint was added under, or null on failure
     */
    static String addWaypoint(int x, int y, int z, String name, String initials, ShulkerColors.ColorRef colorRef) {
        MinimapSession session = BuiltInHudModules.MINIMAP.getCurrentSession();
        if (session == null) {
            return null;
        }
        MinimapWorld world = session.getWorldManager().getCurrentWorld();
        if (world == null) {
            return null;
        }
        WaypointSet set = world.getCurrentWaypointSet();
        if (set == null) {
            set = world.getWaypointSet(MinimapWorld.DEFAULT_SET);
        }
        if (set == null) {
            return null;
        }
        Waypoint waypoint = new Waypoint(x, y, z, name, initials, resolveColor(colorRef), WaypointPurpose.NORMAL, false);
        set.add(waypoint);
        save(session, world);
        Shulkaero.LOGGER.info("Added waypoint '{}' at {},{},{} in {}", name, x, y, z, world.getFullPath());
        return world.getFullPath().toString();
    }

    /**
     * Removes the waypoint with the exact position and name from any waypoint set
     * of the current minimap world. If the player already deleted (or renamed)
     * the waypoint, nothing happens.
     *
     * @return true if a waypoint was found and removed
     */
    static boolean removeWaypointAt(int x, int y, int z, String name) {
        MinimapSession session = BuiltInHudModules.MINIMAP.getCurrentSession();
        if (session == null) {
            return false;
        }
        MinimapWorld world = session.getWorldManager().getCurrentWorld();
        if (world == null) {
            return false;
        }
        boolean removedAny = false;
        for (WaypointSet set : world.getIterableWaypointSets()) {
            List<Waypoint> toRemove = new ArrayList<>();
            for (Waypoint waypoint : set.getWaypoints()) {
                if (waypoint.getX() == x && waypoint.getY() == y && waypoint.getZ() == z
                        && name.equals(waypoint.getName())) {
                    toRemove.add(waypoint);
                }
            }
            for (Waypoint waypoint : toRemove) {
                set.remove(waypoint);
                removedAny = true;
            }
        }
        if (removedAny) {
            save(session, world);
            Shulkaero.LOGGER.info("Removed waypoint '{}' at {},{},{} in {}", name, x, y, z, world.getFullPath());
        }
        return removedAny;
    }

    private static WaypointColor resolveColor(ShulkerColors.ColorRef colorRef) {
        try {
            return WaypointColor.valueOf(colorRef.enumName());
        } catch (IllegalArgumentException e) {
            // Older Xaero's Minimap without the extended color palette
            return WaypointColor.fromIndex(colorRef.fallbackIndex());
        }
    }

    private static void save(MinimapSession session, MinimapWorld world) {
        try {
            session.getWorldManagerIO().saveWorld(world);
        } catch (IOException e) {
            Shulkaero.LOGGER.warn("Failed to save Xaero waypoints", e);
        }
    }
}
