package com.roklenarcic.util.strings;

public interface SetMatchListener {

    // return true to continue matching
    boolean match(final int startPosition, final int endPosition);

}
