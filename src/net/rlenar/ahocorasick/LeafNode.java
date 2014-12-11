package net.rlenar.ahocorasick;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import net.rlenar.ahocorasick.StringSet.HashmapNode;

final class LeafNode implements TrieNode {

	TrieNode failTransition;
	private final Keyword output;

	LeafNode(final TrieNode failTransition, final Keyword output) {
		this.failTransition = failTransition;
		this.output = output;
	}

	public <T> T accept(final TreeVisitor<T> visitor) {
		return visitor.visit(this);
	}

	public <T> List<T> acceptRecursively(final TreeVisitor<T> visitor) {
		return Collections.singletonList(visitor.visit(this));
	}

	public void fixFailTransitions(final Map<HashmapNode, TrieNode> swaps) {
		final TrieNode swap = swaps.get(failTransition);
		if (swap != null) {
			failTransition = swap;
		}
	}

	public TrieNode getFailTransition() {
		return failTransition;
	}

	public TrieNode getTransition(final char c) {
		return null;
	}

	public boolean output(final MatchListener listener, final int idx) {
		// since idx is the last character in the match
		// position it past the match (to be consistent with conventions)
		Keyword k = output;
		boolean ret = true;
		do {
			ret = listener.match(k.word, idx);
			k = k.alsoContains;
		} while (k != null && ret);
		return ret;
	}

}