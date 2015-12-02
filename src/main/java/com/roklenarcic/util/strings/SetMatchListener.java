package com.roklenarcic.util.strings;

/**
 * 
 * @deprecated use {@link MapMatchListener} or {@link ReadableMatchListener} instead
 *
 */
@Deprecated
public interface SetMatchListener {

    // return true to continue matching
    boolean match(String haystack, final int startPosition, final int endPosition);

}
