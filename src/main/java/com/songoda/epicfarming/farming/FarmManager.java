package com.songoda.epicfarming.farming;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class FarmManager {

    private final Map<Location, Farm> registeredFarms = new HashMap<>();

    public void addFarm(Location location, Farm farm) {
        registeredFarms.put(roundLocation(location), farm);
    }

    public Farm removeFarm(Location location) {
        return registeredFarms.remove(roundLocation(location));
    }

    public Farm getFarm(Location location) {
        return registeredFarms.get(roundLocation(location));
    }

    public Farm getFarm(Block block) {
        return getFarm(block.getLocation());
    }

    public Map<Location, Farm> getFarms() {
        return Collections.unmodifiableMap(registeredFarms);
    }

    private Location roundLocation(Location location) {
        location = location.clone();
        location.setX(location.getBlockX());
        location.setY(location.getBlockY());
        location.setZ(location.getBlockZ());
        return location;
    }
}