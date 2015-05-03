package com.roklenarcic.util.strings;

import java.io.IOException;
import java.util.List;

public class AhoCorasickMapTest extends MapTest {

    public static void main(final String[] args) throws IOException {
        System.in.read();
        new AhoCorasickMapTest(true, 1000000).testLiteral();
        new AhoCorasickMapTest(true, 1000000).testOverlap();
        new AhoCorasickMapTest(true, 1000000).testLongKeywords();
        new AhoCorasickMapTest(true, 1000000).testFullRandom();
        new AhoCorasickMapTest(true, 1000000).testFailureTransitions();
        new AhoCorasickMapTest(true, 1000000).testDictionary();
        new AhoCorasickMapTest(true, 1000000).testShortestMatch();
    }

    public AhoCorasickMapTest() {
        super();
    }

    private AhoCorasickMapTest(boolean printTimesOnly, int testLoopSize) {
        super(printTimesOnly, testLoopSize);
    }

    @Override
    protected int getCorrectCount(List<String> keywords, String haystack, StringMap<String> map) {
        int normalCount = 0;
        for (final String needle : keywords) {
            for (int i = 0; i + needle.length() <= haystack.length(); i++) {
                if (haystack.substring(i, i + needle.length()).equals(needle)) {
                    normalCount++;
                }
            }
        }
        return normalCount;
    }

    @Override
    protected StringMap<String> instantiateMap(List<String> keywords, boolean caseSensitive) {
        return new AhoCorasickMap<String>(keywords.iterator(), keywords.iterator(), caseSensitive);
    }

}
