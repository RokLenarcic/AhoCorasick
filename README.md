Java Aho-Corasick implementation
===========

Fast multi-string search. Time complexity is `O(n+m)` where `n` is length of the input stream, and `m` is the number of matches. 
This particular implementation uses specialized trie nodes rather than general HashMap-based trie nodes which makes it much faster.

Dense nodes use an array based approach, while sparse nodes use a hashmap with character keys, open-addressing, power of 2 sizing and FNV-1a hash (this map was the fastest among many alternatives).
