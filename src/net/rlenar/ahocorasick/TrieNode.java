package net.rlenar.ahocorasick;

import java.util.Iterator;

public abstract class TrieNode {

	protected TrieNode failTransition;
	protected Keyword output;

	public abstract <T> Iterator<T> forEach(final EntryVisitor<T> visitor);

	// Get fail transition
	public final TrieNode getFailTransition() {
		return failTransition;
	}

	// Get linked list of outputs at this node. Used in building the tree.
	public final Keyword getOutput() {
		return output;
	}

	// Get fail transition
	public abstract TrieNode getTransition(char c);

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