package com.songoda.epicfarming.tasks;

import com.songoda.epicfarming.EpicFarmingPlugin;
import com.songoda.epicfarming.api.farming.Farm;
import com.songoda.epicfarming.api.farming.FarmManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Hopper;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class HopperTask extends BukkitRunnable {

    private static HopperTask instance;
    private final FarmManager manager;
    
    private HopperTask(EpicFarmingPlugin plugin) {
        this.manager = plugin.getFarmManager();
    }


    public static HopperTask startTask(EpicFarmingPlugin plugin) {
        if (instance == null) {
            instance = new HopperTask(plugin);
            instance.runTaskTimer(plugin, 0, 8);
        }

        return instance;
    }

    @Override
    public void run() {
        for (Farm farm : manager.getFarms().values()) {
            Location farmLocation = farm.getLocation();
            if (farmLocation == null || farmLocation.getWorld() == null) {
                manager.removeFarm(farm.getLocation());
                continue;
            }

            int x = farmLocation.getBlockX() >> 4;
            int z = farmLocation.getBlockZ() >> 4;

            if (!farmLocation.getWorld().isChunkLoaded(x, z)) {
                continue;
            }

            Block block = farmLocation.getBlock().getRelative(BlockFace.DOWN).getRelative(BlockFace.DOWN);

            if (block.getType() != Material.HOPPER)
                continue;

            Inventory inventory = farm.getInventory();
            Inventory hopperInventory = ((Hopper) block.getState()).getInventory();

            for (int i = 27; i < inventory.getSize(); i++) {
                if (inventory.getItem(i) == null || inventory.getItem(i).getType() == Material.AIR) continue;

                int amtToMove = 1;

                ItemStack item = inventory.getItem(i);
                if (item.getType() == Material.BONE_MEAL) continue;

                ItemStack toMove = item.clone();
                toMove.setAmount(amtToMove);

                int newAmt = item.getAmount() - amtToMove;

                if (canHop(hopperInventory, toMove)) {
                    if (newAmt <= 0)
                        inventory.setItem(i, null);
                    else
                        item.setAmount(newAmt);
                    hopperInventory.addItem(toMove);
                }
                break;
            }
        }
    }

    private boolean canHop(Inventory i, ItemStack item) {
        if (i.firstEmpty() != -1) return true;
        for (ItemStack it : i.getContents()) {
            if (it == null || it.isSimilar(item) && (it.getAmount() + item.getAmount()) <= it.getMaxStackSize()) {
                return true;
            }
        }
        return false;
    }
}