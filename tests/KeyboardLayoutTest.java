package jc;

public final class KeyboardLayoutTest {
    public static void main(String[] args) {
        int[] keyCodes = {
            45, 51, 33, 46, 48, 53, 49, 37, 43, 44,
            29, 47, 32, 34, 35, 36, 38, 39, 40, 74,
            75, 54, 52, 31, 50, 30, 42, 41, 55, 56
        };
        String expected = "/׳קראטוןםפשדגכעיחלךף,זסבהנמצתץ";
        for (int i = 0; i < keyCodes.length; i++) {
            char actual = Tail.hebrewCharacter(keyCodes[i], false);
            if (actual != expected.charAt(i)) {
                throw new AssertionError("keyCode " + keyCodes[i] + " expected U+"
                    + Integer.toHexString(expected.charAt(i)) + " but got U+"
                    + Integer.toHexString(actual));
            }
        }

        int[] alphabetKeys = {
            48, 31, 32, 47, 50, 49, 54, 38, 53, 36, 34,
            39, 42, 30, 52, 35, 44, 41, 33, 46, 29, 55
        };
        String alphabet = "אבגדהוזחטיכלמנסעפצקרשת";
        for (int i = 0; i < alphabetKeys.length; i++) {
            assertCharacter(alphabetKeys[i], false, alphabet.charAt(i));
        }

        assertCharacter(45, true, '?');
        assertCharacter(51, true, '\u05F4');
        assertCharacter(55, false, '\u05EA');
        assertCharacter(56, false, '\u05E5');
        assertCharacter(40, false, '\u05DA');
        assertCharacter(43, false, '\u05DD');
        assertCharacter(37, false, '\u05DF');
        assertCharacter(74, false, '\u05E3');
        assertCharacter(71, false, ']');
        assertCharacter(72, false, '[');
        assertCharacter(76, false, '.');
        assertCharacter(200, false, (char) 0);
    }

    private static void assertCharacter(int keyCode, boolean shifted, char expected) {
        char actual = Tail.hebrewCharacter(keyCode, shifted);
        if (actual != expected) {
            throw new AssertionError("keyCode " + keyCode + " expected U+"
                + Integer.toHexString(expected) + " but got U+"
                + Integer.toHexString(actual));
        }
    }
}
