package com.roklenarcic.util.strings;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

public class LongestMatchTest {

    public static void main(final String[] args) throws IOException {
        System.in.read();
        new LongestMatchTest(true, 1000000).testLiteral();
        new LongestMatchTest(true, 1000000).testOverlap();
        new LongestMatchTest(true, 1000000).testLongKeywords();
        new LongestMatchTest(true, 1000000).testFullRandom();
        new LongestMatchTest(true, 1000000).testFailureTransitions();
        new LongestMatchTest(true, 1000000).testDictionary();
        new LongestMatchTest(true, 1000000).testShortestMatch();
    }

    @Rule
    public TestName name = new TestName();

    private final boolean printTimesOnly;
    private int testLoopSize = 10000;

    public LongestMatchTest() {
        this(false, 10000);
    }

    private LongestMatchTest(final boolean printTimesOnly, int testLoopSize) {
        this.printTimesOnly = printTimesOnly;
        this.testLoopSize = testLoopSize;
    }

    @Test
    public void testDictionary() throws IOException {
        File dictFile = new File("/usr/share/dict/words");
        if (dictFile.exists()) {
            BufferedReader str = new BufferedReader(new FileReader(dictFile));
            try {
                List<String> words = new ArrayList<String>();
                String word = null;
                while ((word = str.readLine()) != null) {
                    words.add(word);
                }
                test("Values specified as nondelimited strings are interpreted according "
                        + "their length. For a string 8 or 14 characters long, the year is assumed" + " to be given by the first 4 characters. Otherwise, the "
                        + "year is assumed to be given by the first 2 characters. " + "The string is interpreted from left to right to find year,"
                        + " month, day, hour, minute, and second values, for as many parts" + " as are present in the string. This means you should not use "
                        + "strings that have fewer than 6 characters.", words.toArray(new String[words.size()]));
            } finally {
                str.close();
            }
        }
    }

    @Test
    public void testFailureTransitions() {
        test("abbccddeef", "bc", "cc", "bcc", "ccddee", "ccddeee", "d");
    }

    @Test
    public void testFullNode() {
        final String[] keywords = new String[65536];
        for (int i = 0; i < keywords.length; i++) {
            keywords[i] = String.valueOf((char) i);
        }
        test("\u0000\uffff\ufffe", keywords);
    }

    @Test
    public void testFullRandom() {
        final String[] smallDict = generateRandomStrings(10000, 2, 3);
        final String[] mediumDict = generateRandomStrings(100000, 2, 3);
        final String[] largeDict = generateRandomStrings(1000000, 2, 3);
        test("The quick red fox, jumps over the lazy brown dog.", smallDict);
        test("The quick red fox, jumps over the lazy brown dog.", mediumDict);
        test("The quick red fox, jumps over the lazy brown dog.", largeDict);
    }

    @Test
    public void testLiteral() {
        test("The quick red fox, jumps over the lazy brown dog.", "The", "quick", "red", "fox", "jumps", "over", "the", "lazy", "brown", "dog");
    }

    @Test
    public void testLongestMatch() {
        test("XXXYYZZ", "XXX", "YY", "XXXYYZZZ");
    }

    @Test
    public void testLongKeywords() {
        final String[] keywords = new String[100];
        keywords[0] = "a";
        for (int i = 1; i < keywords.length; i++) {
            keywords[i] = keywords[i - 1] + "a";
        }
        test(keywords[keywords.length - 1], keywords);
    }

    @Test
    public void testOverlap() {
        test("aaaa", "a", "aa", "aaa", "aaaa");
        test(" aaaaaaa aaababababaabaa ", "a", "aa", "aaa", "aaaa");
    }

    @Test
    public void testShortestMatch() {
        final String[] keywords = new String[1000];
        Random r = new Random();
        for (int i = 0; i < keywords.length; i++) {
            keywords[i] = String.format("%10d", r.nextInt());
        }
        String testString = "";
        for (int i = 0; i < 50; i++) {
            testString = testString + " " + keywords[i];
        }
        test(testString, keywords);
        test("abcyyyy", "abcd", "bcxxxx", "cyyyy");
    }

    private String[] generateRandomStrings(final int n, final int minSize, final int maxSize) {
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

    private void test(final String haystack, final String... needles) {
        Arrays.sort(needles, new Comparator<String>() {

            public int compare(String o1, String o2) {
                return o2.length() - o1.length();
            }
        });
        final List<String> keywords = Arrays.asList(needles);
        final LongestMatchSet set = new LongestMatchSet(keywords, true);
        System.gc();
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        class CountingMatchListener implements MatchListener {

            int count = 0;

            public boolean match(final String word, final int endPosition) {
                count++;
                Assert.assertTrue("Could not find needle " + word + " at end position " + endPosition + " in \n" + haystack,
                        keywords.contains(haystack.substring(endPosition - word.length(), endPosition)));
                return true;
            }
        }
        final CountingMatchListener listener = new CountingMatchListener();
        final MatchListener performanceListener = new MatchListener() {

            public boolean match(final String word, final int endPosition) {
                return true;
            }
        };
        set.match(haystack, listener);
        final long timeStart = System.nanoTime();
        for (int i = 0; i < testLoopSize; i++) {
            set.match(haystack, performanceListener);
        }
        final long time = (System.nanoTime() - timeStart) / testLoopSize;
        if (printTimesOnly) {
            System.out.println(time);
        } else {
            String haystackShort = haystack.length() > 40 ? haystack.substring(0, 40) + "..." : haystack;
            System.out.println(haystackShort + " in " + name.getMethodName() + " searched (matches " + listener.count + ") in " + time + "ns");
        }
        // Check count
        final long countStartTime = System.nanoTime();
        int normalCount = 0;
        for (int i = 0; i < haystack.length(); i++) {
            for (final String needle : needles) {
                if (i + needle.length() <= haystack.length() && haystack.substring(i, i + needle.length()).equals(needle)) {
                    normalCount++;
                    i += needle.length() - 1;
                    break;
                }
            }
        }
        if (!printTimesOnly) {
            System.out.println("Normal count completed in : " + (System.nanoTime() - countStartTime) + "ns");
        }
        Assert.assertTrue("Trie found " + listener.count + " normal match found " + normalCount, normalCount == listener.count);
    }
}
