package com.songoda.epicfarming.api.farming;

import org.bukkit.Location;
import org.bukkit.inventory.Inventory;

public interface Farm {
    Inventory getInventory();

    Location getLocation();

    void setLocation(Location location);

    Level getLevel();
}
