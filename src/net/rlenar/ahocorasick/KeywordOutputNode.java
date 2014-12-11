package net.rlenar.ahocorasick;

class KeywordOutputNode extends BaseNode {

	private final Keyword output;

	protected KeywordOutputNode(final TrieNode failTransition, final Keyword output) {
		super(failTransition);
		this.output = output;
	}

}
