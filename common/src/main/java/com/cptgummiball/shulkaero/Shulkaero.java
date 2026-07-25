package com.cptgummiball.shulkaero;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Shulkaero — places a Xaero's Minimap / World Map waypoint whenever the player
 * places a shulker box, using the box's (custom) name and dye color, and removes
 * the waypoint again when the box is broken.
 */
public final class Shulkaero {

    public static final String MOD_ID = "shulkaero";
    public static final Logger LOGGER = LoggerFactory.getLogger("Shulkaero");

    private static Boolean xaeroAvailable;

    private Shulkaero() {
    }

    public static void init(String loader) {
        LOGGER.info("Shulkaero by CptGummiball initialized on {} ({})", loader,
                isXaeroAvailable() ? "Xaero's Minimap detected" : "Xaero's Minimap not found - mod stays idle");
    }

    /**
     * True if Xaero's Minimap (which provides the waypoint system also used by
     * Xaero's World Map) is present. Checked lazily so this mod can be installed
     * without Xaero and simply do nothing.
     */
    public static boolean isXaeroAvailable() {
        if (xaeroAvailable == null) {
            try {
                Class.forName("xaero.hud.minimap.BuiltInHudModules", false, Shulkaero.class.getClassLoader());
                xaeroAvailable = Boolean.TRUE;
            } catch (ClassNotFoundException e) {
                xaeroAvailable = Boolean.FALSE;
            }
        }
        return xaeroAvailable;
    }
}
