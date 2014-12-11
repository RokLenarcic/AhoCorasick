package net.rlenar.ahocorasick;

import java.util.List;

interface TrieNode {

	<T> T accept(TreeVisitor<T> visitor);

	<T> List<T> acceptRecursively(final TreeVisitor<T> visitor);

	// Get fail transition
	TrieNode getFailTransition();

	TrieNode getTransition(char c);

	// Report matches at this node. Use at matching.
	boolean output(MatchListener listener, int idx);

}