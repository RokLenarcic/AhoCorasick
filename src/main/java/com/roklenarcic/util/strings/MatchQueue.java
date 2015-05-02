package com.roklenarcic.util.strings;

class MatchQueue {
    private int emptySlotIdx = 0;
    private int[] endIndexes = new int[2];
    private int[] startIndexes = new int[2];

    public void clear() {
        emptySlotIdx = 0;
    }

    public boolean isEmpty() {
        return emptySlotIdx == 0;
    }

    public boolean matchAndClear(MatchListener listener, int purgeToIndex) {
        if (!isEmpty()) {
            int i = 0;
            while (i < emptySlotIdx) {
                if (endIndexes[i] <= purgeToIndex) {
                    if (!listener.match(startIndexes[i], endIndexes[i])) {
                        return false;
                    }
                } else {
                    break;
                }
                i++;
            }
            emptySlotIdx = emptySlotIdx - i;
            System.arraycopy(endIndexes, i, endIndexes, 0, emptySlotIdx);
            System.arraycopy(startIndexes, i, startIndexes, 0, emptySlotIdx);
        }
        return true;
    }

    public boolean push(int length, int idx) {
        if (emptySlotIdx + 1 == endIndexes.length) {
            int newCapacity = endIndexes.length * 2;
            if (newCapacity < 0) {
                newCapacity = Integer.MAX_VALUE - 8;
            }
            int[] newArr = new int[newCapacity];
            int[] newStartIndexes = new int[newCapacity];
            System.arraycopy(endIndexes, 0, newArr, 0, emptySlotIdx);
            System.arraycopy(startIndexes, 0, newStartIndexes, 0, emptySlotIdx);
            endIndexes = newArr;
            startIndexes = newStartIndexes;
        }
        // See if the new match overlaps with existing matches.
        // This assumes that matches have non-descending idx.
        if (!isEmpty()) {
            int idxToFind = idx - length;
            for (int currSlot = emptySlotIdx - 1; currSlot >= 0; currSlot--) {
                int currStartIdx = startIndexes[currSlot];
                if (idxToFind >= currStartIdx) {
                    // Match in the current slot starts before the new one
                    // Or starts at the same point.
                    // If the new match start after the end of the current one OR
                    // it starts on the same index but is longer, the new match will
                    // replace the one in the current slot.
                    if (idxToFind >= endIndexes[currSlot]) {
                        startIndexes[currSlot + 1] = idxToFind;
                        endIndexes[currSlot + 1] = idx;
                        emptySlotIdx = currSlot + 2;
                        return true;
                    } else if (idxToFind == currStartIdx && endIndexes[currSlot] < idx) {
                        startIndexes[currSlot] = idxToFind;
                        endIndexes[currSlot] = idx;
                        emptySlotIdx = currSlot + 1;
                        return true;
                    } else {
                        return false;
                    }
                }
            }
            startIndexes[0] = idxToFind;
            endIndexes[0] = idx;
            emptySlotIdx = 1;
            return true;
        } else {
            startIndexes[emptySlotIdx] = idx - length;
            endIndexes[emptySlotIdx] = idx;
            emptySlotIdx++;
            return true;
        }
    }
}
