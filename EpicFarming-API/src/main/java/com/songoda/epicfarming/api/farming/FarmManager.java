package com.songoda.epicfarming.api.farming;

import org.bukkit.Location;
import org.bukkit.block.Block;

import java.util.Map;

public interface FarmManager {
    void addFarm(Location location, Farm farm);

    Farm removeFarm(Location location);

    Farm getFarm(Location location);

    Farm getFarm(Block block);

    Map<Location, Farm> getFarms();
}
