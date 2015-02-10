package net.rlenar.ahocorasick;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class StringSet {

	TrieNode root = new HashmapNode();

	public StringSet(final Iterable<String> keywords) {
		// Add all keywords
		for (final String keyword : keywords) {
			if (keyword != null && keyword.length() > 0) {
				root.accept(new Adder(keyword));
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
		TrieNode currentNode = null;

		int idx = -1;
		// For each character.
		final int len = haystack.length();

		OUTER: while (++idx < len) {
			final char c = haystack.charAt(idx);
			// If we're on a root node, just transition and roll around.
			if (currentNode == null) {
				currentNode = root.getTransition(c);
			} else {
				// Output any keyword on the current node
				if (!currentNode.output(listener, idx)) {
					return;
				}

				// Try to transition from the current node using the character
				TrieNode nextNode = currentNode.getTransition(c);

				// If cannot transition, follow the fail transition until finding node X where you can
				// transition to another node Y using this character. Take the transition.
				while (nextNode == null) {
					// Transition follow one fail transition
					currentNode = currentNode.getFailTransition();
					if (currentNode == null) {
						continue OUTER;
					}
					// See if you can transition to another node with this character.
					nextNode = currentNode.getTransition(c);
				}
				// Take the transition.
				currentNode = nextNode;
			}
		}

		// Output any keyword on the current node
		if (currentNode != null) {
			currentNode.output(listener, len);
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

	static class HashmapNode implements TrieNode {

		TrieNode failTransition;
		Keyword output;
		final Map<Character, TrieNode> transitions;

		private HashmapNode() {
			transitions = new HashMap<Character, TrieNode>();
		}

		public <T> T accept(final TreeVisitor<T> visitor) {
			return visitor.visit(this);
		}

		public <T> List<T> acceptRecursively(final TreeVisitor<T> visitor) {
			final List<T> ret = new ArrayList<T>();
			ret.add(visitor.visit(this));
			for (final Map.Entry<Character, TrieNode> n : transitions.entrySet()) {
				ret.addAll(n.getValue().acceptRecursively(visitor));
			}
			return ret;
		}

		// Get edges leading out of the node. Root node doesn't
		// return default transitions.
		public Iterator<Edge> getEdges() {
			// Iterator that simply supplies edge objects from hashmap entries.
			return new Iterator<Edge>() {

				private final Iterator<Map.Entry<Character, TrieNode>> iter = transitions.entrySet().iterator();

				public boolean hasNext() {
					return iter.hasNext();
				}

				public Edge next() {
					final Map.Entry<Character, TrieNode> entry = iter.next();
					return new Edge(HashmapNode.this, entry.getValue(), entry.getKey());
				}

				public void remove() {
				}
			};
		}

		// Get fail transition
		public TrieNode getFailTransition() {
			return failTransition;
		}

		// Get linked list of outputs at this node. Used in building the tree.
		public Keyword getOutput() {
			return output;
		}

		public TrieNode getTransition(final char c) {
			return transitions.get(c);
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
						if (failParent == null) {
							return;
						}
					}
				} while (failTransition == null);
				if (output == null) {
					output = ((HashmapNode) failTransition).getOutput();
				} else {
					output.alsoContains = ((HashmapNode) failTransition).getOutput();
				}
			}
		}

		// Report matches at this node. Use at matching.
		public boolean output(final MatchListener listener, final int idx) {
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

	private static class Adder implements TreeVisitor<Void> {

		private int idx = 0;
		private final String keyword;

		public Adder(final String keyword) {
			super();
			this.keyword = keyword;
		}

		public Void visit(final HashmapNode node) {
			if (keyword.length() > idx) {
				// recursively travel the transitions, creating nodes
				// as needed
				TrieNode t = node.transitions.get(keyword.charAt(idx));
				if (t == null) {
					t = new HashmapNode();
					node.transitions.put(keyword.charAt(idx), t);
				}
				idx++;
				t.accept(this);
			} else {
				// index is past the keyword length
				// this node is the last node in a keyword
				// store the keyword as an output
				// the parameter is offset from the last character
				// to the first
				node.output = new Keyword(keyword);
			}
			return null;
		}

		public Void visit(final TrieNode node) {
			throw new UnsupportedOperationException();
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
