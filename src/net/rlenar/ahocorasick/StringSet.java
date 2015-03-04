package net.rlenar.ahocorasick;

import java.util.Arrays;
import java.util.LinkedList;

class StringSet {

	private TrieNode root;

	public StringSet(final Iterable<String> keywords) {
		root = new HashmapNode(true);
		// Add all keywords
		for (final String keyword : keywords) {
			if (keyword != null && keyword.length() > 0) {
				HashmapNode currentNode = (HashmapNode) root;
				for (int idx = 0; idx < keyword.length(); idx++) {
					currentNode = currentNode.getOrAddChild(keyword.charAt(idx));
				}
				// index is past the keyword length
				// this node is the last node in a keyword
				// store the keyword as an output
				// the parameter is offset from the last character
				// to the first
				currentNode.match = new Match(keyword);
			}
		}
		root = visitAll(new EntryVisitor() {
			public TrieNode visit(TrieNode parent, char key, TrieNode value) {
				return optimizeNode(value);
			}
		});

		// Calculate fail transitions and output sets.
		root = visitAll(new EntryVisitor() {

			public TrieNode visit(TrieNode parent, char key, TrieNode value) {
				if (parent != null) {
					TrieNode failParent = parent.getFailTransition();
					if (failParent == null) {
						// first level nodes have one possible fail transition, which is
						// root because the only possible suffix to a one character
						// string is an empty string
						value.failTransition = parent;
					} else {
						do {
							final TrieNode matchContinuation = failParent.getTransition(key);
							if (matchContinuation != null) {
								value.failTransition = matchContinuation;
							} else {
								failParent = failParent.getFailTransition();
							}
						} while (value.failTransition == null);
						if (value.match == null) {
							value.match = value.failTransition.getMatch();
						} else {
							value.match.subMatch = value.failTransition.getMatch();
						}
					}
				}
				return null;
			}

		});
	}

	public void match(final String haystack, final MatchListener listener) {

		// Start with the root node.
		TrieNode currentNode = root;

		int idx = 0;
		// For each character.
		final int len = haystack.length();
		while (idx < len) {
			final char c = haystack.charAt(idx);
			// Try to transition from the current node using the character
			TrieNode nextNode = currentNode.getTransition(c);

			// If cannot transition, follow the fail transition until finding
			// node X where you can transition to another node Y using this
			// character. Take the transition.
			while (nextNode == null) {
				// Transition follow one fail transition
				currentNode = currentNode.getFailTransition();
				// See if you can transition to another node with this
				// character.
				nextNode = currentNode.getTransition(c);
			}
			// Take the transition.
			currentNode = nextNode;
			// Output any keyword on the current node and increase the index
			if (!currentNode.output(listener, ++idx)) {
				break;
			}
		}
	}

	private final TrieNode optimizeNode(TrieNode n) {
		if (n instanceof HashmapNode) {
			HashmapNode node = (HashmapNode) n;
			char minKey = '\uffff';
			char maxKey = 0;
			int size = node.numEntries;
			for (int i = 0; i < node.children.length; i++) {
				if (node.children[i] != null) {
					if (node.keys[i] > maxKey) {
						maxKey = node.keys[i];
					}
					if (node.keys[i] < minKey) {
						minKey = node.keys[i];
					}
				}
			}
			int keyIntervalSize = maxKey - minKey + 1;
			if (keyIntervalSize <= 8 || (size > (keyIntervalSize) * 0.70)) {
				return new RangeNode(node, minKey, maxKey);
			}
		}
		return n;
	}

	private TrieNode visitAll(final EntryVisitor visitor) {
		final LinkedList<TrieNode> queue = new LinkedList<TrieNode>();
		TrieNode ret = visitor.visit(null, '\ufffe', root);
		if (ret == null) {
			ret = root;
		}
		EntryVisitor queueingVisitor = new EntryVisitor() {

			public TrieNode visit(TrieNode parent, char key, TrieNode value) {
				TrieNode ret = visitor.visit(parent, key, value);
				if (ret != null) {
					queue.add(ret);
				} else {
					queue.add(value);
				}
				return ret;
			}
		};
		ret.mapEntries(queueingVisitor);
		while (!queue.isEmpty()) {
			queue.poll().mapEntries(queueingVisitor);
		}
		return ret;
	}

	private interface EntryVisitor {
		TrieNode visit(TrieNode parent, char key, TrieNode value);
	}

	private final static class HashmapNode extends TrieNode {

		private TrieNode[] children = new TrieNode[1];
		private char[] keys = new char[1];
		private int modulusMask = keys.length - 1;
		private int numEntries = 0;

		protected HashmapNode(boolean root) {
			super(root);
		}

		@Override
		public TrieNode getTransition(final char key) {
			int defaultSlot = hash(key) & modulusMask;
			int currentSlot = defaultSlot;
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
		public void mapEntries(EntryVisitor visitor) {
			for (int i = 0; i < keys.length; i++) {
				if (children[i] != null) {
					TrieNode ret = visitor.visit(this, keys[i], children[i]);
					if (ret != null) {
						children[i] = ret;
					}
				}
			}
		}

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

		private HashmapNode getOrAddChild(char key) {
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

		private int hash(char c) {
			// HASH_BASIS = 0x811c9dc5;
			final int HASH_PRIME = 16777619;
			return (((0x811c9dc5 ^ (c >> 8)) * HASH_PRIME) ^ (c & 0xff)) * HASH_PRIME;
		}

	}

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
			if (size > 0) {
				this.children = new TrieNode[size];
				if (oldNode.defaultTransition != null) {
					Arrays.fill(children, this);
				}
				for (int i = 0; i < oldNode.children.length; i++) {
					if (oldNode.children[i] != null) {
						children[oldNode.keys[i] - from] = oldNode.children[i];
					}
				}
			}
		}

		@Override
		public TrieNode getTransition(char c) {
			int idx = (char) (c - baseChar);
			if (idx < size) {
				return children[idx];
			}
			return defaultTransition;
		}

		@Override
		public void mapEntries(EntryVisitor visitor) {
			if (children != null) {
				for (int i = 0; i < children.length; i++) {
					if (children[i] != null && children[i] != this) {
						TrieNode ret = visitor.visit(this, (char) (baseChar + i), children[i]);
						if (ret != null) {
							children[i] = ret;
						}
					}
				}
			}
		}

	}

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
