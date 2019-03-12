package com.songoda.epicfarming.api;

import com.songoda.epicfarming.api.farming.FarmManager;
import com.songoda.epicfarming.api.farming.Level;
import com.songoda.epicfarming.api.farming.LevelManager;
import org.bukkit.inventory.ItemStack;

public interface EpicFarming {

    int getLevelFromItem(ItemStack item);

    ItemStack makeFarmItem(Level level);

    FarmManager getFarmManager();

    LevelManager getLevelManager();
}
