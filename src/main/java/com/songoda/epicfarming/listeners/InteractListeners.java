package com.songoda.epicfarming.listeners;

import com.songoda.epicfarming.EpicFarming;
import com.songoda.epicfarming.settings.Settings;
import com.songoda.skyblock.SkyBlock;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 * Created by songoda on 3/14/2017.
 */
public class InteractListeners implements Listener {

    private final EpicFarming instance;

    public InteractListeners(EpicFarming instance) {
        this.instance = instance;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockInteract(PlayerInteractEvent e) {
        if (e.getClickedBlock() == null
                || e.getClickedBlock().getType() != Settings.FARM_BLOCK_MATERIAL.getMaterial().getMaterial())
            return;

        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        if (Bukkit.getPluginManager().isPluginEnabled("FabledSkyBlock")) {
            SkyBlock skyBlock = SkyBlock.getInstance();

            if (skyBlock.getWorldManager().isIslandWorld(e.getPlayer().getWorld()))
                if (!skyBlock.getPermissionManager().hasPermission(e.getPlayer(),
                        skyBlock.getIslandManager().getIslandAtLocation(e.getClickedBlock().getLocation()),
                        "EpicFarming"))
                    return;
        }

        Location location = e.getClickedBlock().getLocation();

        if (instance.getFarmManager().getFarms().containsKey(location)) {
            e.setCancelled(true);
            instance.getFarmManager().getFarm(location).view(e.getPlayer(), false);
        }
    }
}