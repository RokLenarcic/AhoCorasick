package com.roklenarcic.util.strings;

public interface MatchListener {

    // return true to continue matching
    boolean match(final int startPosition, final int endPosition);

}
