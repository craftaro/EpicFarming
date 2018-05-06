package com.songoda.epicfarming.handlers;

import com.songoda.epicfarming.EpicFarming;
import com.songoda.epicfarming.farming.Crop;
import com.songoda.epicfarming.farming.Farm;
import com.songoda.epicfarming.utils.CropType;
import com.songoda.epicfarming.utils.Debugger;
import org.bukkit.Bukkit;
import org.bukkit.CropState;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Crops;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class FarmingHandler {

    private EpicFarming instance;

    public FarmingHandler(EpicFarming instance) {
        this.instance = instance;
        Bukkit.getScheduler().scheduleSyncRepeatingTask(EpicFarming.getInstance(), this::farmRunner, 0, instance.getConfig().getInt("Main.Farm Tick Speed"));
    }


    private void farmRunner() {
        try {
            for (Farm farm : instance.getFarmManager().getFarms().values()) {
                for (Block block : getCrops(farm, true)) {
                    Crops crop = (Crops) block.getState().getData();

                    // Add to GrowthHandler
                    if (!instance.getGrowthHandler().liveCrops.containsKey(block.getLocation()))
                        instance.getGrowthHandler().liveCrops.put(block.getLocation(), new Crop(block.getLocation(), farm));

                    if (!farm.getLevel().isAutoHarvest()
                            || !crop.getState().equals(CropState.RIPE)
                            || !doDrop(farm, block.getType())) continue;

                    if (farm.getLevel().isAutoReplant()) {
                        BlockState cropState = block.getState();
                        Crops cropData = (Crops) cropState.getData();
                        cropData.setState(CropState.VERY_SMALL);
                        cropState.setData(cropData);
                        cropState.update();
                        continue;
                    }
                    block.setType(Material.AIR);
                }
            }
        } catch (Exception ex) {
            Debugger.runReport(ex);
        }
    }

    private boolean doDrop(Farm farm, Material material) {
        Random random = new Random();

        ItemStack stack = new ItemStack(CropType.getCropType(material).getYieldMaterial());
        ItemStack seedStack = new ItemStack(CropType.getCropType(material).getSeedMaterial(), random.nextInt(3));

        if (!canMove(farm.getInventory(), stack)) return false;
        farm.getInventory().addItem(stack);
        farm.getInventory().addItem(seedStack);
        return true;
    }

    public List<Block> getCrops(Farm farm, boolean add) {
        List<Block> crops = new ArrayList<>();

        Block block = farm.getLocation().getBlock();
        int radius = farm.getLevel().getRadius();
        int bx = block.getX();
        int by = block.getY();
        int bz = block.getZ();
        for (int fx = -radius; fx <= radius; fx++) {
            for (int fy = -2; fy <= 1; fy++) {
                for (int fz = -radius; fz <= radius; fz++) {
                    Block b2 = block.getWorld().getBlockAt(bx + fx, by + fy, bz + fz);

                    if (!(b2.getState().getData() instanceof Crops)) continue;
                    if (add)
                        crops.add(b2);
                    else {
                        instance.getGrowthHandler().liveCrops.remove(b2.getLocation());
                    }

                }
            }
        }
        return crops;
    }

    private boolean canMove(Inventory inventory, ItemStack item) {
        try {
            if (inventory.firstEmpty() != -1) return true;

            for (ItemStack stack : inventory.getContents()) {
                if (stack.isSimilar(item) && stack.getAmount() <= stack.getMaxStackSize()) {
                    return true;
                }
            }
        } catch (Exception e) {
            Debugger.runReport(e);
        }
        return false;
    }
}
