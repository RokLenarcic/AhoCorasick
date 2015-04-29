package com.roklenarcic.util.strings;

import java.io.IOException;
import java.util.List;

public class FullStringMatchTest extends SetTest {

    public static void main(final String[] args) throws IOException {
        System.in.read();
        new FullStringMatchTest(true, 1000000).testLiteral();
        new FullStringMatchTest(true, 1000000).testOverlap();
        new FullStringMatchTest(true, 1000000).testLongKeywords();
        new FullStringMatchTest(true, 1000000).testFullRandom();
        new FullStringMatchTest(true, 1000000).testFailureTransitions();
        new FullStringMatchTest(true, 1000000).testDictionary();
        new FullStringMatchTest(true, 1000000).testShortestMatch();
    }

    public FullStringMatchTest() {
        super(false, 10000);
    }

    private FullStringMatchTest(final boolean printTimesOnly, int testLoopSize) {
        super(printTimesOnly, testLoopSize);
    }

    @Override
    protected int getCorrectCount(List<String> keywords, String haystack, StringSet set) {
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
    protected StringSet instantiateSet(List<String> keywords, boolean caseSensitive) {
        return new FullStringMatchSet(keywords, caseSensitive);
    }

}
