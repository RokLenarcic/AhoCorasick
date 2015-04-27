package com.roklenarcic.util.strings;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

public class FullStringMatchTest {

    public static void main(final String[] args) throws IOException {
        System.in.read();
        new FullStringMatchTest(true, 1000000).testLongNumbers();
        new FullStringMatchTest(true, 1000000).testFullRandom();
    }

    @Rule
    public TestName name = new TestName();

    private final boolean printTimesOnly;
    private int testLoopSize = 10000;

    public FullStringMatchTest() {
        this(false, 10000);
    }

    private FullStringMatchTest(final boolean printTimesOnly, int testLoopSize) {
        this.printTimesOnly = printTimesOnly;
        this.testLoopSize = testLoopSize;
    }

    @Test
    public void testEmptyString() {
        final String[] smallDict = Generator.randomStrings(10000, 2, 3);
        test("", smallDict);
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
    public void testLongNumbers() {
        final String[] keywords = Generator.randomNumbers(100);
        test(keywords[keywords.length - 1], keywords);
    }

    private void test(final String haystack, final String... needles) {
        final List<String> keywords = Arrays.asList(needles);
        final FullStringMatchSet set = new FullStringMatchSet(keywords, true);
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
            String haystackShort = haystack.length() > 20 ? haystack.substring(0, 20) + "..." : haystack;
            System.out.println(haystackShort + " in " + name.getMethodName() + " searched (matches " + listener.count + ") in " + time + "ns");
        }
        // Check count
        final long countStartTime = System.nanoTime();
        int normalCount = 0;
        for (final String needle : needles) {
            if (needle.equals(haystack)) {
                normalCount++;
                break;
            }
        }
        if (!printTimesOnly) {
            System.out.println("Normal count completed in : " + (System.nanoTime() - countStartTime) + "ns");
        }
        Assert.assertTrue("AC found " + listener.count + " normal match found " + normalCount, normalCount == listener.count);
    }
}
