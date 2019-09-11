package com.songoda.epicfarming.storage;

import com.songoda.epicFarming.utils.Methods;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;

public class StorageItem {

    private final Object object;
    private String key = null;

    public StorageItem(Object object) {
        this.object = object;
    }

    public StorageItem(String key, Object object) {
        this.key = key;
        this.object = object;
    }

    public StorageItem(String key, List<String> string) {
        StringBuilder object = new StringBuilder();
        for (String s : string) {
            object.append(s).append(";");
        }
        this.key = key;
        this.object = object.toString();
    }

    public StorageItem(String key, boolean type, List<Location> blocks) {
        StringBuilder object = new StringBuilder();
        for (Location location : blocks) {
            object.append(Methods.serializeLocation(location));
            object.append(";;");
        }
        this.key = key;
        this.object = object.toString();
    }

    public String getKey() {
        return key;
    }

    public String asString() {
        if (object == null) return null;
        return (String) object;
    }

    public boolean asBoolean() {
        if (object == null) return false;
        if (object instanceof Integer) return (Integer) object == 1;
        return (boolean) object;
    }

    public int asInt() {
        if (object == null) return 0;
        return (int) object;
    }

    public Object asObject() {
        if (object == null) return null;
        if (object instanceof Boolean) return (Boolean) object ? 1 : 0;
        return object;
    }

    public List<String> asStringList() {
        if (object instanceof ArrayList) return new ArrayList<>();
        List<String> list = new ArrayList<>();
        if (object == null) return list;
        String[] stack = ((String) object).split(";");
        for (String item : stack) {
            if (item.equals("")) continue;
            list.add(item);
        }
        return list;
    }

}
