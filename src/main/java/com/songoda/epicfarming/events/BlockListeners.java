package com.songoda.epicfarming.events;

import com.songoda.epicfarming.EpicFarming;
import com.songoda.epicfarming.farming.Farm;
import com.songoda.epicfarming.farming.FarmManager;
import com.songoda.epicfarming.farming.Level;
import com.songoda.epicfarming.utils.Debugger;
import com.songoda.epicfarming.utils.Methods;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Created by songoda on 3/14/2017.
 */
public class BlockListeners implements Listener {

    private EpicFarming instance;

    public BlockListeners(EpicFarming instance) {
        this.instance = instance;
    }

    @EventHandler
    public void onBlockFade(BlockFadeEvent e) {
        try {
            if (checkForFarm(e.getBlock().getLocation())) {
                e.setCancelled(true);
            }

        } catch (Exception ex) {
            Debugger.runReport(ex);
        }
    }

    @EventHandler
    public void onGrow(BlockGrowEvent e) {
        try {
            if (checkForFarm(e.getNewState().getLocation())) {
                e.setCancelled(true);
            }
        } catch (Exception ex) {
            Debugger.runReport(ex);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent e) {
        try {
            if (e.getPlayer().getItemInHand().getType() != Material.END_ROD
                    || Methods.getLevelFromItem(e.getItemInHand()) == 0) return;

            if (e.getBlockAgainst().getType() == Material.END_ROD) e.setCancelled(true);

            Location location = e.getBlock().getLocation();

            Farm farm = new Farm(location, instance.getLevelManager().getLevel(Methods.getLevelFromItem(e.getItemInHand())));
            instance.getFarmManager().addFarm(location, farm);

            farm.tillLand(e.getBlock().getLocation());

        } catch (Exception ex) {
            Debugger.runReport(ex);
        }
    }

    private boolean checkForFarm(Location location) {

        FarmManager farmManager = instance.getFarmManager();

        Block block = location.getBlock();
        for(Level level : instance.getLevelManager().getLevels().values()) {
            int radius = level.getRadius();
            int bx = block.getX();
            int by = block.getY();
            int bz = block.getZ();
            for (int fx = -radius; fx <= radius; fx++) {
                for (int fy = -2; fy <= 1; fy++) {
                    for (int fz = -radius; fz <= radius; fz++) {
                        Block b2 = block.getWorld().getBlockAt(bx + fx, by + fy, bz + fz);
                        if (b2.getType() == Material.END_ROD) {
                            if (!farmManager.getFarms().containsKey(b2.getLocation())) continue;
                            if (level.getRadius() != farmManager.getFarm(b2.getLocation()).getLevel().getRadius()) continue;
                            return true;
                        }

                    }
                }
            }
        }
        return false;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent event) {
        try {
            if (event.getBlock().getType() != Material.END_ROD) return;

            Farm farm = instance.getFarmManager().removeFarm(event.getBlock().getLocation());

            instance.getFarmingHandler().getCrops(farm, false);

            if (farm == null) return;

            event.setCancelled(true);

            ItemStack item = Methods.makeFarmItem(farm.getLevel().getLevel());

            Block block = event.getBlock();

            block.setType(Material.AIR);
            block.getLocation().getWorld().dropItemNaturally(block.getLocation().add(.5,.5,.5), item);

            for (ItemStack itemStack : farm.dumpInventory()) {
                if (itemStack == null) continue;
                farm.getLocation().getWorld().dropItemNaturally(farm.getLocation().add(.5,.5,.5), itemStack);
            }
        } catch (Exception ex) {
            Debugger.runReport(ex);
        }
    }
}
