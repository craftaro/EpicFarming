package com.craftaro.epicfarming.listeners;

import com.craftaro.core.third_party.com.cryptomorin.xseries.XMaterial;
import com.craftaro.epicfarming.EpicFarming;
import com.craftaro.epicfarming.settings.Settings;
import com.craftaro.skyblock.SkyBlock;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class InteractListeners implements Listener {
    private final EpicFarming plugin;

    public InteractListeners(EpicFarming plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockInteract(PlayerInteractEvent e) {
        if (e.getClickedBlock() == null) {
            return;
        }
        Location location = e.getClickedBlock().getLocation();

        if (e.getItem() != null && XMaterial.BONE_MEAL.isSimilar(e.getItem()) && this.plugin.getFarmManager().checkForFarm(location) != null) {
            e.setCancelled(true);
        }

        if (e.getClickedBlock().getType() != Settings.FARM_BLOCK_MATERIAL.getMaterial().parseMaterial()) {
            return;
        }

        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        if (Bukkit.getPluginManager().isPluginEnabled("FabledSkyBlock")) {
            SkyBlock skyBlock = SkyBlock.getInstance();

            if (skyBlock.getWorldManager().isIslandWorld(e.getPlayer().getWorld())) {
                if (!skyBlock.getPermissionManager().hasPermission(
                        e.getPlayer(),
                        skyBlock.getIslandManager().getIslandAtLocation(e.getClickedBlock().getLocation()),
                        "EpicFarming")
                ) {
                    return;
                }
            }
        }

        if (this.plugin.getFarmManager().getFarms().containsKey(location)) {
            e.setCancelled(true);
            this.plugin.getFarmManager().getFarm(location).view(e.getPlayer(), false);
        }
    }
}
