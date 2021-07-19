package com.songoda.epicfarming.tasks;

import com.songoda.core.compatibility.CompatibleMaterial;
import com.songoda.epicfarming.EpicFarming;
import com.songoda.epicfarming.farming.Farm;
import com.songoda.epicfarming.farming.FarmManager;
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

    private HopperTask(EpicFarming plugin) {
        this.manager = plugin.getFarmManager();
    }

    public static HopperTask startTask(EpicFarming plugin) {
        if (instance != null) {
            instance.cancel();
        }

        instance = new HopperTask(plugin);
        instance.runTaskTimer(plugin, 0, 8);
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

            Block block = farmLocation.getBlock().getRelative(BlockFace.DOWN);

            if (block.getType() != Material.HOPPER)
                continue;

            Inventory hopperInventory = ((Hopper) block.getState()).getInventory();

            for (ItemStack item : farm.getItems().toArray(new ItemStack[0])) {
                if (item.getType() == CompatibleMaterial.BONE_MEAL.getMaterial()) continue;

                ItemStack toMove = item.clone();
                toMove.setAmount(1);

                if (canHop(hopperInventory, toMove)) {
                    farm.removeMaterial(toMove.getType(), 1);
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
