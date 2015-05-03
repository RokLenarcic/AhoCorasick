package com.roklenarcic.util.strings;

public interface MapMatchListener<T> {

    // return true to continue matching
    boolean match(final int startPosition, final int endPosition, final T value);

}
