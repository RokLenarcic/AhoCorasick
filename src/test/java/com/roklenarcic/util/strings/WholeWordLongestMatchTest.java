package com.roklenarcic.util.strings;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

public class WholeWordLongestMatchTest {

    public static void main(final String[] args) throws IOException {
        System.in.read();
        new WholeWordLongestMatchTest(true, 1000000).testLiteral();
        new WholeWordLongestMatchTest(true, 1000000).testOverlap();
        new WholeWordLongestMatchTest(true, 1000000).testLongKeywords();
        new WholeWordLongestMatchTest(true, 1000000).testFullRandom();
        new WholeWordLongestMatchTest(true, 1000000).testFailureTransitions();
        new WholeWordLongestMatchTest(true, 1000000).testDictionary();
        new WholeWordLongestMatchTest(true, 1000000).testShortestMatch();
        new WholeWordLongestMatchTest(true, 1000000).testWholeWordLongest();
    }

    @Rule
    public TestName name = new TestName();

    private final boolean printTimesOnly;
    private int testLoopSize = 10000;

    public WholeWordLongestMatchTest() {
        this(false, 10000);
    }

    private WholeWordLongestMatchTest(final boolean printTimesOnly, int testLoopSize) {
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
    public void testFullRandom() {
        final String[] smallDict = Generator.randomStrings(10000, 2, 3);
        final String[] mediumDict = Generator.randomStrings(100000, 2, 3);
        final String[] largeDict = Generator.randomStrings(1000000, 2, 3);
        test("The quick red fox, jumps over the lazy brown dog.", smallDict);
        test("The quick red fox, jumps over the lazy brown dog.", mediumDict);
        test("The quick red fox, jumps over the lazy brown dog.", largeDict);
    }

    @Test
    public void testLiteral() {
        test("The quick red fox, jumps over the lazy brown dog.", "The", "quick", "red", "fox", "jumps", "over", "the", "lazy", "brown", "dog");
    }

    @Test
    public void testLongKeywords() {
        final String[] keywords = Generator.repeating(100, "a");
        test(keywords[keywords.length - 1], keywords);
    }

    @Test
    public void testOverlap() {
        test("aaaa", "a", "aa", "aaa", "aaaa");
        test(" aaaaaaa aaababababaabaa ", "a", "aa", "aaa", "aaaa");
    }

    @Test
    public void testShortestMatch() {
        final String[] keywords = Generator.randomNumbers(1000);
        test(Generator.combinedStrings(keywords, 50), keywords);
        test("abcyyyy", "abcd", "bcxxxx", "cyyyy");
    }

    @Test
    public void testWholeWordLongest() {
        test("as if", "as", "if", "as if");
        test("ax if", "as", "if", "as if");
        test("as in", "as", "if", "as if");
        test("123 4x 1234 5x 1234 56 123 45 1x 345 12 34x 12 345x 123xb 1234 56s", "123", "123 45", "1234 56", "12 345");
    }

    private void test(final String haystack, final String... needles) {
        Arrays.sort(needles, new Comparator<String>() {

            public int compare(String o1, String o2) {
                return o2.length() - o1.length();
            }
        });
        final List<String> keywords = Arrays.asList(needles);
        final WholeWordLongestMatchSet set = new WholeWordLongestMatchSet(keywords, true);
        for (int i = 0; i < keywords.size(); i++) {
            keywords.set(i, keywords.get(i).trim());
        }
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
                Assert.assertTrue("Needle " + word + " at end position " + endPosition + " doesn't end in whitespace or string end in \n" + haystack,
                        haystack.length() == endPosition || !Character.isLetterOrDigit(haystack.charAt(endPosition)));
                Assert.assertTrue("Needle " + word + " at end position " + endPosition + " doesn't start in whitespace or string start in \n" + haystack,
                        0 == (endPosition - word.length()) || !Character.isLetterOrDigit(haystack.charAt(endPosition - word.length() - 1)));
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
                if (needle.length() > 0 && i + needle.length() <= haystack.length() && haystack.substring(i, i + needle.length()).equals(needle)
                        && (i + needle.length() == haystack.length() || !Character.isLetterOrDigit(haystack.charAt(i + needle.length())))
                        && (i == 0 || !Character.isLetterOrDigit(haystack.charAt(i - 1)))) {
                    normalCount++;
                    i += needle.length() - 1;
                    while (++i < haystack.length() && !set.getWordChars()[haystack.charAt(i)]) {
                    }
                    i--;
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
