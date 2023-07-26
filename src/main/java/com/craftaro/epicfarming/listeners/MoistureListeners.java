package com.craftaro.epicfarming.listeners;

import com.craftaro.epicfarming.EpicFarming;
import com.craftaro.epicfarming.farming.Farm;
import org.bukkit.block.data.type.Farmland;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.MoistureChangeEvent;

public class MoistureListeners implements Listener {
    private final EpicFarming plugin;

    public MoistureListeners(EpicFarming plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onMoistureChange(MoistureChangeEvent event) {
        if (event.getNewState().getBlockData() instanceof Farmland) {
            return;
        }

        Farm farm = this.plugin.getFarmManager().checkForFarm(event.getBlock().getLocation());
        if (farm != null) {
            event.setCancelled(true);
        }
    }
}
