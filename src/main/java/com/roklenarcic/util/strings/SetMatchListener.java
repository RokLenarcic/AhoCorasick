package com.roklenarcic.util.strings;

public interface SetMatchListener {

    // return true to continue matching
    boolean match(String haystack, final int startPosition, final int endPosition);

}
