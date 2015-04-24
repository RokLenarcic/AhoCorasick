package com.roklenarcic.util.strings;

import org.junit.Assert;
import org.junit.Test;

public class MatchQueueTest {

	@Test
	public void testMatchQueue() {
		MatchQueue q = new MatchQueue();
		q.push("abc", 3);
		q.push("cde", 6);
		q.push("abc", 9);
		q.push("abcdefghi", 10);
		Listener l = new Listener(new int[] { 3, 6, 9, 10 }, new String[] { "abc", "cde", "abc", "abcdefg" });
		q.matchAndClear(l, 10);
		q.push("abcdefg", 10);
		q.matchAndClear(l, 10);
		l.assertAllExpended();
	}

	@Test
	public void testMatchQueueExtendingOverlap() {
		MatchQueue q = new MatchQueue();
		q.push("abc", 3);
		q.push("abcd", 4);
		q.push("de", 5);
		Listener l = new Listener(new int[] { 4 }, new String[] { "abcd" });
		q.matchAndClear(l, 4);
		l.assertAllExpended();
	}

	@Test
	public void testMatchQueueSimple() {
		MatchQueue q = new MatchQueue();
		q.push("abc", 3);
		q.push("bc", 3);
		q.push("bc", 4);
		q.push("bc", 5);
		Listener l = new Listener(new int[] { 3, 5 }, new String[] { "abc", "bc" });
		q.matchAndClear(l, 5);
		l.assertAllExpended();
	}

	@Test
	public void testPartialClear() {
		MatchQueue q = new MatchQueue();
		q.push("abc", 3);
		q.push("cde", 6);
		q.push("abc", 9);
		q.push("abcdefghi", 10);
		Listener l = new Listener(new int[] { 3, 10 }, new String[] { "abc", "abcdefg" });
		q.matchAndClear(l, 4);
		q.push("abcdefg", 10);
		q.matchAndClear(l, 10);
		l.assertAllExpended();
	}

	private static class Listener implements MatchListener {

		private int i = 0;
		private int[] indexes;
		private String[] matches;

		public Listener(int[] indexes, String[] matches) {
			super();
			this.indexes = indexes;
			this.matches = matches;
		}

		public void assertAllExpended() {
			Assert.assertEquals(indexes.length, i);
		}

		public boolean match(String word, int endPosition) {
			Assert.assertEquals(indexes[i], endPosition);
			Assert.assertEquals(matches[i], word);
			i++;
			return true;
		}

	}
}
