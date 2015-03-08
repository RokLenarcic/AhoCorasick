package com.roklenarcic.util.strings;

import java.util.Arrays;

// Standard Aho-Corasick set
// It matches all occurences of the strings in the set anywhere.
// It is highly optimized for this particular use.
class AhoCorasickSet {

	private boolean caseSensitive = true;
	private TrieNode root;

	public AhoCorasickSet(final Iterable<String> keywords, boolean caseSensitive) {
		// Create the root node
		root = new HashmapNode(true);
		// Add all keywords
		for (final String keyword : keywords) {
			// Skip any empty keywords
			if (keyword != null && keyword.length() > 0) {
				// Start with the current node and traverse the tree
				// character by character. Add nodes as needed to
				// fill out the tree.
				HashmapNode currentNode = (HashmapNode) root;
				for (int idx = 0; idx < keyword.length(); idx++) {
					currentNode = currentNode.getOrAddChild(caseSensitive ? keyword.charAt(idx) : Character.toLowerCase(keyword.charAt(idx)));
				}
				// Last node will contains the keyword as a match.
				// Suffix matches will be added later.
				currentNode.match = new Match(keyword);
			}
		}
		// Go through nodes depth first, swap any hashmap nodes,
		// whose size is close to the size of range of keys with
		// flat array based nodes.
		root = optimizeNodes(root);

		// Calculate fail transitions and add suffix matches to nodes.
		// A lot of these properties are defined in a recursive fashion i.e.
		// calculating for a 3 letter word requires having done the calculation
		// for all 2 letter words.
		//
		// Setup a queue to enable breath-first processing.
		final Queue queue = new Queue();
		EntryVisitor queueingVisitor = new EntryVisitor() {

			public void visit(TrieNode parent, char key, TrieNode value) {
				// Get fail transiton of the parent.
				TrieNode parentFail = parent.getFailTransition();
				// Since root node has null fail transition, first level nodes have null parentFail.
				if (parentFail == null) {
					// First level nodes have one possible fail transition, which is
					// root because the only possible suffix to a one character
					// string is an empty string
					value.failTransition = parent;
				} else {
					// Dig up the tree until you find a fail transition.
					do {
						// suffix of parent + key = suffix of this node
						// parent -> char -> value
						// parentFail -> char -> valueFail
						// e.g. "ab" -> c -> "abc"
						// "b" -> c -> "bc"
						final TrieNode matchContinuation = parentFail.getTransition(key);
						if (matchContinuation != null) {
							value.failTransition = matchContinuation;
						} else {
							// If parentFail didn't have key mapping
							// take parentFail's failTransition and try again
							// The last fail transition is the root node, which
							// always has a key mapping.
							parentFail = parentFail.getFailTransition();
						}
					} while (value.failTransition == null);
					// Now that we have a fail transition, this node matches all
					// the matches of it's failTransition node in addition to any
					// match it already has.
					// e.g for keywords "abc", "bc", "c", "b", the "abc" node matches
					// "abc" and also its failure transtion's matches ("bc", "c")
					// "ab" has no match of its own, but it matches failure transition's
					// match "b".
					if (value.match == null) {
						value.match = value.failTransition.getMatch();
					} else {
						value.match.subMatch = value.failTransition.getMatch();
					}
				}
				// Queue the non-leaf node.
				if (!value.isEmpty()) {
					queue.push(value);
				}
			}

		};
		root.mapEntries(queueingVisitor);
		while (!queue.isEmpty()) {
			queue.pop().mapEntries(queueingVisitor);
		}
	}

	public void match(final String haystack, final MatchListener listener) {

		// Start with the root node.
		TrieNode currentNode = root;

		int idx = 0;
		// For each character.
		final int len = haystack.length();
		while (idx < len) {
			final char c = caseSensitive ? haystack.charAt(idx) : Character.toLowerCase(haystack.charAt(idx));
			// Try to transition from the current node using the character
			TrieNode nextNode = currentNode.getTransition(c);

			// If cannot transition, follow the fail transition until finding
			// node X where you can transition to another node Y using this
			// character. Take the transition.
			while (nextNode == null) {
				// Transition follow one fail transition
				currentNode = currentNode.getFailTransition();
				// See if you can transition to another node with this
				// character. Note that root node will return itself for any
				// missing transition.
				nextNode = currentNode.getTransition(c);
			}
			// Take the transition.
			currentNode = nextNode;
			// Output any matches on the current node and increase the index
			if (!currentNode.output(listener, ++idx)) {
				break;
			}
		}
	}

	// A recursive function that replaces hashmap nodes with range nodes
	// when appropriate.
	private final TrieNode optimizeNodes(TrieNode n) {
		if (n instanceof HashmapNode) {
			HashmapNode node = (HashmapNode) n;
			char minKey = '\uffff';
			char maxKey = 0;
			// Find you the min and max key on the node.
			int size = node.numEntries;
			for (int i = 0; i < node.children.length; i++) {
				if (node.children[i] != null) {
					node.children[i] = optimizeNodes(node.children[i]);
					if (node.keys[i] > maxKey) {
						maxKey = node.keys[i];
					}
					if (node.keys[i] < minKey) {
						minKey = node.keys[i];
					}
				}
			}
			// If difference between min and max key are small
			// or only slightly larger than number of entries, use a range node
			int keyIntervalSize = maxKey - minKey + 1;
			if (keyIntervalSize <= 8 || (size > (keyIntervalSize) * 0.70)) {
				return new RangeNode(node, minKey, maxKey);
			}
		}
		return n;
	}

	private interface EntryVisitor {
		void visit(TrieNode parent, char key, TrieNode value);
	}

	// An open addressing hashmap implementation with linear probing
	// and capacity of 2^n
	private final static class HashmapNode extends TrieNode {

		// Start with capacity of 1 and resize as needed.
		private TrieNode[] children = new TrieNode[1];
		private char[] keys = new char[1];
		// Since capacity is a power of 2, we calculate mod by just
		// bitwise AND with the right mask.
		private int modulusMask = keys.length - 1;
		private int numEntries = 0;

		protected HashmapNode(boolean root) {
			super(root);
		}

		@Override
		public TrieNode getTransition(final char key) {
			int defaultSlot = hash(key) & modulusMask;
			int currentSlot = defaultSlot;
			// Linear probing to find the entry for key.
			do {
				if (keys[currentSlot] == key) {
					return children[currentSlot];
				} else if (children[currentSlot] == null) {
					return defaultTransition;
				} else {
					currentSlot = ++currentSlot & modulusMask;
				}
			} while (currentSlot != defaultSlot);
			return defaultTransition;
		}

		@Override
		public boolean isEmpty() {
			return numEntries == 0;
		}

		@Override
		public void mapEntries(EntryVisitor visitor) {
			for (int i = 0; i < keys.length; i++) {
				if (children[i] != null) {
					visitor.visit(this, keys[i], children[i]);
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
			this.modulusMask = biggerMask;
		}

		// Return the node for a key or create a new hashmap node for that key
		// and return that.
		private HashmapNode getOrAddChild(char key) {
			// Check if we need to resize. Capacity of 2^16 doesn't need to resize.
			// If capacity is <16 and arrays are full or capacity is >16 and
			// arrays are 90% full, resize
			if (keys.length < 0x10000 && ((numEntries >= keys.length) || (numEntries > 16 && (numEntries >= keys.length * 0.90f)))) {
				enlarge();
			}
			++numEntries;
			int defaultSlot = hash(key) & modulusMask;
			int currentSlot = defaultSlot;
			do {
				if (children[currentSlot] == null) {
					keys[currentSlot] = key;
					HashmapNode newChild = new HashmapNode(false);
					children[currentSlot] = newChild;
					return newChild;
				} else if (keys[currentSlot] == key) {
					return (HashmapNode) children[currentSlot];
				} else {
					currentSlot = ++currentSlot & modulusMask;
				}
			} while (currentSlot != defaultSlot);
			throw new IllegalStateException();
		}

		// FNV-1a hash
		private int hash(char c) {
			// HASH_BASIS = 0x811c9dc5;
			final int HASH_PRIME = 16777619;
			return (((0x811c9dc5 ^ (c >> 8)) * HASH_PRIME) ^ (c & 0xff)) * HASH_PRIME;
		}

	}

	// Single-linked list
	private static class Queue {

		private QueueNode first;
		private QueueNode last;

		boolean isEmpty() {
			return first == null;
		}

		TrieNode pop() {
			if (first != null) {
				TrieNode ret = first.n;
				first = first.next;
				if (first == null) {
					last = null;
				}
				return ret;
			} else {
				return null;
			}
		}

		void push(TrieNode n) {
			if (last != null) {
				last.next = new QueueNode(n);
				last = last.next;
			} else {
				first = last = new QueueNode(n);
			}
		}

		private static class QueueNode {
			TrieNode n;
			QueueNode next;

			private QueueNode(TrieNode n) {
				this.n = n;
			}
		}
	}

	// This node is good at representing dense ranges of keys.
	// It has a single array of nodes and a base key value.
	// Child at array index 3 has key of baseChar + 3.
	private static final class RangeNode extends TrieNode {

		private char baseChar = 0;
		private TrieNode[] children;
		private int size = 0;

		private RangeNode(HashmapNode oldNode, char from, char to) {
			super(oldNode.defaultTransition != null);
			// Value of the first character
			this.baseChar = from;
			this.size = to - from + 1;
			this.match = oldNode.match;
			// Avoid even allocating a children array if size is 0.
			if (size > 0) {
				this.children = new TrieNode[size];
				// If original node is root node, prefill everything with yourself.
				if (oldNode.defaultTransition != null) {
					Arrays.fill(children, this);
				}
				// Grab the children of the old node.
				for (int i = 0; i < oldNode.children.length; i++) {
					if (oldNode.children[i] != null) {
						children[oldNode.keys[i] - from] = oldNode.children[i];
					}
				}
			}
		}

		@Override
		public TrieNode getTransition(char c) {
			// First check if the key is between max and min value.
			// Here we use the fact that char type is unsigned to figure it out
			// with a single condition.
			int idx = (char) (c - baseChar);
			if (idx < size) {
				return children[idx];
			}
			return defaultTransition;
		}

		@Override
		public boolean isEmpty() {
			return size == 0;
		}

		@Override
		public void mapEntries(EntryVisitor visitor) {
			if (children != null) {
				for (int i = 0; i < children.length; i++) {
					if (children[i] != null && children[i] != this) {
						visitor.visit(this, (char) (baseChar + i), children[i]);
					}
				}
			}
		}

	}

	// Basic node for both
	private static abstract class TrieNode {

		protected TrieNode defaultTransition = null;
		protected TrieNode failTransition;
		protected Match match;

		protected TrieNode(boolean root) {
			this.defaultTransition = root ? this : null;
		}

		// Get fail transition
		public final TrieNode getFailTransition() {
			return failTransition;
		}

		// Get linked list of outputs at this node. Used in building the tree.
		public final Match getMatch() {
			return match;
		}

		// Get transition (root node returns something non-null for all characters - itself)
		public abstract TrieNode getTransition(char c);

		public abstract boolean isEmpty();

		public abstract void mapEntries(final EntryVisitor visitor);

		// Report matches at this node. Use at matching.
		public final boolean output(MatchListener listener, int idx) {
			// since idx is the last character in the match
			// position it past the match (to be consistent with conventions)
			Match k = match;
			boolean ret = true;
			while (k != null && ret) {
				ret = listener.match(k.word, idx);
				k = k.subMatch;
			}
			return ret;
		}
	}
}
