Java Aho-Corasick implementation
===========

This library provides tools to search for a large set of strings in a given string. This is accomplished by Aho-Corasick algorithm and other variants of trie searches for special cases.

These particular implementations use specialized trie nodes rather than general HashMap-based trie nodes which makes them much faster.

Dense nodes use an array based approach, while sparse nodes use a hashmap with character keys, open-addressing, power of 2 sizing and FNV-1a hash (this map was the fastest among many alternatives tried).


### AhoCorasickSet

Time complexity is `O(n+m)` where `n` is length of the input stream, and `m` is the number of matches. It matches all occurences of all strings. For example:

For input string of `aaaa` and a set of `a, aa, aaa, aaaa`, string `a` will match 4 times, string `aa` will match 3 times, string `aaa` will match twice and `aaaa` will match once.
