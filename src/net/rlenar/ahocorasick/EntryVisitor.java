package net.rlenar.ahocorasick;

public interface EntryVisitor {
	TrieNode visit(TrieNode parent, char key, TrieNode value);
}