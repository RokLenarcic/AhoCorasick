package net.rlenar.ahocorasick;

import net.rlenar.ahocorasick.StringSet.HashmapNode;

interface TreeVisitor<T> {
	T visit(HashmapNode node);

	T visit(TrieNode node);
}
