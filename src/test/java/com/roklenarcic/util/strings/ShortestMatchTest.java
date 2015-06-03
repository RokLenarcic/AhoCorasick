package com.roklenarcic.util.strings;

import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class ShortestMatchTest extends SetTest {

    public static void main(final String[] args) throws IOException {
        System.in.read();
        new ShortestMatchTest(true, 1000000).testLiteral();
        new ShortestMatchTest(true, 1000000).testOverlap();
        new ShortestMatchTest(true, 1000000).testLongKeywords();
        new ShortestMatchTest(true, 1000000).testFullRandom();
        new ShortestMatchTest(true, 1000000).testFailureTransitions();
        new ShortestMatchTest(true, 1000000).testDictionary();
        new ShortestMatchTest(true, 1000000).testShortestMatch();
    }

    public ShortestMatchTest() {
        super();
    }

    private ShortestMatchTest(boolean printTimesOnly, int testLoopSize) {
        super(printTimesOnly, testLoopSize);
    }

    @Override
    protected int getCorrectCount(List<String> keywords, String haystack, StringSet set) {
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
    protected StringSet instantiateSet(List<String> keywords, boolean caseSensitive) {
        return new ShortestMatchSet(keywords, caseSensitive);
    }

    @Override
    protected List<String> prepareKeywords(String[] keywords) {
        Arrays.sort(keywords, new Comparator<String>() {

            public int compare(String o1, String o2) {
                return o1.length() - o2.length();
            }
        });
        return super.prepareKeywords(keywords);
    }

}
