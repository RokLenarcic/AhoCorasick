package com.roklenarcic.util.strings;

import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.junit.Assert;

public class WholeWordLongestMatchTest extends SetTest {

    public static void main(final String[] args) throws IOException {
        System.in.read();
        new WholeWordLongestMatchTest(true, 1000000).testLiteral();
        new WholeWordLongestMatchTest(true, 1000000).testOverlap();
        new WholeWordLongestMatchTest(true, 1000000).testLongKeywords();
        new WholeWordLongestMatchTest(true, 1000000).testFullRandom();
        new WholeWordLongestMatchTest(true, 1000000).testFailureTransitions();
        new WholeWordLongestMatchTest(true, 1000000).testDictionary();
        new WholeWordLongestMatchTest(true, 1000000).testShortestMatch();
    }

    public WholeWordLongestMatchTest() {
        super();
    }

    private WholeWordLongestMatchTest(boolean printTimesOnly, int testLoopSize) {
        super(printTimesOnly, testLoopSize);
    }

    @Override
    protected void assertCorrectMatch(int startPosition, int endPosition, List<String> keywords, String haystack, StringSet set) {
        WholeWordLongestMatchSet wwset = (WholeWordLongestMatchSet) set;
        Assert.assertTrue("Could not find needle " + haystack.substring(startPosition, endPosition) + " at end position " + endPosition + " in set.",
                keywords.contains(haystack.substring(startPosition, endPosition)));
        Assert.assertTrue("Needle " + haystack.substring(startPosition, endPosition) + " at end position " + endPosition
                + " doesn't end in whitespace or string end in \n" + haystack,
                haystack.length() == endPosition || !wwset.getWordChars()[haystack.charAt(endPosition)]);
        Assert.assertTrue("Needle " + haystack.substring(startPosition, endPosition) + " at end position " + endPosition
                + " doesn't start in whitespace or string start in \n" + haystack,
                startPosition == 0 || !wwset.getWordChars()[haystack.charAt(startPosition - 1)]);
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
                    while (++i < haystack.length() && !((WholeWordLongestMatchSet) set).getWordChars()[haystack.charAt(i)]) {
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
        WholeWordLongestMatchSet s = new WholeWordLongestMatchSet(keywords.iterator(), caseSensitive);
        for (int i = 0; i < keywords.size(); i++) {
            keywords.set(i, trim(keywords.get(i), s.getWordChars()));
        }
        return s;
    }

    @Override
    protected List<String> prepareKeywords(String[] keywords) {
        Arrays.sort(keywords, new Comparator<String>() {

            public int compare(String o1, String o2) {
                return o2.length() - o1.length();
            }
        });
        return super.prepareKeywords(keywords);
    }

    private String trim(String keyword, boolean[] wordChars) {
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
