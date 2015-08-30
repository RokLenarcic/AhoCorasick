Java Aho-Corasick implementation
===========

# Dependency

```xml
    <dependency>
        <groupId>com.github.roklenarcic</groupId>
        <artifactId>aho-corasick</artifactId>
        <version>1.1</version>
    </dependency>
```

This library provides tools to search for a large set of strings in a given string. This is accomplished by Aho-Corasick algorithm and other variants of trie searches for special cases.

These particular implementations use specialized trie nodes rather than general HashMap-based trie nodes which makes them much faster.

Dense nodes use an array based approach, while sparse nodes use a hashmap with character keys, open-addressing, power of 2 sizing and FNV-1a hash (this map was the fastest among many alternatives tried).

Time complexity is `O(n+m)` where `n` is length of the input stream, and `m` is the number of matches. 

### Upsides of this implementation

- It is Java 5 compatible (lots of people are still running this).
- It's very fast
- It has a low memory footprint
- Matching does no allocations, which means no GC churning
- It's highly adaptable with listener reporting. You can stop matching after the first match, or after you find whatever you need
- Keyword are fed to constructor via `Iterable` object, so you can save memory if you create an Iterable that feeds them directly from a file, without loading them into the memory
- It allows reading from a `Readable` object (from file, from socket) which allows processing of very large or infinite character streams.
- When matching whole words only, you can specify which characters are word characters

### Downsides of this implementation

- **Code is ugly with tons of duplicated code for performance reasons so it's a bad example of the algorithm for learning purposes**

### Example

```
   StringSet set = new AhoCorasickSet(dictionary, true);
   SetMatchListener listener = new SetMatchListener() {
      public boolean match(final String haystack, final int startPosition, final int endPosition) {
         System.out.println("Found " + haystack.substring(startPosition, endPosition) + " at position " + startPosition");
      }
   }
   set.match(haystack, listener);
```

### General interface

There are several different implementations tailored for different purposes. There are two general types, sets and maps.
Sets report matches of strings. Maps report matches of strings and the value attached to the keyword.
Matches are reported through a callback interface. 

##### Map and set constructors

Constructors have parameters:

`Iterable<String> dictorary`

`Iterable<? extends T> values` if a map

`boolean caseSensitive`

##### SetMatchListener callback:

`boolean match(final String haystack, final int startPosition, final int endPosition);`

Start position is the start position of the match and the end position is position of the character AFTER the match (same way String.substring works). So to get the match all you have to do is `haystack.substring(startPosition, endPosition)`.
The return value should be true to continue matching or false to stop matching.

**Performance note: Calling a substring for each match results in allocations and it can create garbage. You can avoid that by using a map, and simply use something like `AhoCorasickMap(dictionary, dictionary, true)`. It will consume a bit more memory, but you'll get the matched string as a value in the listener, see below. This is quite a bit faster, see performance chapter.**

##### MapMatchListener callback:

`boolean match(final String haystack, final int startPosition, final int endPosition, final T value);`

Same as set match listener but with additional parameter of a value for a match.

##### ReadableMatchListener callback:

`boolean match(final T value);`

This interface is for matching in map with a reader.

### AhoCorasickSet/Map

Standard Aho-Corasick algorithm. It matches all occurences of all strings, possibly overlapping. For example:

For input string of `aaaa` and a dictionary of `a, aa, aaa, aaaa`, string `a` will match 4 times, string `aa` will match 3 times, string `aaa` will match twice and `aaaa` will match once.

### LongestMatchSet/Map

Matches left-most longest non-overlapping occurences of keywords. 

For input string `a1b2c3d4` and a dictionary of `b, b2, 2c3d4`, only `b2` will match. `2c3d4` is longer but it overlaps with a match that starts earlier.

### ShortestMatchSet/Map

Matches left-most shortest non-overlapping occurences of keywords. 

For input string `a1b2c3d4` and a dictionary of `2, b2, 2c3d4`, only `b2` will match. `2` is shorter but it overlaps with a match that starts earlier. With a dictionary of `b, 2, b2`, both `b` and `2` will match.

### WholeWordMatchSet/Map

Matches only whole word matches, i.e. keywords surrounded by non-word characters or string boundaries.
It will trim all keywords of non-word characters and it will throw an `IllegalArgumentException` if any keyword contains a non-word characters.

For input string `late evening` and keywords `la, late, eve, evening` it will match `late` and `evening`.

Default word characters are all letters in the unicode, all numbers in the unicode and `_` and `-`.
Additional two constructors are provided to allow user to specify their own word characters.

User can provide a char array with word characters, in which case they are the word characters used. The other constructor takes a character array and a boolean array. The word characters used are the default ones modified by the ones provided and boolean flags signal where characters are turned on and off. This is useful when you just want to turn off a specific character in the set of default characters. For example:

`new WholeWordMatchSet(keywords, true, ['_', '='], [false, true])`

Will produce a set where letters and digits and `-` and `=` are considered word characters, but not `_`.

### WholeWordLongestMatchSet/Map

Same as above but it allows non-word characters in the keywords. This means there can be overlaps, in which case the leftmost longest match will be returned. E.g.:

For input string `as if` and dictionary `as if, as, if` it matches `as if`. For input string `ax if` it matches `if` and for input string `as of` it will match `as`.

### Performance

Comparing this implementation to `https://github.com/robert-bor/aho-corasick`'s `org.ahocorasick.trie.Trie` which, is one of the more popular java implementations on the github and has a short, clean implementation (good if you want to learn the algorithm).

Dictionary: OS X dictionary at `/usr/share/dict/words`, 235886 english words.
Input string: a paragraph of english text

#### org.ahocorasick.trie.Trie

- Memory size 187 MB
- Full matching: 62 µs
- Whole word matching: 150 µs
- Longest non-overlapping: 800 µs

#### com.roklenarcic.util.string.*Set/Map

Several variants, times listed correspond with this order:

1. A StringSet with an empty listener.
2. A StringSet with a listener that substrings matches from input strings and collects them in a list.
3. A StringMap with keywords as values and a listener that collects values in a list.

- Full matching: 3.6 µs, 18 µs, 9.2 µs, memory size 71 MB
- Whole word matching: 2.9 µs, 5 µs, 3.8 µs, memory size 52 MB
- Longest non-overlapping: 7.1 µs, 9.5 µs, 9.9 µs, memory size 74 MB

Comparing for the longest match is not directly possible since the `Trie` class returns longest leftmost match, while LongestMatchSet returns leftmost longest match. However leftmost longest match would be implemented by `Trie` by just changing its match sort order, so same performance applies.

### Memory/speed trade-offs

You can adjust memory consumption vs speed a bit via thresholder class. Explanation is found here:

https://github.com/RokLenarcic/AhoCorasick/wiki/Thresholding-and-memory-trade-offs

# License

This repository is licensed under LGPL v3 license found in the `LICENSE.md` or here:

`http://opensource.org/licenses/lgpl-3.0.html`
