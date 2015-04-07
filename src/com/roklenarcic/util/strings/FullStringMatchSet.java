package com.roklenarcic.util.strings;

// A set that matches only when one of the strings in the dictionary
// matches the whole input string.
// It is highly optimized for this particular use.
class FullStringMatchSet {

	// FNV-1a hash
	private static int hash(char c) {
		// HASH_BASIS = 0x811c9dc5;
		final int HASH_PRIME = 16777619;
		return (((0x811c9dc5 ^ (c >> 8)) * HASH_PRIME) ^ (c & 0xff)) * HASH_PRIME;
	}

	private boolean caseSensitive = true;
	private TrieNode root;

	public FullStringMatchSet(final Iterable<String> keywords, boolean caseSensitive) {
		// Create the root node
		root = new TrieNode();
		// Add all keywords
		for (final String keyword : keywords) {
			// Skip any empty keywords
			if (keyword != null && keyword.length() > 0) {
				// Start with the current node and traverse the tree
				// character by character. Add nodes as needed to
				// fill out the tree.
				TrieNode currentNode = root;
				for (int idx = 0; idx < keyword.length(); idx++) {
					currentNode = currentNode.getOrAddChild(caseSensitive ? keyword.charAt(idx) : Character.toLowerCase(keyword.charAt(idx)));
				}
			}
		}
		// Go through nodes depth first, swap any hashmap nodes,
		// whose size is close to the size of range of keys with
		// flat array based nodes.
		root.optimizeNodes();
	}

	public void match(final String haystack, final MatchListener listener) {

		// Start with the root node.
		TrieNode currentNode = root;

		int idx = 0;
		// For each character.
		final int len = haystack.length();
		// Putting this if into the loop worsens the performance so we'll sadly
		// have to deal with duplicated code.
		if (caseSensitive) {
			while (idx < len) {
				final char c = haystack.charAt(idx++);
				// Try to transition from the current node using the character
				if (currentNode.keys == null) {
					// First check if the key is between max and min value.
					// Here we use the fact that char type is unsigned to figure it out
					// with a single condition.
					int slot = (char) (c - currentNode.baseCharOrModulusMask);
					if (slot < currentNode.size) {
						currentNode = currentNode.children[slot];
						if (currentNode == null) {
							return;
						}
					} else {
						return;
					}
				} else {
					int defaultSlot = hash(c) & currentNode.baseCharOrModulusMask;
					int currentSlot = defaultSlot;
					// Linear probing to find the entry for key.
					do {
						if (currentNode.children[currentSlot] == null) {
							return;
						} else if (currentNode.keys[currentSlot] == c) {
							currentNode = currentNode.children[currentSlot];
							continue;
						} else {
							currentSlot = ++currentSlot & currentNode.baseCharOrModulusMask;
						}
					} while (currentSlot != defaultSlot);
					return;
				}
			}
			listener.match(haystack, len);
		} else {
			while (idx < len) {
				final char c = Character.toLowerCase(haystack.charAt(idx++));
				// Try to transition from the current node using the character
				if (currentNode.keys == null) {
					// First check if the key is between max and min value.
					// Here we use the fact that char type is unsigned to figure it out
					// with a single condition.
					int slot = (char) (c - currentNode.baseCharOrModulusMask);
					if (slot < currentNode.size) {
						currentNode = currentNode.children[slot];
						if (currentNode == null) {
							return;
						}
					} else {
						return;
					}
				} else {
					int defaultSlot = hash(c) & currentNode.baseCharOrModulusMask;
					int currentSlot = defaultSlot;
					// Linear probing to find the entry for key.
					do {
						if (currentNode.children[currentSlot] == null) {
							return;
						} else if (currentNode.keys[currentSlot] == c) {
							currentNode = currentNode.children[currentSlot];
							continue;
						} else {
							currentSlot = ++currentSlot & currentNode.baseCharOrModulusMask;
						}
					} while (currentSlot != defaultSlot);
					return;
				}
			}
			listener.match(haystack, len);
		}
	}

	// Basic node for both
	private final static class TrieNode {

		private int baseCharOrModulusMask = 0;
		private TrieNode[] children = new TrieNode[1];
		private char[] keys = new char[1];
		private int size = 0;

		// Return the node for a key or create a new hashmap node for that key
		// and return that.
		public TrieNode getOrAddChild(char key) {
			// Check if we need to resize. Capacity of 2^16 doesn't need to resize.
			// If capacity is <16 and arrays are full or capacity is >16 and
			// arrays are 90% full, resize
			if (keys.length < 0x10000 && ((size >= keys.length) || (size > 16 && (size >= keys.length * 0.90f)))) {
				enlarge();
			}
			++size;
			int defaultSlot = hash(key) & baseCharOrModulusMask;
			int currentSlot = defaultSlot;
			do {
				if (children[currentSlot] == null) {
					keys[currentSlot] = key;
					TrieNode newChild = new TrieNode();
					children[currentSlot] = newChild;
					return newChild;
				} else if (keys[currentSlot] == key) {
					return children[currentSlot];
				} else {
					currentSlot = ++currentSlot & baseCharOrModulusMask;
				}
			} while (currentSlot != defaultSlot);
			throw new IllegalStateException();
		}

		// A recursive function that replaces hashmap nodes with range nodes
		// when appropriate.
		public final void optimizeNodes() {
			char minKey = '\uffff';
			char maxKey = 0;
			// Find you the min and max key on the node.
			for (int i = 0; i < children.length; i++) {
				if (children[i] != null) {
					children[i].optimizeNodes();
					if (keys[i] > maxKey) {
						maxKey = keys[i];
					}
					if (keys[i] < minKey) {
						minKey = keys[i];
					}
				}
			}
			// If difference between min and max key are small
			// or only slightly larger than number of entries, use a range node
			int keyIntervalSize = maxKey - minKey + 1;
			if (keyIntervalSize <= 8 || (size > (keyIntervalSize) * 0.70)) {
				// Value of the first character
				baseCharOrModulusMask = minKey;
				size = maxKey - minKey + 1;
				// Avoid even allocating a children array if size is 0.
				if (size <= 0) {
					children = null;
					size = 0;
					keys = null;
				} else {
					TrieNode[] newChildren = new TrieNode[size];
					// Grab the children of the old node.
					for (int i = 0; i < children.length; i++) {
						if (children[i] != null) {
							newChildren[keys[i] - minKey] = children[i];
						}
					}
					children = newChildren;
					keys = null;
				}
			}
		}

		// Double the capacity of the node, calculate the new mask,
		// rehash and reinsert the entries
		private void enlarge() {
			char[] biggerKeys = new char[keys.length * 2];
			TrieNode[] biggerChildren = new TrieNode[children.length * 2];
			int biggerMask = biggerKeys.length - 1;
			for (int i = 0; i < children.length; i++) {
				char key = keys[i];
				TrieNode node = children[i];
				if (node != null) {
					int defaultSlot = hash(key) & biggerMask;
					int currentSlot = defaultSlot;
					do {
						if (biggerChildren[currentSlot] == null) {
							biggerKeys[currentSlot] = key;
							biggerChildren[currentSlot] = node;
							break;
						} else if (biggerKeys[currentSlot] == key) {
							throw new IllegalStateException();
						} else {
							currentSlot = ++currentSlot & biggerMask;
						}
					} while (currentSlot != defaultSlot);
				}
			}
			this.keys = biggerKeys;
			this.children = biggerChildren;
			this.baseCharOrModulusMask = biggerMask;
		}

	}

}
