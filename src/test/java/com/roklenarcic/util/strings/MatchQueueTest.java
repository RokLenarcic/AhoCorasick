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
		Assert.assertArrayEquals(new int[] { 3, 6, 9 }, q.getIndexes());
		Assert.assertArrayEquals(new String[] { "abc", "cde", "abc" }, q.getMatches());
		q.push("abcdefg", 10);
		Assert.assertArrayEquals(new int[] { 3, 10 }, q.getIndexes());
		Assert.assertArrayEquals(new String[] { "abc", "abcdefg" }, q.getMatches());
	}

	@Test
	public void testMatchQueueExtendingOverlap() {
		MatchQueue q = new MatchQueue();
		q.push("abc", 3);
		q.push("abcd", 4);
		q.push("de", 5);
		Assert.assertArrayEquals(new int[] { 4 }, q.getIndexes());
		Assert.assertArrayEquals(new String[] { "abcd" }, q.getMatches());
	}

	@Test
	public void testMatchQueueSimple() {
		MatchQueue q = new MatchQueue();
		q.push("abc", 3);
		q.push("bc", 3);
		q.push("bc", 4);
		q.push("bc", 5);
		Assert.assertArrayEquals(new int[] { 3, 5 }, q.getIndexes());
		Assert.assertArrayEquals(new String[] { "abc", "bc" }, q.getMatches());
	}
}
