package com.roklenarcic.util.strings;

import java.util.Iterator;

class KeyValueIterator<T> implements Iterator<KeyValue<T>> {
	
	private final Iterator<String> keywordsIter;
	private final Iterator<? extends T> valuesIter;
	
	public KeyValueIterator(Iterable<String> keys, Iterable<? extends T> values) {
		keywordsIter = keys.iterator();
        valuesIter = values.iterator();
	}
	
	public boolean hasNext() {
		return keywordsIter.hasNext() && valuesIter.hasNext();
	}
	
	public KeyValue<T> next() {
		return new KeyValue<T>(keywordsIter.next(), valuesIter.next());
	}
}