package com.roklenarcic.util.strings;

// This class is used in the longest non-overlapping matching.
class MapMatchQueue<T> {
    // It works like an arraylist that occasionally gets cleared at the front.
    // We don't bother downsizing it.
    private int emptySlotIdx = 0;
    private int[] endIndexes = new int[2];
    private int[] startIndexes = new int[2];
    private Object[] values = new Object[2];

    public void clear() {
        emptySlotIdx = 0;
    }

    public boolean isEmpty() {
        return emptySlotIdx == 0;
    }

    @SuppressWarnings("unchecked")
    public boolean matchAndClear(ReadableMatchListener<T> listener, int purgeToIndex) {
        // Start at the start of the array and flush to listener all matches which have
        // end index lower of equal than the purgeToIndex, then clear them out from array by
        // moving the rest of the matches to front.
        if (!isEmpty()) {
            int i = 0;
            while (i < emptySlotIdx) {
                if (endIndexes[i] <= purgeToIndex) {
                    if (!listener.match((T) values[i])) {
                        return false;
                    }
                } else {
                    break;
                }
                i++;
            }
            if (i > 0) {
                emptySlotIdx = emptySlotIdx - i;
                System.arraycopy(endIndexes, i, endIndexes, 0, emptySlotIdx);
                System.arraycopy(startIndexes, i, startIndexes, 0, emptySlotIdx);
                System.arraycopy(values, i, values, 0, emptySlotIdx);
            }
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    public boolean matchAndClear(String haystack, MapMatchListener<T> listener, int purgeToIndex) {
        // Start at the start of the array and flush to listener all matches which have
        // end index lower of equal than the purgeToIndex, then clear them out from array by
        // moving the rest of the matches to front.
        if (!isEmpty()) {
            int i = 0;
            while (i < emptySlotIdx) {
                if (endIndexes[i] <= purgeToIndex) {
                    if (!listener.match(haystack, startIndexes[i], endIndexes[i], (T) values[i])) {
                        return false;
                    }
                } else {
                    break;
                }
                i++;
            }
            if (i > 0) {
                emptySlotIdx = emptySlotIdx - i;
                System.arraycopy(endIndexes, i, endIndexes, 0, emptySlotIdx);
                System.arraycopy(startIndexes, i, startIndexes, 0, emptySlotIdx);
                System.arraycopy(values, i, values, 0, emptySlotIdx);
            }
        }
        return true;
    }

    // Adds a match to the queue.
    public boolean push(int length, int idx, T value) {
        // Resize if needed.
        if (emptySlotIdx + 1 == endIndexes.length) {
            int newCapacity = endIndexes.length * 2;
            if (newCapacity < 0) {
                newCapacity = Integer.MAX_VALUE - 8;
            }
            int[] newArr = new int[newCapacity];
            int[] newStartIndexes = new int[newCapacity];
            Object[] newValues = new Object[newCapacity];
            System.arraycopy(endIndexes, 0, newArr, 0, emptySlotIdx);
            System.arraycopy(startIndexes, 0, newStartIndexes, 0, emptySlotIdx);
            System.arraycopy(values, 0, newValues, 0, emptySlotIdx);
            endIndexes = newArr;
            startIndexes = newStartIndexes;
            values = newValues;
        }
        // See if the new match overlaps with existing matches.
        // This assumes that matches have non-descending end index.
        if (!isEmpty()) {
            int idxToFind = idx - length;
            for (int currSlot = emptySlotIdx - 1; currSlot >= 0; currSlot--) {
                int currStartIdx = startIndexes[currSlot];
                if (idxToFind >= currStartIdx) {
                    // Match in the current slot starts before the new one or starts at the same point.
                    // If the new match starts after the end of the current one OR
                    // it starts on the same index but is longer, the new match will
                    // replace the one in the current slot.
                    if (idxToFind >= endIndexes[currSlot]) {
                        startIndexes[currSlot + 1] = idxToFind;
                        endIndexes[currSlot + 1] = idx;
                        values[currSlot + 1] = value;
                        emptySlotIdx = currSlot + 2;
                        return true;
                    } else if (idxToFind == currStartIdx && endIndexes[currSlot] < idx) {
                        startIndexes[currSlot] = idxToFind;
                        endIndexes[currSlot] = idx;
                        values[currSlot] = value;
                        emptySlotIdx = currSlot + 1;
                        return true;
                    } else {
                        return false;
                    }
                }
            }
            startIndexes[0] = idxToFind;
            endIndexes[0] = idx;
            values[0] = value;
            emptySlotIdx = 1;
            return true;
        } else {
            startIndexes[emptySlotIdx] = idx - length;
            endIndexes[emptySlotIdx] = idx;
            values[emptySlotIdx] = value;
            emptySlotIdx++;
            return true;
        }
    }
}
