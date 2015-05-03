package com.roklenarcic.util.strings;

import java.io.IOException;
import java.util.List;

public class FullStringMatchMapTest extends MapTest {

    public static void main(final String[] args) throws IOException {
        System.in.read();
        new FullStringMatchMapTest(true, 1000000).testLiteral();
        new FullStringMatchMapTest(true, 1000000).testOverlap();
        new FullStringMatchMapTest(true, 1000000).testLongKeywords();
        new FullStringMatchMapTest(true, 1000000).testFullRandom();
        new FullStringMatchMapTest(true, 1000000).testFailureTransitions();
        new FullStringMatchMapTest(true, 1000000).testDictionary();
        new FullStringMatchMapTest(true, 1000000).testShortestMatch();
    }

    public FullStringMatchMapTest() {
        super(false, 10000);
    }

    private FullStringMatchMapTest(final boolean printTimesOnly, int testLoopSize) {
        super(printTimesOnly, testLoopSize);
    }

    @Override
    protected int getCorrectCount(List<String> keywords, String haystack, StringMap<String> map) {
        int normalCount = 0;
        for (final String needle : keywords) {
            if (needle.equals(haystack)) {
                normalCount++;
                break;
            }
        }
        return normalCount;
    }

    @Override
    protected StringMap<String> instantiateMap(List<String> keywords, boolean caseSensitive) {
        return new FullStringMatchMap<String>(keywords.iterator(), keywords.iterator(), caseSensitive);
    }

}
