package net.rlenar.ahocorasick;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class OpenAddressMap {
	private static final char EMPTY = 0xfffe;
	private static final int HASH_BASIS = 0x811c9dc5;
	private static final int HASH_PRIME = 16777619;
	private TrieNode def = null;
	private char[] keys = new char[1];
	private int mask = keys.length - 1;
	private TrieNode[] nodes = new TrieNode[1];
	private int size = 0;

	public OpenAddressMap(TrieNode def) {
		Arrays.fill(keys, EMPTY);
		this.def = def;
	}

	public <T> Iterator<T> forEach(final EntryVisitor<T> visitor) {
		return new Iterator<T>() {

			private T bank = null;
			private int i = -1;
			private Boolean next = null;

			public boolean hasNext() {
				if (next == null) {
					while (++i != keys.length) {
						if (nodes[i] != null) {
							bank = visitor.visit(keys[i], nodes[i]);
							next = Boolean.TRUE;
							return next;
						}
					}
					next = Boolean.FALSE;
				}
				return next;
			}

			public T next() {
				if (hasNext()) {
					next = null;
					return bank;
				}
				throw new NoSuchElementException();
			}

			public void remove() {
				throw new IllegalArgumentException();
			}
		};
	}

	public TrieNode get(char c) {
		int slot = hash(c) & mask;
		int currentSlot = slot;
		do {
			char keyInSlot = keys[currentSlot];
			if (keyInSlot == EMPTY) {
				return null;
			} else if (keyInSlot == c) {
				return nodes[currentSlot];
			} else {
				currentSlot = ++currentSlot & mask;
			}
		} while (currentSlot != slot);
		return null;
	}

	public TrieNode getOrDefault(char c) {
		int slot = hash(c) & mask;
		int currentSlot = slot;
		do {
			char keyInSlot = keys[currentSlot];
			if (keyInSlot == EMPTY) {
				return def;
			} else if (keyInSlot == c) {
				return nodes[currentSlot];
			} else {
				currentSlot = ++currentSlot & mask;
			}
		} while (currentSlot != slot);
		return def;
	}

	public TrieNode put(char c, TrieNode value) {
		if (keys.length < 0x10000 && ((size + 1 > keys.length) || (size > 16 && (size + 1 > keys.length * 0.90f)))) {
			enlarge();
		}
		++size;
		int slot = hash(c) & mask;
		for (int i = slot; i < keys.length; i++) {
			if (keys[i] == EMPTY) {
				keys[i] = c;
				nodes[i] = value;
				return null;
			} else if (keys[i] == c) {
				keys[i] = c;
				TrieNode ret = nodes[i];
				nodes[i] = value;
				return ret;
			}
		}
		for (int i = 0; i < slot; i++) {
			if (keys[i] == EMPTY) {
				keys[i] = c;
				nodes[i] = value;
				return null;
			} else if (keys[i] == c) {
				TrieNode ret = nodes[i];
				nodes[i] = value;
				return ret;
			}
		}
		throw new IllegalStateException();
	}

	private void enlarge() {
		char[] oldKeysArray = keys;
		TrieNode[] oldNodesArray = nodes;
		keys = new char[oldKeysArray.length * 2];
		mask = keys.length - 1;
		Arrays.fill(keys, EMPTY);
		nodes = new TrieNode[oldNodesArray.length * 2];
		size = 0;
		for (int i = 0; i < oldKeysArray.length; i++) {
			if (oldKeysArray[i] != EMPTY) {
				this.put(oldKeysArray[i], oldNodesArray[i]);
			}
		}
	}

	private int hash(char c) {
		return (((HASH_BASIS ^ (c >> 8)) * HASH_PRIME) ^ (c & 0xff)) * HASH_PRIME;
	}

	public interface EntryVisitor<T> {
		T visit(char key, TrieNode value);
	}
}
