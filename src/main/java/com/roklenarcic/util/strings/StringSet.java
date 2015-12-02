package com.roklenarcic.util.strings;

/**
 * @deprecated use {@link StringMap}
 */
@Deprecated
public interface StringSet {
    void match(final String haystack, final SetMatchListener listener);
}
