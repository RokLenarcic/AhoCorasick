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
				HashmapNode node = (HashmapNode) root;
				for (int idx = 0; idx < keyword.length(); idx++) {
					HashmapNode t = (HashmapNode) node.get(keyword.charAt(idx));
					if (t == null) {
						t = new HashmapNode(false);
						node.put(keyword.charAt(idx), t);
					}
					node = t;
				}
				// index is past the keyword length
				// this node is the last node in a keyword
				// store the keyword as an output
				// the parameter is offset from the last character
				// to the first
				node.output = new Keyword(keyword);
			}
		}
		root = visitAll(new EntryVisitor() {
			public TrieNode visit(TrieNode parent, char key, TrieNode value) {
				return RangeNode.optimizeNode(value);
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
						if (value.output == null) {
							value.output = value.failTransition.getOutput();
						} else {
							value.output.alsoContains = value.failTransition.getOutput();
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

	private static class HashmapNode extends TrieNode {

		private char[] keys = new char[1];
		private int mask = keys.length - 1;
		private TrieNode[] nodes = new TrieNode[1];
		private int size = 0;

		protected HashmapNode(boolean root) {
			super(root);
		}

		public TrieNode get(char c) {
			int slot = hash(c) & mask;
			int currentSlot = slot;
			do {
				TrieNode nodeInSlot = nodes[currentSlot];
				if (nodeInSlot == null) {
					return null;
				} else if (keys[currentSlot] == c) {
					return nodeInSlot;
				} else {
					currentSlot = ++currentSlot & mask;
				}
			} while (currentSlot != slot);
			return null;
		}

		@Override
		public TrieNode getTransition(final char c) {
			int slot = hash(c) & mask;
			int currentSlot = slot;
			do {
				if (keys[currentSlot] == c) {
					return nodes[currentSlot];
				} else if (nodes[currentSlot] == null) {
					return defaultTransition;
				} else {
					currentSlot = ++currentSlot & mask;
				}
			} while (currentSlot != slot);
			return defaultTransition;
		}

		@Override
		public void mapEntries(EntryVisitor visitor) {
			for (int i = 0; i < keys.length; i++) {
				if (nodes[i] != null) {
					TrieNode ret = visitor.visit(this, keys[i], nodes[i]);
					if (ret != null) {
						nodes[i] = ret;
					}
				}
			}
		}

		public TrieNode put(char c, TrieNode value) {
			if (keys.length < 0x10000 && ((size + 1 > keys.length) || (size > 16 && (size + 1 > keys.length * 0.90f)))) {
				enlarge();
			}
			++size;
			int slot = hash(c) & mask;
			int currentSlot = slot;
			do {
				if (nodes[currentSlot] == null || keys[currentSlot] == c) {
					keys[currentSlot] = c;
					TrieNode ret = nodes[currentSlot];
					nodes[currentSlot] = value;
					return ret;
				} else {
					currentSlot = ++currentSlot & mask;
				}
			} while (currentSlot != slot);
			throw new IllegalStateException();
		}

		private void enlarge() {
			char[] oldKeysArray = keys;
			TrieNode[] oldNodesArray = nodes;
			keys = new char[oldKeysArray.length * 2];
			mask = keys.length - 1;
			nodes = new TrieNode[oldNodesArray.length * 2];
			size = 0;
			for (int i = 0; i < oldKeysArray.length; i++) {
				if (oldNodesArray[i] != null) {
					this.put(oldKeysArray[i], oldNodesArray[i]);
				}
			}
		}

		private int hash(char c) {
			// HASH_BASIS = 0x811c9dc5;
			final int HASH_PRIME = 16777619;
			return (((0x811c9dc5 ^ (c >> 8)) * HASH_PRIME) ^ (c & 0xff)) * HASH_PRIME;
		}

	}

	private static final class RangeNode extends TrieNode {

		private static TrieNode optimizeNode(TrieNode n) {
			if (n instanceof HashmapNode) {
				HashmapNode node = (HashmapNode) n;
				char min = '\uffff';
				char max = 0;
				int size = node.size;
				for (int i = 0; i < node.nodes.length; i++) {
					if (node.nodes[i] != null) {
						if (node.keys[i] > max) {
							max = node.keys[i];
						}
						if (node.keys[i] < min) {
							min = node.keys[i];
						}
					}
				}
				int intervalSize = max - min + 1;
				if (intervalSize <= 8 || (size > (intervalSize) * 0.70)) {
					return new RangeNode(node, min, intervalSize);
				}
			}
			return n;
		}

		private TrieNode[] children;
		private int from;
		private int size;

		private RangeNode(HashmapNode n, char from, int size) {
			super(n.defaultTransition != null);
			this.from = from;
			this.size = size;
			this.output = n.output;
			if (size != 0) {
				this.children = new TrieNode[size];
				if (n.defaultTransition != null) {
					Arrays.fill(children, this);
				}
				for (int i = 0; i < n.nodes.length; i++) {
					if (n.nodes[i] != null) {
						children[n.keys[i] - from] = n.nodes[i];
					}
				}
			}
		}

		@Override
		public TrieNode getTransition(char c) {
			int idx = c - from;
			if (idx >= 0 && idx < size) {
				return children[idx];
			}
			return defaultTransition;
		}

		@Override
		public void mapEntries(EntryVisitor visitor) {
			for (int i = 0; i < size; i++) {
				if (children[i] != null && children[i] != this) {
					TrieNode ret = visitor.visit(this, (char) (from + i), children[i]);
					if (ret != null) {
						children[i] = ret;
					}
				}
			}

		}

	}

	private static abstract class TrieNode {

		protected TrieNode defaultTransition = null;
		protected TrieNode failTransition;
		protected Keyword output;

		protected TrieNode(boolean root) {
			this.defaultTransition = root ? this : null;
		}

		// Get fail transition
		public final TrieNode getFailTransition() {
			return failTransition;
		}

		// Get linked list of outputs at this node. Used in building the tree.
		public final Keyword getOutput() {
			return output;
		}

		// Get transition (root node returns something non-null for all characters - itself)
		public abstract TrieNode getTransition(char c);

		public abstract void mapEntries(final EntryVisitor visitor);

		// Report matches at this node. Use at matching.
		public final boolean output(MatchListener listener, int idx) {
			// since idx is the last character in the match
			// position it past the match (to be consistent with conventions)
			Keyword k = output;
			boolean ret = true;
			while (k != null && ret) {
				ret = listener.match(k.word, idx);
				k = k.alsoContains;
			}
			return ret;
		}
	}
}
