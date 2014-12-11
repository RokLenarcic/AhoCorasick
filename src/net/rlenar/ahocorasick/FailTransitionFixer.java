package net.rlenar.ahocorasick;

import java.util.Map;

import net.rlenar.ahocorasick.StringSet.HashmapNode;
import net.rlenar.ahocorasick.StringSet.HashmapRootNode;

/**
 * Takes a map of nodes that were swapped and fixes all failTransitions to new
 * nodes.
 *
 * @author rok
 *
 */
class FailTransitionFixer implements TreeVisitor<Void> {

	private final Map<TrieNode, TrieNode> swaps;

	public FailTransitionFixer(final Map<TrieNode, TrieNode> swaps) {
		this.swaps = swaps;
	}

	public Void visit(final HashmapNode node) {
		final TrieNode swap = swaps.get(node.failTransition);
		if (swap != null) {
			node.failTransition = swap;
		}
		return null;
	}

	public Void visit(final HashmapRootNode node) {
		return visit((HashmapNode) node);
	}

	public Void visit(final LeafNode node) {
		final TrieNode swap = swaps.get(node.failTransition);
		if (swap != null) {
			node.failTransition = swap;
		}
		return null;
	}

	public Void visit(final TrieNode node) {
		return null;
	}

}
