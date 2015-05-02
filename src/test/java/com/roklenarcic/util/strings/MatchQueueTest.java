package com.roklenarcic.util.strings;

import org.junit.Assert;
import org.junit.Test;

public class MatchQueueTest {

    @Test
    public void testMatchQueue() {
        MatchQueue q = new MatchQueue();
        q.push(3, 3);
        q.push(3, 6);
        q.push(3, 9);
        q.push(9, 10);
        Listener l = new Listener(new int[] { 3, 6, 9, 10 }, new int[] { 3, 3, 3, 7 });
        q.matchAndClear(l, 10);
        q.push(7, 10);
        q.matchAndClear(l, 10);
        l.assertAllExpended();
    }

    @Test
    public void testMatchQueueExtendingOverlap() {
        MatchQueue q = new MatchQueue();
        q.push(3, 3);
        q.push(4, 4);
        q.push(2, 5);
        Listener l = new Listener(new int[] { 4 }, new int[] { 4 });
        q.matchAndClear(l, 4);
        l.assertAllExpended();
    }

    @Test
    public void testMatchQueueSimple() {
        MatchQueue q = new MatchQueue();
        q.push(3, 3);
        q.push(2, 3);
        q.push(2, 4);
        q.push(2, 5);
        Listener l = new Listener(new int[] { 3, 5 }, new int[] { 3, 2 });
        q.matchAndClear(l, 5);
        l.assertAllExpended();
    }

    @Test
    public void testPartialClear() {
        MatchQueue q = new MatchQueue();
        q.push(3, 3);
        q.push(3, 6);
        q.push(3, 9);
        q.push(9, 10);
        Listener l = new Listener(new int[] { 3, 10 }, new int[] { 3, 7 });
        q.matchAndClear(l, 4);
        q.push(7, 10);
        q.matchAndClear(l, 10);
        l.assertAllExpended();
    }

    private static class Listener implements MatchListener {

        private int i = 0;
        private int[] indexes;
        private int[] lengths;

        public Listener(int[] indexes, int[] lengths) {
            super();
            this.indexes = indexes;
            this.lengths = lengths;
        }

        public void assertAllExpended() {
            Assert.assertEquals(indexes.length, i);
        }

        public boolean match(int startPosition, int endPosition) {
            Assert.assertEquals(indexes[i], endPosition);
            Assert.assertEquals(lengths[i], endPosition - startPosition);
            i++;
            return true;
        }

    }
}
