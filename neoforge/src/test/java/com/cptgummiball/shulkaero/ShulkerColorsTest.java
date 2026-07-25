package com.cptgummiball.shulkaero;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ShulkerColorsTest {

    @Test
    void allSixteenDyesMap() {
        assertEquals(new ShulkerColors.ColorRef("WHITE", 15), ShulkerColors.forDyeName("white"));
        assertEquals(new ShulkerColors.ColorRef("GOLD", 6), ShulkerColors.forDyeName("orange"));
        assertEquals(new ShulkerColors.ColorRef("MAGENTA", 13), ShulkerColors.forDyeName("magenta"));
        assertEquals(new ShulkerColors.ColorRef("LIGHT_BLUE", 11), ShulkerColors.forDyeName("light_blue"));
        assertEquals(new ShulkerColors.ColorRef("YELLOW", 14), ShulkerColors.forDyeName("yellow"));
        assertEquals(new ShulkerColors.ColorRef("LIME", 10), ShulkerColors.forDyeName("lime"));
        assertEquals(new ShulkerColors.ColorRef("PINK", 13), ShulkerColors.forDyeName("pink"));
        assertEquals(new ShulkerColors.ColorRef("DARK_GRAY", 8), ShulkerColors.forDyeName("gray"));
        assertEquals(new ShulkerColors.ColorRef("GRAY", 7), ShulkerColors.forDyeName("light_gray"));
        assertEquals(new ShulkerColors.ColorRef("DARK_AQUA", 3), ShulkerColors.forDyeName("cyan"));
        assertEquals(new ShulkerColors.ColorRef("DARK_PURPLE", 5), ShulkerColors.forDyeName("purple"));
        assertEquals(new ShulkerColors.ColorRef("BLUE", 9), ShulkerColors.forDyeName("blue"));
        assertEquals(new ShulkerColors.ColorRef("BROWN", 4), ShulkerColors.forDyeName("brown"));
        assertEquals(new ShulkerColors.ColorRef("DARK_GREEN", 2), ShulkerColors.forDyeName("green"));
        assertEquals(new ShulkerColors.ColorRef("RED", 12), ShulkerColors.forDyeName("red"));
        assertEquals(new ShulkerColors.ColorRef("BLACK", 0), ShulkerColors.forDyeName("black"));
    }

    @Test
    void undyedAndUnknownFallBackToPurple() {
        ShulkerColors.ColorRef expected = new ShulkerColors.ColorRef("DARK_PURPLE", 5);
        assertEquals(expected, ShulkerColors.forDyeName(null));
        assertEquals(expected, ShulkerColors.forDyeName("unknown_future_color"));
    }
}
