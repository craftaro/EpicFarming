package com.songoda.epicfarming.api.farming;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.inventory.Inventory;

import java.util.List;
import java.util.UUID;

public interface Farm {
    Inventory getInventory();

    List<Block> getCachedCrops();

    Location getLocation();

    UUID getPlacedBy();

    void setLocation(Location location);

    Level getLevel();
}
