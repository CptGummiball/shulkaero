package com.cptgummiball.shulkaero;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class InitialsOfTest {

    @Test
    void twoWordsUseBothInitials() {
        assertEquals("DL", NameColorCodes.initialsOf("Diamanten Lager"));
    }

    @Test
    void singleWordUsesFirstLetter() {
        assertEquals("S", NameColorCodes.initialsOf("shulker"));
    }

    @Test
    void digitsCount() {
        assertEquals("1R", NameColorCodes.initialsOf("1x Redstone 4you"));
    }

    @Test
    void punctuationOnlyWordIsSkipped() {
        assertEquals("AB", NameColorCodes.initialsOf("Alpha - Beta"));
    }

    @Test
    void blankFallsBackToS() {
        assertEquals("S", NameColorCodes.initialsOf("   "));
        assertEquals("S", NameColorCodes.initialsOf("---"));
    }
}
