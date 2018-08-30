package com.songoda.epicfarming.events;

import com.songoda.epicfarming.EpicFarmingPlugin;
import com.songoda.epicfarming.api.farming.Farm;
import com.songoda.epicfarming.api.farming.Level;
import com.songoda.epicfarming.farming.EFarm;
import com.songoda.epicfarming.farming.EFarmManager;
import com.songoda.epicfarming.farming.ELevel;
import com.songoda.epicfarming.utils.Debugger;
import com.songoda.epicfarming.utils.Methods;
import org.bukkit.Bukkit;
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

    private EpicFarmingPlugin instance;

    public BlockListeners(EpicFarmingPlugin instance) {
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
            Material farmBlock = Material.valueOf(instance.getConfig().getString("Main.Farm Block Material"));

            boolean allowNonCommandIssued = instance.getConfig().getBoolean("Main.Allow Non Command Issued Farm Items");

            if (e.getPlayer().getItemInHand().getType() != farmBlock
                    || instance.getLevelFromItem(e.getItemInHand()) == 0 && !allowNonCommandIssued) return;

            if (e.getBlockAgainst().getType() == farmBlock) e.setCancelled(true);

            Location location = e.getBlock().getLocation();

            Bukkit.getScheduler().runTaskLater(instance, () -> {
                int level = 1;
                if (instance.getLevelFromItem(e.getItemInHand()) != 0) {
                    level = instance.getLevelFromItem(e.getItemInHand());
                }

                if (location.getBlock().getType() != farmBlock) return;

                EFarm farm = new EFarm(location, instance.getLevelManager().getLevel(level));
                instance.getFarmManager().addFarm(location, farm);

                farm.tillLand(e.getBlock().getLocation());
            }, 1);

        } catch (Exception ex) {
            Debugger.runReport(ex);
        }
    }

    private boolean checkForFarm(Location location) {

        EFarmManager farmManager = instance.getFarmManager();

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
                        if (b2.getType() == Material.valueOf(instance.getConfig().getString("Main.Farm Block Material"))) {
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
            if (event.getBlock().getType() != Material.valueOf(instance.getConfig().getString("Main.Farm Block Material")))
                return;

            Farm farm = instance.getFarmManager().removeFarm(event.getBlock().getLocation());

            instance.getFarmingHandler().getCrops(farm, false);

            if (farm == null) return;

            event.setCancelled(true);

            ItemStack item = instance.makeFarmItem(farm.getLevel());

            Block block = event.getBlock();

            block.setType(Material.AIR);
            block.getLocation().getWorld().dropItemNaturally(block.getLocation().add(.5,.5,.5), item);

            for (ItemStack itemStack : ((EFarm)farm).dumpInventory()) {
                if (itemStack == null) continue;
                farm.getLocation().getWorld().dropItemNaturally(farm.getLocation().add(.5,.5,.5), itemStack);
            }
        } catch (Exception ex) {
            Debugger.runReport(ex);
        }
    }
}