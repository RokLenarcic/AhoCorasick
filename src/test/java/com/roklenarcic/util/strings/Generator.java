package com.roklenarcic.util.strings;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * Class for generating test data.
 *
 * @author rok
 *
 */
class Generator {

    /**
     * Returns strings from input array joined by ' ' up to max elements.
     *
     * @param s
     *            input strings
     * @param max
     *            join up to this many elements
     * @return elements joined by ' '
     */
    public static String combinedStrings(String[] s, int max) {
        String testString = "";
        for (int i = 0; i < max && i < s.length; i++) {
            testString = testString + " " + s[i];
        }
        return testString;

    }

    /**
     * Return random ints as strings, padded to length 10.
     *
     * @param n
     *            number of strings generated
     * @return random ints as strings, padded to length 10
     */
    public static String[] randomNumbers(int n) {
        final String[] keywords = new String[n];
        Random r = new Random();
        for (int i = 0; i < keywords.length; i++) {
            keywords[i] = String.format("%10d", r.nextInt());
        }
        return keywords;
    }

    /**
     * Generates random strings with length in a certain range. The randomness is not uniform. The odds of a
     * character being an ASCII character is 50%.
     *
     * @param n
     *            number of strings to generate
     * @param minSize
     *            minimum size of strings
     * @param maxSize
     *            maximum size of strings
     * @return strings generated
     */
    public static String[] randomStrings(final int n, final int minSize, final int maxSize) {
        final Set<String> ret = new HashSet<String>();
        final char[] buf = new char[maxSize];
        final Random r = new Random();
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < maxSize; j++) {
                if (r.nextBoolean()) {
                    buf[j] = (char) r.nextInt(256);
                } else {
                    buf[j] = (char) r.nextInt();
                }
            }
            ret.add(new String(buf, 0, r.nextInt(maxSize - minSize) + minSize));
        }
        return ret.toArray(new String[ret.size()]);
    }

    /**
     * Generate a sequence of strings where each string is the given string concatenated to itself n-times.
     *
     * @param n
     *            number of strings to generate
     * @param s
     *            base string
     * @return generated strings
     */
    public static String[] repeating(final int n, String s) {
        final String[] keywords = new String[100];
        keywords[0] = s;
        for (int i = 1; i < keywords.length; i++) {
            keywords[i] = keywords[i - 1] + s;
        }
        return keywords;
    }
}
