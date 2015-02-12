package net.rlenar.ahocorasick;

import java.util.HashMap;
import java.util.Map;

import net.rlenar.ahocorasick.StringSet.HashmapNode;

abstract class Swapper implements TreeVisitor<TrieNode> {

	protected Map<TrieNode, TrieNode> swaps = new HashMap<TrieNode, TrieNode>();

	public abstract TrieNode visit(final HashmapNode node);

	public final TrieNode visit(final TrieNode node) {
		return null;
	}

}
