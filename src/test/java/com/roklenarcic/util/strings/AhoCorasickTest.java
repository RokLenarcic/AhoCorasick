package com.roklenarcic.util.strings;

import java.io.IOException;
import java.util.List;

public class AhoCorasickTest extends SetTest {

    public static void main(final String[] args) throws IOException {
        System.in.read();
        new AhoCorasickTest(true, 1000000).testLiteral();
        new AhoCorasickTest(true, 1000000).testOverlap();
        new AhoCorasickTest(true, 1000000).testLongKeywords();
        new AhoCorasickTest(true, 1000000).testFullRandom();
        new AhoCorasickTest(true, 1000000).testFailureTransitions();
        new AhoCorasickTest(true, 1000000).testDictionary();
        new AhoCorasickTest(true, 1000000).testShortestMatch();
    }

    public AhoCorasickTest() {
        super();
    }

    private AhoCorasickTest(boolean printTimesOnly, int testLoopSize) {
        super(printTimesOnly, testLoopSize);
    }

    @Override
    protected int getCorrectCount(List<String> keywords, String haystack, StringSet set) {
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
    protected StringSet instantiateSet(List<String> keywords, boolean caseSensitive) {
        return new AhoCorasickSet(keywords.iterator(), caseSensitive);
    }

}
