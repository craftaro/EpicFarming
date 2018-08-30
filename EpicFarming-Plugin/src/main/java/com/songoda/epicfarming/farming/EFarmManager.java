package com.songoda.epicfarming.farming;

import com.songoda.epicfarming.api.farming.Farm;
import com.songoda.epicfarming.api.farming.FarmManager;
import org.bukkit.Location;
import org.bukkit.block.Block;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class EFarmManager implements FarmManager {

    private final Map<Location, Farm> registeredFarms = new HashMap<>();

    @Override
    public void addFarm(Location location, Farm farm) {
        registeredFarms.put(roundLocation(location), farm);
    }

    @Override
    public Farm removeFarm(Location location) {
        return registeredFarms.remove(roundLocation(location));
    }

    @Override
    public Farm getFarm(Location location) {
        return registeredFarms.get(roundLocation(location));
    }

    @Override
    public Farm getFarm(Block block) {
        return getFarm(block.getLocation());
    }

    @Override
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