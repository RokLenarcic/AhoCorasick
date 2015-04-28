package com.roklenarcic.util.strings;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

public class WholeWordMatchTest extends SetTest {

    public static void main(final String[] args) throws IOException {
        System.in.read();
        new WholeWordMatchTest(true, 1000000).testLiteral();
        new WholeWordMatchTest(true, 1000000).testOverlap();
        new WholeWordMatchTest(true, 1000000).testLongKeywords();
        new WholeWordMatchTest(true, 1000000).testFailureTransitions();
        new WholeWordMatchTest(true, 1000000).testDictionary();
        new WholeWordMatchTest(true, 1000000).testShortestMatch();
    }

    public WholeWordMatchTest() {
        super();
    }

    private WholeWordMatchTest(boolean printTimesOnly, int testLoopSize) {
        super(printTimesOnly, testLoopSize);
    }

    @Override
    @Ignore
    public void testEmptyString() {

    }

    @Override
    @Ignore
    public void testFullNode() {
    }

    @Override
    @Ignore
    public void testFullRandom() {
    }

    @Test(expected = IllegalArgumentException.class)
    public void testKeywordsWithNWCRejection() {
        new WholeWordMatchSet(Collections.singleton("A B"), true);
    }

    @Override
    @Ignore
    public void testWholeWordLongest() {
    }

    @Override
    protected void assertCorrectMatch(String word, int endPosition, List<String> keywords, String haystack, StringSet set) {
        WholeWordMatchSet wwset = (WholeWordMatchSet) set;
        System.out.println("Word " + (int) word.charAt(0) + " at length " + word.length());
        Assert.assertTrue("Could not find needle " + word + " at end position " + endPosition + " in \n" + haystack,
                keywords.contains(haystack.substring(endPosition - word.length(), endPosition)));
        Assert.assertTrue("Needle " + word + " at end position " + endPosition + " doesn't end in whitespace or string end in \n" + haystack,
                haystack.length() == endPosition || !wwset.getWordChars()[haystack.charAt(endPosition)]);
        Assert.assertTrue("Needle " + word + " at end position " + endPosition + " doesn't start in whitespace or string start in \n" + haystack,
                0 == (endPosition - word.length()) || !wwset.getWordChars()[haystack.charAt(endPosition - word.length() - 1)]);
    }

    @Override
    protected int getCorrectCount(List<String> keywords, String haystack, StringSet set) {
        int normalCount = 0;
        for (int i = 0; i < haystack.length(); i++) {
            for (final String needle : keywords) {
                if (needle.length() > 0 && i + needle.length() <= haystack.length() && haystack.substring(i, i + needle.length()).equals(needle)
                        && (i + needle.length() == haystack.length() || !Character.isLetterOrDigit(haystack.charAt(i + needle.length())))
                        && (i == 0 || !Character.isLetterOrDigit(haystack.charAt(i - 1)))) {
                    normalCount++;
                    i += needle.length() - 1;
                    while (++i < haystack.length() && !((WholeWordMatchSet) set).getWordChars()[haystack.charAt(i)]) {
                    }
                    i--;
                    break;
                }
            }
        }
        return normalCount;
    }

    @Override
    protected StringSet instantiateSet(List<String> keywords, boolean caseSensitive) {
        StringSet s = new WholeWordMatchSet(keywords, caseSensitive);
        for (int i = 0; i < keywords.size(); i++) {
            keywords.set(i, keywords.get(i).trim());
        }
        return s;
    }

}
