package com.roklenarcic.util.strings;

// Utility class for dealing with word characters.
class WordCharacters {

    public static boolean[] generateWordCharsFlags() {
        boolean[] characterFlags = new boolean[65536];
        characterFlags['-'] = true;
        characterFlags['_'] = true;
        for (int i = 0; i < characterFlags.length; i++) {
            if (Character.isLetterOrDigit((char) i)) {
                characterFlags[i] = true;
            }
        }
        return characterFlags;
    }

    public static boolean[] generateWordCharsFlags(char[] wordCharacters) {
        boolean[] characterFlags = new boolean[65536];
        for (char c : wordCharacters) {
            characterFlags[c] = true;
        }
        return characterFlags;
    }

    public static boolean[] generateWordCharsFlags(char[] wordCharacters, boolean[] toggleFlags) {
        boolean[] characterFlags = new boolean[65536];
        characterFlags['-'] = true;
        characterFlags['_'] = true;
        for (int i = 0; i < characterFlags.length; i++) {
            if (Character.isLetterOrDigit((char) i)) {
                characterFlags[i] = true;
            }
        }
        for (int i = 0; i < wordCharacters.length; i++) {
            characterFlags[wordCharacters[i]] = toggleFlags[i];
        }
        return characterFlags;
    }

    public static String trim(String keyword, boolean[] wordChars) {
        // Trim any non-word chars from the start and the end.
        int wordStart = 0;
        int wordEnd = keyword.length();
        for (int i = 0; i < keyword.length(); i++) {
            if (wordChars[keyword.charAt(i)]) {
                wordStart = i;
                break;
            }
        }
        for (int i = keyword.length() - 1; i >= 0; i--) {
            if (wordChars[keyword.charAt(i)]) {
                wordEnd = i + 1;
                break;
            }
        }
        // Don't substring if you don't have to.
        if (wordStart != 0 || wordEnd != keyword.length()) {
            keyword = keyword.substring(wordStart, wordEnd);
        }
        return keyword;
    }
}
