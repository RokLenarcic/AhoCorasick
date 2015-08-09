package com.roklenarcic.util.strings;

class Queue<T> {
    private Object[] arr = new Object[50];
    private int first = 0;
    private int last = 0;

    public boolean isEmpty() {
        return first == last;
    }

    @SuppressWarnings("unchecked")
    public T peek() {
        if (!isEmpty()) {
            if (last - 1 < 0) {
                return (T) arr[arr.length - 1];
            } else {
                return (T) arr[last - 1];
            }
        } else {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public T pop() {
        if (!isEmpty()) {
            if (--last < 0) {
                last = arr.length - 1;
            }
            return (T) arr[last];
        } else {
            return null;
        }
    }

    public void push(T n) {
        if (((last + 1) % arr.length) == first) {
            int newCapacity = arr.length + (arr.length >> 1);
            if (newCapacity < 0) {
                newCapacity = Integer.MAX_VALUE - 8;
            }
            Object[] newArr = new Object[newCapacity];
            if (first <= last) {
                System.arraycopy(arr, first, newArr, first, last - first);
            } else {
                System.arraycopy(arr, first, newArr, 0, arr.length - first);
                System.arraycopy(arr, 0, newArr, arr.length - first, last);
                last += arr.length - first;
                first = 0;
            }
            arr = newArr;
        }
        arr[last] = n;
        last = ++last % arr.length;
    }

    public T take() {
        if (!isEmpty()) {
            @SuppressWarnings("unchecked")
            T ret = (T) arr[first];
            first = ++first % arr.length;
            return ret;
        } else {
            return null;
        }
    }

}
