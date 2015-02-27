package net.rlenar.ahocorasick;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public class StringSet {

	TrieNode root;

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

		// construct breath-first flat representation of the tree
		final List<Edge> edgeQueue = flatten();

		// Calculate fail transitions and output sets.
		for (final Edge e : edgeQueue) {
			((HashmapNode) e.to).init(e.from, e.c);
		}
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

	private List<Edge> flatten() {

		final List<Edge> edgeQueue = new ArrayList<Edge>();

		edgeQueue.add(new Edge(null, root, (char) 0));
		for (int i = 0; i < edgeQueue.size(); i++) {
			final Iterator<Edge> e = ((HashmapNode) edgeQueue.get(i).to).getEdges();
			while (e.hasNext()) {
				edgeQueue.add(e.next());
			}
		}
		return edgeQueue;
	}

	static class HashmapNode extends TrieNode {

		private static final char EMPTY = 0xfffe;

		TrieNode failTransition;
		Keyword output;
		private TrieNode def = null;
		private char[] keys = new char[1];
		private int mask = keys.length - 1;
		private TrieNode[] nodes = new TrieNode[1];
		private int size = 0;

		protected HashmapNode(boolean root) {
			Arrays.fill(keys, EMPTY);
			this.def = root ? this : null;
		}

		@Override
		public <T> Iterator<T> forEach(final EntryVisitor<T> visitor) {
			return new Iterator<T>() {

				private T bank = null;
				private int i = -1;
				private Boolean next = null;

				public boolean hasNext() {
					if (next == null) {
						while (++i != keys.length) {
							if (nodes[i] != null) {
								bank = visitor.visit(keys[i], nodes[i]);
								next = Boolean.TRUE;
								return next;
							}
						}
						next = Boolean.FALSE;
					}
					return next;
				}

				public T next() {
					if (hasNext()) {
						next = null;
						return bank;
					}
					throw new NoSuchElementException();
				}

				public void remove() {
					throw new IllegalArgumentException();
				}
			};
		}

		public TrieNode get(char c) {
			int slot = hash(c) & mask;
			int currentSlot = slot;
			do {
				char keyInSlot = keys[currentSlot];
				if (keyInSlot == EMPTY) {
					return null;
				} else if (keyInSlot == c) {
					return nodes[currentSlot];
				} else {
					currentSlot = ++currentSlot & mask;
				}
			} while (currentSlot != slot);
			return null;
		}

		// Get edges leading out of the node. Root node doesn't
		// return default transitions.
		public Iterator<Edge> getEdges() {
			// Iterator that simply supplies edge objects from hashmap entries.
			return forEach(new EntryVisitor<Edge>() {

				public Edge visit(char key, TrieNode value) {
					return new Edge(HashmapNode.this, value, key);
				}

			});
		}

		@Override
		public TrieNode getTransition(final char c) {
			int slot = hash(c) & mask;
			int currentSlot = slot;
			do {
				char keyInSlot = keys[currentSlot];
				if (keyInSlot == EMPTY) {
					return def;
				} else if (keyInSlot == c) {
					return nodes[currentSlot];
				} else {
					currentSlot = ++currentSlot & mask;
				}
			} while (currentSlot != slot);
			return def;
		}

		// this function is called in breadth-first fashion
		public void init(final TrieNode parent, final char c) {
			if (parent == null) {
				return;
			}
			TrieNode failParent = parent.getFailTransition();
			//
			if (failParent == null) {
				// first level nodes have one possible fail transition, which is
				// root because the only possible suffix to a one character
				// string is an empty string
				failTransition = parent;
			} else {
				do {
					final TrieNode matchContinuation = failParent.getTransition(c);
					if (matchContinuation != null) {
						failTransition = matchContinuation;
					} else {
						failParent = failParent.getFailTransition();
					}
				} while (failTransition == null);
				if (output == null) {
					output = ((HashmapNode) failTransition).getOutput();
				} else {
					output.alsoContains = ((HashmapNode) failTransition).getOutput();
				}
			}
		}

		public TrieNode put(char c, TrieNode value) {
			if (keys.length < 0x10000 && ((size + 1 > keys.length) || (size > 16 && (size + 1 > keys.length * 0.90f)))) {
				enlarge();
			}
			++size;
			int slot = hash(c) & mask;
			for (int i = slot; i < keys.length; i++) {
				if (keys[i] == EMPTY) {
					keys[i] = c;
					nodes[i] = value;
					return null;
				} else if (keys[i] == c) {
					keys[i] = c;
					TrieNode ret = nodes[i];
					nodes[i] = value;
					return ret;
				}
			}
			for (int i = 0; i < slot; i++) {
				if (keys[i] == EMPTY) {
					keys[i] = c;
					nodes[i] = value;
					return null;
				} else if (keys[i] == c) {
					TrieNode ret = nodes[i];
					nodes[i] = value;
					return ret;
				}
			}
			throw new IllegalStateException();
		}

		private void enlarge() {
			char[] oldKeysArray = keys;
			TrieNode[] oldNodesArray = nodes;
			keys = new char[oldKeysArray.length * 2];
			mask = keys.length - 1;
			Arrays.fill(keys, EMPTY);
			nodes = new TrieNode[oldNodesArray.length * 2];
			size = 0;
			for (int i = 0; i < oldKeysArray.length; i++) {
				if (oldKeysArray[i] != EMPTY) {
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

	private static final class Edge {
		final char c;
		final TrieNode from;
		final TrieNode to;

		Edge(final TrieNode from, final TrieNode to, final char c) {
			this.c = c;
			this.from = from;
			this.to = to;
		}
	}

}
