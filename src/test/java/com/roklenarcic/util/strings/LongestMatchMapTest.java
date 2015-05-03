package com.roklenarcic.util.strings;

import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class LongestMatchMapTest extends MapTest {

    public static void main(final String[] args) throws IOException {
        System.in.read();
        new LongestMatchMapTest(true, 1000000).testLiteral();
        new LongestMatchMapTest(true, 1000000).testOverlap();
        new LongestMatchMapTest(true, 1000000).testLongKeywords();
        new LongestMatchMapTest(true, 1000000).testFullRandom();
        new LongestMatchMapTest(true, 1000000).testFailureTransitions();
        new LongestMatchMapTest(true, 1000000).testDictionary();
        new LongestMatchMapTest(true, 1000000).testShortestMatch();
    }

    public LongestMatchMapTest() {
        super();
    }

    private LongestMatchMapTest(boolean printTimesOnly, int testLoopSize) {
        super(printTimesOnly, testLoopSize);
    }

    @Override
    protected int getCorrectCount(List<String> keywords, String haystack, StringMap<String> map) {
        int normalCount = 0;
        for (int i = 0; i < haystack.length(); i++) {
            for (final String needle : keywords) {
                if (i + needle.length() <= haystack.length() && haystack.substring(i, i + needle.length()).equals(needle)) {
                    normalCount++;
                    i += needle.length() - 1;
                    break;
                }
            }
        }
        return normalCount;
    }

    @Override
    protected StringMap<String> instantiateMap(List<String> keywords, boolean caseSensitive) {
        return new LongestMatchMap<String>(keywords.iterator(), keywords.iterator(), caseSensitive);
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

}
