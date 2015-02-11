package net.rlenar.ahocorasick;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

public class BasicTest {

	public static void main(final String[] args) throws IOException {
		System.in.read();
		new BasicTest(true).testLiteral();
		new BasicTest(true).testOverlap();
		new BasicTest(true).testLongKeywords();
		new BasicTest(true).testFullRandom();
		new BasicTest(true).testFailureTransitions();
	}

	@Rule
	public TestName name = new TestName();

	private final boolean printTimesOnly;

	public BasicTest() {
		this(false);
	}

	private BasicTest(final boolean printTimesOnly) {
		this.printTimesOnly = printTimesOnly;
	}

	@Test
	public void testFailureTransitions() {
		test("abbccddeef", "bc", "cc", "bcc", "ccddee", "ccddeee", "d");
	}

	@Test
	public void testFullRandom() {
		final String[] smallDict = generateRandomStrings(10000, 2, 3);
		final String[] mediumDict = generateRandomStrings(100000, 2, 3);
		final String[] largeDict = generateRandomStrings(1000000, 2, 3);
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
		final String[] keywords = new String[100];
		keywords[0] = "a";
		for (int i = 1; i < keywords.length; i++) {
			keywords[i] = keywords[i - 1] + "a";
		}
		test(keywords[keywords.length - 1], keywords);
	}

	@Test
	public void testOverlap() {
		test("aaaa", "a", "aa", "aaa", "aaaa");
		test(" aaaaaaa aaababababaabaa ", "a", "aa", "aaa", "aaaa");
	}

	private String[] generateRandomStrings(final int n, final int minSize, final int maxSize) {
		final Set<String> ret = new HashSet<String>();
		final char[] buf = new char[maxSize];
		final Random r = new Random();
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < maxSize; j++) {
				if (r.nextBoolean()) {
					buf[j] = (char) r.nextInt(256);
				} else {
					buf[j] = (char) r.nextInt();
				}
			}
			ret.add(new String(buf, 0, r.nextInt(maxSize - minSize) + minSize));
		}
		return ret.toArray(new String[ret.size()]);
	}

	private void test(final String haystack, final String... needles) {
		final List<String> keywords = Arrays.asList(needles);
		final StringSet set = new StringSet(keywords);
		class CountingMatchListener implements MatchListener {

			int count = 0;

			public boolean match(final String word, final int endPosition) {
				count++;
				Assert.assertTrue("Could not find needle " + word + " at end position " + endPosition + " in \n" + haystack,
						keywords.contains(haystack.substring(endPosition - word.length(), endPosition)));
				// System.out.println(word);
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
		for (int i = 0; i < 10000; i++) {
			set.match(haystack, performanceListener);
		}
		final long time = (System.nanoTime() - timeStart) / 10000;
		if (printTimesOnly) {
			System.out.println(time);
		} else {
			System.out.println(haystack + " in " + name.getMethodName() + " searched (matches " + listener.count + ") in " + time + "ns");
		}
		// Check count
		final long countStartTime = System.nanoTime();
		int normalCount = 0;
		for (final String needle : needles) {
			for (int i = 0; i + needle.length() <= haystack.length(); i++) {
				if (haystack.substring(i, i + needle.length()).equals(needle)) {
					normalCount++;
				}
			}
		}
		if (!printTimesOnly) {
			System.out.println("Normal count completed in : " + (System.nanoTime() - countStartTime) + "ns");
		}
		Assert.assertTrue("AC found " + listener.count + " normal match found " + normalCount, normalCount == listener.count);
	}
}
