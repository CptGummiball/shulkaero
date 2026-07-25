package com.cptgummiball.shulkaero;

/**
 * Helpers for literal legacy color codes (§0-§f) in item names.
 * Pure string logic, unit-testable without Minecraft.
 */
public final class NameColorCodes {

    public static final char SECTION = '§';
    private static final String COLOR_CHARS = "0123456789abcdef";

    private NameColorCodes() {
    }

    /**
     * @return the chat color index (0-15) of the first §-color code in the
     *         string, or null if there is none
     */
    public static Integer chatIndexOf(String s) {
        if (s == null) {
            return null;
        }
        for (int i = 0; i < s.length() - 1; i++) {
            if (s.charAt(i) == SECTION) {
                int idx = COLOR_CHARS.indexOf(Character.toLowerCase(s.charAt(i + 1)));
                if (idx >= 0) {
                    return idx;
                }
            }
        }
        return null;
    }

    /**
     * Derives up to two uppercase initials from the name (first letter/digit of
     * the first two words), falling back to "S".
     */
    public static String initialsOf(String name) {
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

    /** Removes all §-formatting codes (color and style) from the string. */
    public static String strip(String s) {
        if (s == null || s.indexOf(SECTION) < 0) {
            return s;
        }
        StringBuilder out = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == SECTION) {
                i++; // skip the code character as well
                continue;
            }
            out.append(c);
        }
        return out.toString();
    }
}
