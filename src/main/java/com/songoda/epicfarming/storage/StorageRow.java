package com.songoda.epicfarming.storage;

import java.util.Collections;
import java.util.Map;

public class StorageRow {
    private final String key;

    private final Map<String, StorageItem> items;

    public StorageRow(String key, Map<String, StorageItem> items) {
        this.key = key;
        this.items = items;
    }

    public String getKey() {
        return this.key;
    }

    public Map<String, StorageItem> getItems() {
        return Collections.unmodifiableMap(this.items);
    }

    public StorageItem get(String key) {
        if (!this.items.containsKey(key) || this.items.get(key).asObject().toString().equals("")) {
            return new StorageItem(null);
        }
        return this.items.get(key);
    }
}
