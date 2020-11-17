package com.ipeirotis.ofy;

import java.util.List;

public class ListByCursorResult<T> {

    private List<T> items;
    private String nextPageToken;

    public List<T> getItems() {
        return items;
    }

    public ListByCursorResult<T> setItems(List<T> items) {
        this.items = items;
        return this;
    }

    public String getNextPageToken() {
        return nextPageToken;
    }

    public ListByCursorResult<T> setNextPageToken(String nextPageToken) {
        this.nextPageToken = nextPageToken;
        return this;
    }

}

