package com.roklenarcic.util.strings;

class KeyValue<T> {
	
	public final String key;
	public final T value;
	
	public KeyValue(String key, T value) {
		this.key = key;
		this.value = value;
	}
}