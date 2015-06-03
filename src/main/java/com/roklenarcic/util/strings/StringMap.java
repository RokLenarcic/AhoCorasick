package com.roklenarcic.util.strings;

import java.io.IOException;

public interface StringMap<T> {
    void match(final Readable haystack, final ReadableMatchListener<T> listener) throws IOException;

    void match(final String haystack, final MapMatchListener<T> listener);
}
