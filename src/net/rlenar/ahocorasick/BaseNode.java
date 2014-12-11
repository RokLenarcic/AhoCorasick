package net.rlenar.ahocorasick;

class BaseNode {
	protected TrieNode failTransition;

	protected BaseNode(final TrieNode failTransition) {
		this.failTransition = failTransition;
	}
}
