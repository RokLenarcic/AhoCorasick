package com.roklenarcic.util.strings;

import java.util.Iterator;

// A set that matches only whole word matches. Non-word characters are user defined (with a default).
// Any non-word characters around input strings get trimmed. Non-word characters are allowed in the keywords.
class WholeWordLongestMatchMap<T> implements StringMap<T> {

    private boolean caseSensitive = true;
    private TrieNode<T> root;
    private boolean[] wordChars;

    // Set where digits and letters, '-' and '_' are considered word characters.
    public WholeWordLongestMatchMap(final Iterator<String> keywords, final Iterator<? extends T> values, boolean caseSensitive) {
        boolean[] characterFlags = new boolean[65536];
        characterFlags['-'] = true;
        characterFlags['_'] = true;
        for (int i = 0; i < characterFlags.length; i++) {
            if (Character.isLetterOrDigit((char) i)) {
                characterFlags[i] = true;
            }
        }
        init(keywords, values, caseSensitive, characterFlags);
    }

    // Set where the characters in the given array are considered word characters
    public WholeWordLongestMatchMap(final Iterator<String> keywords, final Iterator<? extends T> values, boolean caseSensitive, char[] wordCharacters) {
        boolean[] characterFlags = new boolean[65536];
        for (char c : wordCharacters) {
            characterFlags[c] = true;
        }
        init(keywords, values, caseSensitive, characterFlags);
    }

    // Set where digits and letters and '-' and '_' are considered word characters but modified by the two
    // given arrays
    public WholeWordLongestMatchMap(final Iterator<String> keywords, final Iterator<? extends T> values, boolean caseSensitive, char[] wordCharacters,
            boolean[] enableCharacterFlags) {
        boolean[] characterFlags = new boolean[65536];
        characterFlags['-'] = true;
        characterFlags['_'] = true;
        for (int i = 0; i < characterFlags.length; i++) {
            if (Character.isLetterOrDigit((char) i)) {
                characterFlags[i] = true;
            }
        }
        for (int i = 0; i < wordCharacters.length; i++) {
            characterFlags[wordCharacters[i]] = enableCharacterFlags[i];
        }
        init(keywords, values, caseSensitive, characterFlags);
    }

    public void match(final String haystack, final MapMatchListener<T> listener) {
        // Nodes contain fail matches, which is the last normal match up the tree before the current node
        // match.

        // Start with the root node.
        TrieNode<T> currentNode = root;

        int idx = 0;
        // For each character.
        final int len = haystack.length();
        // Putting this if into the loop worsens the performance so we'll sadly
        // have to deal with duplicated code.
        if (caseSensitive) {
            while (idx < len) {
                char c = haystack.charAt(idx);
                TrieNode<T> nextNode = currentNode.getTransition(c);
                // Regardless of the type of the character, we keep moving till we run into
                // a situation where there's no transition available.
                if (nextNode == null) {
                    // Awkward if structure saves us a branch in the else statement.
                    if (!wordChars[c]) {
                        // If we ran into no-transition scenario on non-word character we can
                        // output the match on the current node if there is one, else we output
                        // a fail match if there is one.
                        // Later we will run through non-word characters to the start of the next word.
                        if (currentNode.matchLength != 0) {
                            if (!listener.match(idx - currentNode.matchLength, idx, currentNode.value)) {
                                return;
                            }
                        } else if (currentNode.failMatchLength != 0) {
                            int failMatchEnd = idx - currentNode.failMatchOffset;
                            if (!listener.match(failMatchEnd - currentNode.failMatchLength, failMatchEnd, currentNode.failValue)) {
                                return;
                            }
                        }
                    } else {
                        // If we ran into no-transition situation on a word character, we output any
                        // fail match on the node and scroll through word characters to a non-word character.
                        if (currentNode.failMatchLength != 0) {
                            int failMatchEnd = idx - currentNode.failMatchOffset;
                            if (!listener.match(failMatchEnd - currentNode.failMatchLength, failMatchEnd, currentNode.failValue)) {
                                return;
                            }
                        }
                        // Scroll to the first non-word character
                        while (++idx < len && wordChars[haystack.charAt(idx)]) {
                            ;
                        }
                    }
                    // Scroll to the first word character
                    while (++idx < len && !wordChars[haystack.charAt(idx)]) {
                        ;
                    }
                    currentNode = root;
                } else {
                    // If we have transition just take it.
                    ++idx;
                    currentNode = nextNode;
                }
            }
            // Output any matches on the last node, either a normal match or fail match.
            if (currentNode.matchLength != 0) {
                if (!listener.match(idx - currentNode.matchLength, idx, currentNode.value)) {
                    return;
                }
            } else if (currentNode.failMatchLength != 0) {
                int failMatchEnd = idx - currentNode.failMatchOffset;
                if (!listener.match(failMatchEnd - currentNode.failMatchLength, failMatchEnd, currentNode.failValue)) {
                    return;
                }
            }
        } else {
            while (idx < len) {
                char c = Character.toLowerCase(haystack.charAt(idx));
                TrieNode<T> nextNode = currentNode.getTransition(c);
                // Regardless of the type of the character, we keep moving till we run into
                // a situation where there's no transition available.
                if (nextNode == null) {
                    // Awkward if structure saves us a branch in the else statement.
                    if (!wordChars[c]) {
                        // If we ran into no-transition scenario on non-word character we can
                        // output the match on the current node if there is one, else we output
                        // a fail match if there is one.
                        // Later we will run through non-word characters to the start of the next word.
                        if (currentNode.matchLength != 0) {
                            if (!listener.match(idx - currentNode.matchLength, idx, currentNode.value)) {
                                return;
                            }
                        } else if (currentNode.failMatchLength != 0) {
                            int failMatchEnd = idx - currentNode.failMatchOffset;
                            if (!listener.match(failMatchEnd - currentNode.failMatchLength, failMatchEnd, currentNode.failValue)) {
                                return;
                            }
                        }
                    } else {
                        // If we ran into no-transition situation on a word character, we output any
                        // fail match on the node and scroll through word characters to a non-word character.
                        if (currentNode.failMatchLength != 0) {
                            int failMatchEnd = idx - currentNode.failMatchOffset;
                            if (!listener.match(failMatchEnd - currentNode.failMatchLength, failMatchEnd, currentNode.failValue)) {
                                return;
                            }
                        }
                        // Scroll to the first non-word character
                        while (++idx < len && wordChars[haystack.charAt(idx)]) {
                            ;
                        }
                    }
                    // Scroll to the first word character
                    while (++idx < len && !wordChars[haystack.charAt(idx)]) {
                        ;
                    }
                    currentNode = root;
                } else {
                    // If we have transition just take it.
                    ++idx;
                    currentNode = nextNode;
                }
            }
            // Output any matches on the last node, either a normal match or fail match.
            if (currentNode.matchLength != 0) {
                if (!listener.match(idx - currentNode.matchLength, idx, currentNode.value)) {
                    return;
                }
            } else if (currentNode.failMatchLength != 0) {
                int failMatchEnd = idx - currentNode.failMatchOffset;
                if (!listener.match(failMatchEnd - currentNode.failMatchLength, failMatchEnd, currentNode.failValue)) {
                    return;
                }
            }
        }
    }

    boolean[] getWordChars() {
        return wordChars;
    }

    private void init(final Iterator<String> keywords, final Iterator<? extends T> values, boolean caseSensitive, final boolean[] wordChars) {
        this.wordChars = wordChars;
        // Create the root node
        root = new HashmapNode<T>();
        // Add all keywords
        while (keywords.hasNext() && values.hasNext()) {
            String keyword = keywords.next();
            T value = values.next();
            // Skip any empty keywords
            if (keyword != null) {
                // Trim any non-word chars from the start and the end.
                int wordStart = 0;
                int wordEnd = keyword.length();
                for (int i = 0; i < keyword.length(); i++) {
                    if (wordChars[keyword.charAt(i)]) {
                        wordStart = i;
                        break;
                    }
                }
                for (int i = keyword.length() - 1; i >= 0; i--) {
                    if (wordChars[keyword.charAt(i)]) {
                        wordEnd = i + 1;
                        break;
                    }
                }
                // Don't substring if you don't have to.
                if (wordStart != 0 || wordEnd != keyword.length()) {
                    keyword = keyword.substring(wordStart, wordEnd);
                }
                if (keyword.length() > 0) {
                    // Start with the current node and traverse the tree
                    // character by character. Add nodes as needed to
                    // fill out the tree.
                    HashmapNode<T> currentNode = (HashmapNode<T>) root;
                    for (int idx = 0; idx < keyword.length(); idx++) {
                        currentNode = currentNode.getOrAddChild(caseSensitive ? keyword.charAt(idx) : Character.toLowerCase(keyword.charAt(idx)));
                    }
                    // Last node will contains the keyword as a match.
                    // Suffix matches will be added later.
                    currentNode.matchLength = keyword.length();
                    currentNode.value = value;
                }
            }
        }
        // Go through nodes depth first, swap any hashmap nodes,
        // whose size is close to the size of range of keys with
        // flat array based nodes.
        root = optimizeNodes(root);
        // Fill the fail match variables. We do that by carrying the last match up the tree
        // and increasing the offset.
        root.mapEntries(new EntryVisitor<T>() {

            private T failMatch = null;
            private int failMatchLength = 0;
            private int failMatchOffset = 0;

            public void visit(TrieNode<T> parent, char key, TrieNode<T> value) {
                // We save the state so we can restore it later. It's a poor man's stack.
                int length = failMatchLength;
                int offset = failMatchOffset;
                T match = failMatch;
                // If the 'parent' node has a match and the transition is a non-word character
                // we carry that match as a fail match to children after that transition.
                if (parent.matchLength != 0 && !wordChars[key]) {
                    failMatchLength = parent.matchLength;
                    failMatchOffset = 1;
                    failMatch = parent.value;
                } else {
                    failMatchOffset++;
                }
                value.failMatchLength = failMatchLength;
                value.failMatchOffset = failMatchOffset;
                value.failValue = failMatch;
                value.mapEntries(this);
                // Reset the state before exiting.
                failMatchLength = length;
                failMatchOffset = offset;
                failMatch = match;

            }
        });
    }

    // A recursive function that replaces hashmap nodes with range nodes
    // when appropriate.
    private final TrieNode<T> optimizeNodes(TrieNode<T> n) {
        if (n instanceof HashmapNode) {
            HashmapNode<T> node = (HashmapNode<T>) n;
            char minKey = '\uffff';
            char maxKey = 0;
            // Find you the min and max key on the node.
            int size = node.numEntries;
            for (int i = 0; i < node.children.length; i++) {
                if (node.children[i] != null) {
                    node.children[i] = optimizeNodes(node.children[i]);
                    if (node.keys[i] > maxKey) {
                        maxKey = node.keys[i];
                    }
                    if (node.keys[i] < minKey) {
                        minKey = node.keys[i];
                    }
                }
            }
            // If difference between min and max key are small
            // or only slightly larger than number of entries, use a range node
            int keyIntervalSize = maxKey - minKey + 1;
            if (keyIntervalSize <= 8 || (size > (keyIntervalSize) * 0.70)) {
                return new RangeNode<T>(node, minKey, maxKey);
            }
        }
        return n;
    }

    private interface EntryVisitor<T> {
        void visit(TrieNode<T> parent, char key, TrieNode<T> value);
    }

    // An open addressing hashmap implementation with linear probing
    // and capacity of 2^n
    private final static class HashmapNode<T> extends TrieNode<T> {

        // Start with capacity of 1 and resize as needed.
        @SuppressWarnings("unchecked")
        private TrieNode<T>[] children = new TrieNode[1];

        private char[] keys = new char[1];
        // Since capacity is a power of 2, we calculate mod by just
        // bitwise AND with the right mask.
        private int modulusMask = keys.length - 1;
        private int numEntries = 0;

        @SuppressWarnings("unchecked")
        @Override
        public void clear() {
            children = new TrieNode[1];
            keys = new char[1];
            modulusMask = keys.length - 1;
            numEntries = 0;
        }

        @Override
        public TrieNode<T> getTransition(final char key) {
            int defaultSlot = hash(key) & modulusMask;
            int currentSlot = defaultSlot;
            // Linear probing to find the entry for key.
            do {
                if (keys[currentSlot] == key) {
                    return children[currentSlot];
                } else if (children[currentSlot] == null) {
                    return null;
                } else {
                    currentSlot = ++currentSlot & modulusMask;
                }
            } while (currentSlot != defaultSlot);
            return null;
        }

        @Override
        public boolean isEmpty() {
            return numEntries == 0;
        }

        @Override
        public void mapEntries(EntryVisitor<T> visitor) {
            for (int i = 0; i < keys.length; i++) {
                if (children[i] != null) {
                    visitor.visit(this, keys[i], children[i]);
                }
            }
        }

        // Double the capacity of the node, calculate the new mask,
        // rehash and reinsert the entries
        @SuppressWarnings("unchecked")
        private void enlarge() {
            char[] biggerKeys = new char[keys.length * 2];
            TrieNode<T>[] biggerChildren = new TrieNode[children.length * 2];
            int biggerMask = biggerKeys.length - 1;
            for (int i = 0; i < children.length; i++) {
                char key = keys[i];
                TrieNode<T> node = children[i];
                if (node != null) {
                    int defaultSlot = hash(key) & biggerMask;
                    int currentSlot = defaultSlot;
                    do {
                        if (biggerChildren[currentSlot] == null) {
                            biggerKeys[currentSlot] = key;
                            biggerChildren[currentSlot] = node;
                            break;
                        } else if (biggerKeys[currentSlot] == key) {
                            throw new IllegalStateException();
                        } else {
                            currentSlot = ++currentSlot & biggerMask;
                        }
                    } while (currentSlot != defaultSlot);
                }
            }
            this.keys = biggerKeys;
            this.children = biggerChildren;
            this.modulusMask = biggerMask;
        }

        // Return the node for a key or create a new hashmap node for that key
        // and return that.
        private HashmapNode<T> getOrAddChild(char key) {
            // Check if we need to resize. Capacity of 2^16 doesn't need to resize.
            // If capacity is <16 and arrays are full or capacity is >16 and
            // arrays are 90% full, resize
            if (keys.length < 0x10000 && ((numEntries >= keys.length) || (numEntries > 16 && (numEntries >= keys.length * 0.90f)))) {
                enlarge();
            }
            ++numEntries;
            int defaultSlot = hash(key) & modulusMask;
            int currentSlot = defaultSlot;
            do {
                if (children[currentSlot] == null) {
                    keys[currentSlot] = key;
                    HashmapNode<T> newChild = new HashmapNode<T>();
                    children[currentSlot] = newChild;
                    return newChild;
                } else if (keys[currentSlot] == key) {
                    return (HashmapNode<T>) children[currentSlot];
                } else {
                    currentSlot = ++currentSlot & modulusMask;
                }
            } while (currentSlot != defaultSlot);
            throw new IllegalStateException();
        }

        // FNV-1a hash
        private int hash(char c) {
            // HASH_BASIS = 0x811c9dc5;
            final int HASH_PRIME = 16777619;
            return (((0x811c9dc5 ^ (c >> 8)) * HASH_PRIME) ^ (c & 0xff)) * HASH_PRIME;
        }

    }

    // This node is good at representing dense ranges of keys.
    // It has a single array of nodes and a base key value.
    // Child at array index 3 has key of baseChar + 3.
    private static final class RangeNode<T> extends TrieNode<T> {

        private char baseChar = 0;
        private TrieNode<T>[] children;
        private int size = 0;

        @SuppressWarnings("unchecked")
        private RangeNode(HashmapNode<T> oldNode, char from, char to) {
            // Value of the first character
            this.baseChar = from;
            this.size = to - from + 1;
            this.matchLength = oldNode.matchLength;
            this.value = oldNode.value;
            // Avoid even allocating a children array if size is 0.
            if (size <= 0) {
                size = 0;
            } else {
                this.children = new TrieNode[size];
                // Grab the children of the old node.
                for (int i = 0; i < oldNode.children.length; i++) {
                    if (oldNode.children[i] != null) {
                        children[oldNode.keys[i] - from] = oldNode.children[i];
                    }
                }
            }
        }

        @Override
        public void clear() {
            children = null;
            size = 0;
        }

        @Override
        public TrieNode<T> getTransition(char c) {
            // First check if the key is between max and min value.
            // Here we use the fact that char type is unsigned to figure it out
            // with a single condition.
            int idx = (char) (c - baseChar);
            if (idx < size) {
                return children[idx];
            }
            return null;
        }

        @Override
        public boolean isEmpty() {
            return size == 0;
        }

        @Override
        public void mapEntries(EntryVisitor<T> visitor) {
            if (children != null) {
                for (int i = 0; i < children.length; i++) {
                    if (children[i] != null && children[i] != this) {
                        visitor.visit(this, (char) (baseChar + i), children[i]);
                    }
                }
            }
        }

    }

    // Basic node for both
    private static abstract class TrieNode<T> {

        protected int failMatchLength = 0;
        protected int failMatchOffset = 0;
        protected T failValue;
        protected int matchLength = 0;
        protected T value;

        public abstract void clear();

        // Get transition (root node returns something non-null for all characters - itself)
        public abstract TrieNode<T> getTransition(char c);

        public abstract boolean isEmpty();

        public abstract void mapEntries(final EntryVisitor<T> visitor);

    }

}
