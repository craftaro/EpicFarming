package com.songoda.epicfarming.storage;

import com.songoda.epicfarming.utils.Serializers;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StorageItem {
    private String key = null;

    private Object object;

    public StorageItem(Object object) {
        this.object = object;
    }

    public StorageItem(String key, Object object) {
        this.key = key;
        this.object = object;
    }

    public StorageItem(String key, List<ItemStack> items) {
        if (items == null || items.isEmpty()) {
            return;
        }

        StringBuilder object = new StringBuilder();
        for (ItemStack item : items) {
            if (item == null) {
                continue;
            }

            object.append(Serializers.serialize(item));
            object.append(";;");
        }

        this.key = key;
        this.object = object.toString();
    }

    public String getKey() {
        return this.key;
    }

    public String asString() {
        if (this.object == null) {
            return null;
        }
        return (String) this.object;
    }

    public boolean asBoolean() {
        if (this.object == null) {
            return false;
        }

        return (boolean) this.object;
    }

    public int asInt() {
        if (this.object == null) {
            return 0;
        }

        return (int) this.object;
    }

    public Object asObject() {
        return this.object;
    }

    public List<ItemStack> asItemStackList() {
        List<ItemStack> list = new ArrayList<>();
        if (this.object == null) {
            return list;
        }

        String obj = (String) this.object;
        if (obj.equals("[]")) {
            return list;
        }

        List<String> sers = new ArrayList<>(Arrays.asList(obj.split(";;")));
        for (String ser : sers) {
            list.add(Serializers.deserialize(ser));
        }
        return list;
    }
}
