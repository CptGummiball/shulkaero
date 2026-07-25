package com.cptgummiball.shulkaero;

import net.minecraft.world.item.DyeColor;

/**
 * Maps a shulker box dye color to a Xaero waypoint color.
 * <p>
 * The Xaero color is referenced by enum constant name plus a fallback index into
 * the classic 16-color palette, because the extended colors (MAGENTA, LIGHT_BLUE,
 * LIME, PINK, BROWN) only exist in newer Xaero's Minimap versions. Resolution by
 * name happens reflectively-safe in {@link XaeroIntegration} via valueOf with a
 * fallback, so running against an older Xaero build degrades gracefully.
 */
public final class ShulkerColors {

    /** Reference to a Xaero waypoint color: enum constant name + classic palette fallback index. */
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
        if (dye == null) {
            return DEFAULT;
        }
        return switch (dye) {
            case WHITE -> new ColorRef("WHITE", 15);
            case ORANGE -> new ColorRef("GOLD", 6);
            case MAGENTA -> new ColorRef("MAGENTA", 13);
            case LIGHT_BLUE -> new ColorRef("LIGHT_BLUE", 11);
            case YELLOW -> new ColorRef("YELLOW", 14);
            case LIME -> new ColorRef("LIME", 10);
            case PINK -> new ColorRef("PINK", 13);
            case GRAY -> new ColorRef("DARK_GRAY", 8);
            case LIGHT_GRAY -> new ColorRef("GRAY", 7);
            case CYAN -> new ColorRef("DARK_AQUA", 3);
            case PURPLE -> new ColorRef("DARK_PURPLE", 5);
            case BLUE -> new ColorRef("BLUE", 9);
            case BROWN -> new ColorRef("BROWN", 4);
            case GREEN -> new ColorRef("DARK_GREEN", 2);
            case RED -> new ColorRef("RED", 12);
            case BLACK -> new ColorRef("BLACK", 0);
        };
    }
}
