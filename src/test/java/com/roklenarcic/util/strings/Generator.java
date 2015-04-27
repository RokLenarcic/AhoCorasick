package com.roklenarcic.util.strings;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

class Generator {

    public static String combinedStrings(String[] s, int max) {
        String testString = "";
        for (int i = 0; i < max; i++) {
            testString = testString + " " + s[i];
        }
        return testString;

    }

    public static String[] randomNumbers(int n) {
        final String[] keywords = new String[n];
        Random r = new Random();
        for (int i = 0; i < keywords.length; i++) {
            keywords[i] = String.format("%10d", r.nextInt());
        }
        return keywords;
    }

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

    public static String[] repeating(final int n, String s) {
        final String[] keywords = new String[100];
        keywords[0] = s;
        for (int i = 1; i < keywords.length; i++) {
            keywords[i] = keywords[i - 1] + s;
        }
        return keywords;
    }
}
