package com.cptgummiball.shulkaero;

import net.minecraft.world.item.DyeColor;

/**
 * Maps a shulker box dye color to a Xaero waypoint color.
 * <p>
 * The Xaero color is referenced by enum constant name plus a fallback index into
 * the classic 16-color palette, because the extended colors (MAGENTA, LIGHT_BLUE,
 * LIME, PINK, BROWN) only exist in newer Xaero's Minimap versions. Resolution by
 * name happens in {@link XaeroIntegration} via valueOf with a fallback, so running
 * against an older Xaero build degrades gracefully.
 */
public final class ShulkerColors {

    /**
     * Reference to a Xaero waypoint color: enum constant name + classic palette
     * fallback index. enumName == null means "use the index directly" (used for
     * §-code derived colors).
     */
    public record ColorRef(String enumName, int fallbackIndex) {
    }

    // Classic 16-color palette indices (chat formatting order):
    // 0 BLACK, 1 DARK_BLUE, 2 DARK_GREEN, 3 DARK_AQUA, 4 DARK_RED, 5 DARK_PURPLE,
    // 6 GOLD, 7 GRAY, 8 DARK_GRAY, 9 BLUE, 10 GREEN, 11 AQUA, 12 RED, 13 PURPLE,
    // 14 YELLOW, 15 WHITE

    private static final ColorRef DEFAULT = new ColorRef("DARK_PURPLE", 5); // undyed shulker box

    private ShulkerColors() {
    }

    /**
     * @param dye the shulker box color, or null for the undyed (purple) box
     */
    public static ColorRef forDye(DyeColor dye) {
        return forDyeName(dye == null ? null : dye.getName());
    }

    /** Pure mapping by dye color name ("white", "red", ...), unit-testable without Minecraft. */
    public static ColorRef forDyeName(String dyeName) {
        if (dyeName == null) {
            return DEFAULT;
        }
        return switch (dyeName) {
            case "white" -> new ColorRef("WHITE", 15);
            case "orange" -> new ColorRef("GOLD", 6);
            case "magenta" -> new ColorRef("MAGENTA", 13);
            case "light_blue" -> new ColorRef("LIGHT_BLUE", 11);
            case "yellow" -> new ColorRef("YELLOW", 14);
            case "lime" -> new ColorRef("LIME", 10);
            case "pink" -> new ColorRef("PINK", 13);
            case "gray" -> new ColorRef("DARK_GRAY", 8);
            case "light_gray" -> new ColorRef("GRAY", 7);
            case "cyan" -> new ColorRef("DARK_AQUA", 3);
            case "purple" -> new ColorRef("DARK_PURPLE", 5);
            case "blue" -> new ColorRef("BLUE", 9);
            case "brown" -> new ColorRef("BROWN", 4);
            case "green" -> new ColorRef("DARK_GREEN", 2);
            case "red" -> new ColorRef("RED", 12);
            case "black" -> new ColorRef("BLACK", 0);
            default -> DEFAULT;
        };
    }
}
