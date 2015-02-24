package net.rlenar.ahocorasick;

public interface EntryVisitor<T> {
	T visit(char key, TrieNode value);
}