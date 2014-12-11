package net.rlenar.ahocorasick;

import java.util.HashMap;
import java.util.Map;

import net.rlenar.ahocorasick.StringSet.HashmapNode;
import net.rlenar.ahocorasick.StringSet.HashmapRootNode;

abstract class Swapper implements TreeVisitor<TrieNode> {

	protected Map<TrieNode, TrieNode> swaps = new HashMap<TrieNode, TrieNode>();

	public abstract TrieNode visit(final HashmapNode node);

	public abstract TrieNode visit(final HashmapRootNode node);

	public final TrieNode visit(final LeafNode node) {
		return null;
	}

	public final TrieNode visit(final TrieNode node) {
		return null;
	}

}
