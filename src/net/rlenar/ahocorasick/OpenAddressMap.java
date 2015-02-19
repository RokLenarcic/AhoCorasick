package net.rlenar.ahocorasick;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class OpenAddressMap {
	public static long access = 0;
	public static long capacityAll = 0;
	public static long gets = 0;
	public static boolean record = false;
	public static long sizeAll = 0;
	private static final char EMPTY = 0xfffe;
	private static final int HASH_BASIS = 0x811c9dc5;
	private static final int HASH_PRIME = 16777619;
	private TrieNode def = null;
	private char[] keys = new char[2];
	private TrieNode[] nodes = new TrieNode[2];
	private int size = 0;
	private float threshold = 0.75f;

	public OpenAddressMap(TrieNode def) {
		Arrays.fill(keys, EMPTY);
		this.def = def;
	}

	public <T> Iterator<T> forEach(final EntryVisitor<T> visitor) {
		capacityAll += keys.length;
		sizeAll += size;
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
		int slot = hash(c) & (keys.length - 1);
		if (keys[slot] == EMPTY) {
			return null;
		} else if (keys[slot] == c) {
			return nodes[slot];
		} else {
			for (int i = slot + 1; i < keys.length; i++) {
				if (keys[i] == EMPTY) {
					return null;
				} else if (keys[i] == c) {
					return nodes[i];
				}
			}
			for (int i = 0; i < slot; i++) {
				if (keys[i] == EMPTY) {
					return null;
				} else if (keys[i] == c) {
					return nodes[i];
				}
			}
			throw new IllegalStateException();
		}
	}

	public TrieNode getOrDefault(char c) {
		if (record) {
			gets++;
		}
		int slot = hash(c) & (keys.length - 1);
		if (keys[slot] == EMPTY) {
			access++;
			return def;
		} else if (keys[slot] == c) {
			access++;
			return nodes[slot];
		} else {
			for (int i = slot + 1; i < keys.length; i++) {
				access++;
				if (keys[i] == EMPTY) {
					return def;
				} else if (keys[i] == c) {
					return nodes[i];
				}
			}
			for (int i = 0; i < slot; i++) {
				access++;
				if (keys[i] == EMPTY) {
					return def;
				} else if (keys[i] == c) {
					return nodes[i];
				}
			}
			throw new IllegalStateException();
		}
	}

	public TrieNode put(char c, TrieNode value) {
		if (++size > keys.length * threshold) {
			enlarge();
			++size;
		}
		int slot = hash(c) & (keys.length - 1);
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
