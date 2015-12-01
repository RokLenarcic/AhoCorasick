package com.roklenarcic.util.strings;

import java.util.Iterator;

import com.roklenarcic.util.strings.threshold.RangeNodeThreshold;
import com.roklenarcic.util.strings.threshold.Thresholder;

// Standard Aho-Corasick set
// It matches all occurences of the strings in the set anywhere.
// It is highly optimized for this particular use.
public class AhoCorasickSet extends AhoCorasickMap<String> implements StringSet {

    public AhoCorasickSet(final Iterable<String> keywords, boolean caseSensitive) {
        this(keywords, caseSensitive, new RangeNodeThreshold());
    }

    public AhoCorasickSet(final Iterable<String> keywords, boolean caseSensitive, final Thresholder thresholdStrategy) {
    	super(new KeyKeyIterator(keywords), caseSensitive, thresholdStrategy);
    }

    @Deprecated
    public void match(final String haystack, final SetMatchListener listener) {
    	MapMatchListener<String> maListener = new MapMatchListener<String>() {

			public boolean match(String haystack, int startPosition, int endPosition, String value) {
				return listener.match(haystack, startPosition, endPosition);
			}
		};
    	
    	super.match(haystack, maListener);
    }
    
    private static class KeyKeyIterator implements Iterator<KeyValue<String>> {
    	
    	private final Iterator<String> keys;
    	
    	public KeyKeyIterator(Iterable<String> keys) {
    		this.keys = keys.iterator();
    	}
    	
    	public boolean hasNext() {
    		return keys.hasNext();
    	}
    	
    	public KeyValue<String> next() {
    		String key = keys.next();
    		return new KeyValue<String>(key, key);
    	}
    	
    }

}

