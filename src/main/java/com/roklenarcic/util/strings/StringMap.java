package com.roklenarcic.util.strings;

public interface StringMap<T> {
    void match(final String haystack, final MapMatchListener<T> listener);
}
