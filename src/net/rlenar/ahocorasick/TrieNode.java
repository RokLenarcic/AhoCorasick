package net.rlenar.ahocorasick;

import java.util.Iterator;

interface TrieNode {

	<T> Iterator<T> forEach(final EntryVisitor<T> visitor);

	// Get fail transition
	TrieNode getFailTransition();

	TrieNode getTransition(char c);

	// Report matches at this node. Use at matching.
	boolean output(MatchListener listener, int idx);
}