package com.roklenarcic.util.strings;

import java.util.Arrays;
import java.util.Iterator;

// Matches leftmost shortest matches. Useful when you want non-overlapping
// matches with a string set that doesn't have strings that are prefix to other strings in the set.
public class ShortestMatchMap<T> implements StringMap<T> {

    private boolean caseSensitive = true;
    private TrieNode<T> root;

    public ShortestMatchMap(final Iterator<String> keywords, final Iterator<? extends T> values, boolean caseSensitive) {
        // Create the root node
        root = new HashmapNode<T>(true);
        // Add all keywords
        OUTER: while (keywords.hasNext() && values.hasNext()) {
            final String keyword = keywords.next();
            T value = values.next();
            // Skip any empty keywords
            if (keyword != null && keyword.length() > 0) {
                // Start with the current node and traverse the tree
                // character by character. Add nodes as needed to
                // fill out the tree.
                HashmapNode<T> currentNode = (HashmapNode<T>) root;
                for (int idx = 0; idx < keyword.length(); idx++) {
                    currentNode = currentNode.getOrAddChild(caseSensitive ? keyword.charAt(idx) : Character.toLowerCase(keyword.charAt(idx)));
                    // If currentNode has a match go on to the next keyword, the current keyword won't ever
                    // match, because there's a shorter one in the set.
                    if (currentNode.matchLength != 0) {
                        continue OUTER;
                    }
                }
                // Last node will contains the keyword as a match.
                // Suffix matches will be added later.
                currentNode.matchLength = keyword.length();
                currentNode.value = value;
            }
        }
        // Go through nodes depth first, swap any hashmap nodes,
        // whose size is close to the size of range of keys with
        // flat array based nodes.
        root = optimizeNodes(root);

        // Calculate fail transitions and add suffix matches to nodes.
        // A lot of these properties are defined in a recursive fashion i.e.
        // calculating for a 3 letter word requires having done the calculation
        // for all 2 letter words.
        //
        // Setup a queue to enable breath-first processing.
        final Queue<TrieNode<T>> queue = new Queue<TrieNode<T>>();
        EntryVisitor<T> failTransAndOutputsVisitor = new EntryVisitor<T>() {

            public void visit(TrieNode<T> parent, char key, TrieNode<T> value) {
                // Get fail transiton of the parent.
                TrieNode<T> parentFail = parent.getFailTransition();
                // Since root node has null fail transition, first level nodes have null parentFail.
                if (parentFail == null) {
                    // First level nodes have one possible fail transition, which is
                    // root because the only possible suffix to a one character
                    // string is an empty string
                    value.failTransition = parent;
                } else {
                    // Dig up the tree until you find a fail transition.
                    do {
                        // Suffix of a parent + transition character from parent to this
                        // node is the suffix of this node.

                        // parent ---char---> value
                        // parentFail ----char----> valueFail
                        // e.g. "ab" -> c -> "abc"
                        // "b" -> c -> "bc"
                        final TrieNode<T> matchContinuation = parentFail.getTransition(key);
                        if (matchContinuation != null) {
                            value.failTransition = matchContinuation;
                        } else {
                            // If parentFail didn't have key mapping
                            // take parentFail's failTransition and try again
                            // The last fail transition is the root node, which
                            // always has a key mapping.
                            parentFail = parentFail.getFailTransition();
                        }
                    } while (value.failTransition == null);
                    // Now that we have a fail transition, if this node has no match,
                    // find follow fail transitions to find a node that has match.
                    if (value.matchLength == 0) {
                        TrieNode<T> fail = value.failTransition;
                        while (fail != root && fail.matchLength == 0) {
                            fail = fail.failTransition;
                        }
                        value.matchLength = fail.matchLength;
                        value.value = fail.value;
                    }
                    // If node has any kind of match (naturally or from fail transition),
                    // then no progression is possible, so clear all the transitions, also,
                    // make fail transition a root.
                    if (value.matchLength != 0) {
                        value.clear();
                        value.failTransition = root;
                    }
                }
                // Queue the non-leaf node.
                if (!value.isEmpty()) {
                    queue.push(value);
                }
            }

        };
        root.mapEntries(failTransAndOutputsVisitor);
        while (!queue.isEmpty()) {
            queue.pop().mapEntries(failTransAndOutputsVisitor);
        }

        // Range nodes represent a range of transitions without all the transitions in the range being
        // there. In case of hitting on an empty slot the logic in match loop runs down the fail transition
        // chain to find a node with a transition for that char. Instead of wasting space on empty slots
        // we can do that beforehand and add that transition to the node. We need to do that in depth first
        // fashion, otherwise an endless loop can form.
        EntryVisitor<T> fillOutRangeNodesVisitor = new EntryVisitor<T>() {

            public void visit(TrieNode<T> parent, char key, TrieNode<T> value) {
                // go depth first
                if (!value.isEmpty()) {
                    value.mapEntries(this);
                }
                if (value instanceof RangeNode) {
                    // Range nodes have gaps (null values) in their array. We can put this wasted
                    // memory to work by filling these gaps with the correct next node for that character
                    // which we can figure out by following failure transitions.
                    RangeNode<T> rangeNode = (RangeNode<T>) value;
                    for (int i = 0; i < rangeNode.size; i++) {
                        if (rangeNode.children[i] == null) {
                            char charOfMissingTransition = (char) (rangeNode.baseChar + i);
                            // Walk up fail transition until you run out of them (and do nothing)
                            // or one of them has a transition for this char. Put that node
                            // into the empty slot on the range node.
                            TrieNode<T> n = rangeNode.failTransition;
                            while (n != null) {
                                TrieNode<T> nextNode = n.getTransition(charOfMissingTransition);
                                if (nextNode == null) {
                                    n = n.failTransition;
                                } else {
                                    rangeNode.children[i] = nextNode;
                                    break;
                                }
                            }
                        }
                    }
                }
            }

        };
        root.mapEntries(fillOutRangeNodesVisitor);

    }

    public void match(final String haystack, final MapMatchListener<T> listener) {
        // This particualr match method is different from the other match functions in that
        // the current node is lagging behind the character being examined by one position.

        // Start with the root node.
        TrieNode<T> currentNode = root;
        int currentNodeMatchLength = currentNode.matchLength;
        T currentNodeMatchValue = currentNode.value;
        int idx = 0;
        // For each character.
        final int len = haystack.length();
        // Putting this if into the loop worsens the performance so we'll sadly
        // have to deal with duplicated code.
        if (caseSensitive) {
            while (idx < len) {
                final char c = haystack.charAt(idx);
                // The current node at this point is the node after the transition from the last loop
                // iteration.
                if (currentNodeMatchLength != 0) {
                    // If that node had a match output any matches on the node
                    // and jump to root, only leaf nodes have matches so next character won't match anything,
                    // so continue matching from the root.
                    if (!listener.match(idx - currentNodeMatchLength, idx, currentNodeMatchValue)) {
                        break;
                    }
                    currentNode = root.getTransition(c);
                } else {
                    // Try to transition from the current node using the character
                    TrieNode<T> nextNode = currentNode.getTransition(c);
                    while (nextNode == null) {
                        currentNode = currentNode.failTransition;
                        nextNode = currentNode.getTransition(c);
                    }
                    currentNode = nextNode;
                }
                // Save the node match.
                currentNodeMatchLength = currentNode.matchLength;
                currentNodeMatchValue = currentNode.value;
                ++idx;
            }
            // Because we are lagging behind when outputting matches on the current nodes,
            // we need to output a potential match after the loop.
            if (currentNodeMatchLength != 0) {
                // Output any matches on the last node
                listener.match(idx - currentNodeMatchLength, idx, currentNodeMatchValue);
            }
        } else {
            while (idx < len) {
                final char c = Character.toLowerCase(haystack.charAt(idx));
                // The current node at this point is the node after the transition from the last loop
                // iteration.
                if (currentNodeMatchLength != 0) {
                    // If that node had a match output any matches on the node
                    // and jump to root, only leaf nodes have matches so next character won't match anything,
                    // so continue matching from the root.
                    if (!listener.match(idx - currentNodeMatchLength, idx, currentNodeMatchValue)) {
                        break;
                    }
                    currentNode = root.getTransition(c);
                } else {
                    // Try to transition from the current node using the character
                    TrieNode<T> nextNode = currentNode.getTransition(c);
                    while (nextNode == null) {
                        currentNode = currentNode.failTransition;
                        nextNode = currentNode.getTransition(c);
                    }
                    currentNode = nextNode;
                }
                // Save the node match.
                currentNodeMatchLength = currentNode.matchLength;
                currentNodeMatchValue = currentNode.value;
                ++idx;
            }
            // Because we are lagging behind when outputting matches on the current nodes,
            // we need to output a potential match after the loop.
            if (currentNodeMatchLength != 0) {
                // Output any matches on the last node
                listener.match(idx - currentNodeMatchLength, idx, currentNodeMatchValue);
            }
        }
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

        protected HashmapNode(boolean root) {
            super(root);
        }

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
                    return defaultTransition;
                } else {
                    currentSlot = ++currentSlot & modulusMask;
                }
            } while (currentSlot != defaultSlot);
            return defaultTransition;
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
        private void enlarge() {
            char[] biggerKeys = new char[keys.length * 2];
            @SuppressWarnings("unchecked")
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
                    HashmapNode<T> newChild = new HashmapNode<T>(false);
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
            super(oldNode.defaultTransition != null);
            // Value of the first character
            this.baseChar = from;
            this.size = to - from + 1;
            this.value = oldNode.value;
            this.matchLength = oldNode.matchLength;
            // Avoid even allocating a children array if size is 0.
            if (size <= 0) {
                size = 0;
            } else {
                this.children = new TrieNode[size];
                // If original node is root node, prefill everything with yourself.
                if (oldNode.defaultTransition != null) {
                    Arrays.fill(children, this);
                }
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
            return defaultTransition;
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

        protected TrieNode<T> defaultTransition = null;
        protected TrieNode<T> failTransition;
        protected int matchLength;
        protected T value;

        protected TrieNode(boolean root) {
            this.defaultTransition = root ? this : null;
        }

        public abstract void clear();

        // Get fail transition
        public final TrieNode<T> getFailTransition() {
            return failTransition;
        }

        // Get transition (root node returns something non-null for all characters - itself)
        public abstract TrieNode<T> getTransition(char c);

        public abstract boolean isEmpty();

        public abstract void mapEntries(final EntryVisitor<T> visitor);

    }

}
