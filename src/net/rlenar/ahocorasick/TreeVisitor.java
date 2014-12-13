package net.rlenar.ahocorasick;

import net.rlenar.ahocorasick.StringSet.HashmapNode;
import net.rlenar.ahocorasick.StringSet.HashmapRootNode;

interface TreeVisitor<T> {
	T visit(HashmapNode node);

	T visit(HashmapRootNode node);

	T visit(TrieNode node);
}
