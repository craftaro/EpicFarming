package com.songoda.epicfarming.api.farming;

import org.bukkit.Location;
import org.bukkit.inventory.Inventory;

import java.util.UUID;

public interface Farm {
    Inventory getInventory();

    Location getLocation();

    UUID getPlacedBy();

    void setLocation(Location location);

    Level getLevel();
}
