package com.example.hcc_elektrobit.training;

import androidx.annotation.Nullable;

/**
 * Used as a wrapper for data that is exposed via a LiveData that represents an event.
 */
public class Event<T> {
    private boolean hasBeenHandled = false;
    private final T content;

    public Event(T content) {
        this.content = content;
    }

    /**
     * Returns the content if it hasn't been handled yet.
     * Marks the content as handled to prevent subsequent accesses.
     */
    @Nullable
    public T getContentIfNotHandled() {
        if (hasBeenHandled) {
            return null;
        } else {
            hasBeenHandled = true;
            return content;
        }
    }

    /**
     * Returns the content, even if it's already been handled.
     */
    public T peekContent() {
        return content;
    }
}