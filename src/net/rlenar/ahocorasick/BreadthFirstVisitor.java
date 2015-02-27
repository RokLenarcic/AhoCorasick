package net.rlenar.ahocorasick;

import java.util.ArrayList;
import java.util.List;

public class BreadthFirstVisitor {

	private EntryVisitor visitor;

	public BreadthFirstVisitor(final EntryVisitor visitor) {
		this.visitor = visitor;
	}

	public TrieNode visitAll(TrieNode root) {
		final List<TrieNode> queue = new ArrayList<TrieNode>();
		TrieNode ret = visitor.visit(null, '\ufffe', root);
		if (ret == null) {
			ret = root;
		}
		EntryVisitor queueingVisitor = new EntryVisitor() {

			public TrieNode visit(TrieNode parent, char key, TrieNode value) {
				TrieNode ret = visitor.visit(parent, key, value);
				if (ret != null) {
					queue.add(ret);
				} else {
					queue.add(value);
				}
				return ret;
			}
		};
		ret.mapEntries(queueingVisitor);
		for (int i = 0; i < queue.size(); i++) {
			queue.get(i).mapEntries(queueingVisitor);
		}
		return ret;
	}
}
