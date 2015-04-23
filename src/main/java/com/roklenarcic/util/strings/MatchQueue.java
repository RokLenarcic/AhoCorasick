package com.roklenarcic.util.strings;

public class MatchQueue {
	private int emptySlotIdx = 0;
	private int[] indexes = new int[2];
	private String[] matches = new String[2];

	public void clear() {
		emptySlotIdx = 0;
	}

	public int[] getIndexes() {
		if (isEmpty()) {
			return null;
		} else {
			int[] ret = new int[emptySlotIdx];
			for (int i = 0; i < ret.length; i++) {
				ret[i] = indexes[i];
			}
			return ret;
		}
	}

	public String[] getMatches() {
		if (isEmpty()) {
			return null;
		} else {
			String[] ret = new String[emptySlotIdx];
			for (int i = 0; i < ret.length; i++) {
				ret[i] = matches[i];
			}
			return ret;
		}
	}

	public boolean isEmpty() {
		return emptySlotIdx == 0;
	}

	public boolean push(String match, int idx) {
		if (emptySlotIdx + 1 == indexes.length) {
			int newCapacity = indexes.length * 2;
			if (newCapacity < 0) {
				newCapacity = Integer.MAX_VALUE - 8;
			}
			int[] newArr = new int[newCapacity];
			String[] newMatches = new String[newCapacity];
			System.arraycopy(indexes, 0, newArr, 0, emptySlotIdx);
			System.arraycopy(matches, 0, newMatches, 0, emptySlotIdx);
			indexes = newArr;
			matches = newMatches;
		}
		// See if the new match overlaps with existing matches.
		// This assumes that matches have non-descending idx.
		if (!isEmpty()) {
			int idxToFind = idx - match.length();
			for (int currSlot = emptySlotIdx - 1; currSlot >= 0; currSlot--) {
				int currStartIdx = indexes[currSlot] - matches[currSlot].length();
				if (idxToFind >= currStartIdx) {
					// Match in the current slot starts before the new one
					// Or starts at the same point.
					// If the new match start after the end of the current one OR
					// it starts on the same index but is longer, the new match will
					// replace the one in the current slot.
					if (idxToFind >= indexes[currSlot]) {
						matches[currSlot + 1] = match;
						indexes[currSlot + 1] = idx;
						emptySlotIdx = currSlot + 2;
						return true;
					} else if (idxToFind == currStartIdx && indexes[currSlot] < idx) {
						matches[currSlot] = match;
						indexes[currSlot] = idx;
						emptySlotIdx = currSlot + 1;
						return true;
					} else {
						return false;
					}
				}
			}
			matches[0] = match;
			indexes[0] = idx;
			emptySlotIdx = 1;
			return true;
		} else {
			matches[emptySlotIdx] = match;
			indexes[emptySlotIdx] = idx;
			emptySlotIdx++;
			return true;
		}
	}
}
