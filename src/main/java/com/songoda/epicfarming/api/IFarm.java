package com.songoda.epicfarming.api;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;

public interface IFarm extends InventoryHolder {

    void view(Player player);

    void upgrade(UpgradeType type, Player player);

    Location getLocation();

    ILevel getLevel();
}
