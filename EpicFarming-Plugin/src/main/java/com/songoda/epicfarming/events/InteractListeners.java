package com.songoda.epicfarming.events;

import com.songoda.epicfarming.EpicFarmingPlugin;
import com.songoda.epicfarming.farming.EFarm;
import com.songoda.epicfarming.utils.Debugger;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 * Created by songoda on 3/14/2017.
 */
public class InteractListeners implements Listener {

    private EpicFarmingPlugin instance;

    public InteractListeners(EpicFarmingPlugin instance) {
        this.instance = instance;
    }

    @EventHandler
    public void onBlockInteract(PlayerInteractEvent e) {
        try {
            if (e.getClickedBlock() == null || e.getClickedBlock().getType() != Material.valueOf(instance.getConfig().getString("Main.Farm Block Material")))
                return;

            if (!instance.canBuild(e.getPlayer(), e.getClickedBlock().getLocation())) {
                e.setCancelled(true);
                return;
            }

            if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;

            Location location = e.getClickedBlock().getLocation();

            if (instance.getFarmManager().getFarms().containsKey(location)) {
                e.setCancelled(true);
                ((EFarm)instance.getFarmManager().getFarm(location)).view(e.getPlayer());
            }
        } catch (Exception ex) {
            Debugger.runReport(ex);
        }
    }
}