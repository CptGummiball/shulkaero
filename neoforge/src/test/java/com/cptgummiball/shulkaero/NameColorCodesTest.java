package com.cptgummiball.shulkaero;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class NameColorCodesTest {

    @Test
    void findsFirstColorCode() {
        assertEquals(6, NameColorCodes.chatIndexOf("§6Goldlager"));
        assertEquals(12, NameColorCodes.chatIndexOf("§l§cWichtig"));
        assertEquals(0, NameColorCodes.chatIndexOf("Prefix §0Schwarz"));
    }

    @Test
    void upperCaseCodesWork() {
        assertEquals(11, NameColorCodes.chatIndexOf("§BAqua"));
    }

    @Test
    void noCodeReturnsNull() {
        assertNull(NameColorCodes.chatIndexOf("Nur Text"));
        assertNull(NameColorCodes.chatIndexOf("§lNurStil"));
        assertNull(NameColorCodes.chatIndexOf(null));
        assertNull(NameColorCodes.chatIndexOf("endet mit §"));
    }

    @Test
    void stripRemovesAllCodes() {
        assertEquals("GoldlagerRot", NameColorCodes.strip("§6Goldlager§cRot"));
        assertEquals("NurStil", NameColorCodes.strip("§lNurStil"));
        assertEquals("Nur Text", NameColorCodes.strip("Nur Text"));
        assertEquals("endet mit ", NameColorCodes.strip("endet mit §"));
    }
}
