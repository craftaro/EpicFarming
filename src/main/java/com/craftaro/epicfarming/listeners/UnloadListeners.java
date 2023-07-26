package com.craftaro.epicfarming.listeners;

import com.craftaro.epicfarming.EpicFarming;
import com.craftaro.epicfarming.farming.Farm;
import com.craftaro.epicfarming.settings.Settings;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkUnloadEvent;

import java.util.ArrayList;

public class UnloadListeners implements Listener {
    private final EpicFarming plugin;

    public UnloadListeners(EpicFarming plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onUnload(ChunkUnloadEvent event) {
        Material type = Settings.FARM_BLOCK_MATERIAL.getMaterial().parseMaterial();
        for (Farm farm : new ArrayList<>(this.plugin.getFarmManager().getFarms().values())) {
            int x = farm.getLocation().getBlockX() >> 4;
            int z = farm.getLocation().getBlockZ() >> 4;
            if (event.getChunk().getX() == x && event.getChunk().getZ() == z) {
                if (farm.getLocation().getBlock().getType() != type) {
                    this.plugin.getFarmManager().removeFarm(farm.getLocation());
                }
            }
        }
    }
}
