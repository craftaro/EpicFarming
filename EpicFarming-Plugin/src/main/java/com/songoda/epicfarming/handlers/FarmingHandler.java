package com.songoda.epicfarming.handlers;

import com.songoda.epicfarming.EpicFarmingPlugin;
import com.songoda.epicfarming.api.farming.Farm;
import com.songoda.epicfarming.farming.Crop;
import com.songoda.epicfarming.farming.EFarm;
import com.songoda.epicfarming.utils.CropType;
import com.songoda.epicfarming.utils.Debugger;
import org.bukkit.Bukkit;
import org.bukkit.CropState;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Hopper;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Crops;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class FarmingHandler {

    private EpicFarmingPlugin instance;

    public FarmingHandler(EpicFarmingPlugin instance) {
        this.instance = instance;
        Bukkit.getScheduler().scheduleSyncRepeatingTask(EpicFarmingPlugin.getInstance(), this::farmRunner, 0, instance.getConfig().getInt("Main.Farm Tick Speed"));
        Bukkit.getScheduler().scheduleSyncRepeatingTask(EpicFarmingPlugin.getInstance(), this::hopRunner, 0, 8);
    }


    private void farmRunner() {
        try {
            for (Farm farm : instance.getFarmManager().getFarms().values()) {
                if (farm.getLocation() == null) continue;
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

    private void hopRunner() {
        for (Farm farm : instance.getFarmManager().getFarms().values()) {
            if (farm.getLocation() == null || farm.getLocation().getBlock() == null) {
                instance.getFarmManager().removeFarm(farm.getLocation());
                continue;
            }
            Block block = farm.getLocation().getBlock();

            if (block.getRelative(BlockFace.DOWN).getType() != Material.HOPPER)
                return;

            Inventory inventory = farm.getInventory();
            Inventory hopperInventory = ((Hopper) block.getRelative(BlockFace.DOWN).getState()).getInventory();

            for (int i = 27; i < inventory.getSize(); i++) {
                if (inventory.getItem(i) == null || inventory.getItem(i).getType() == Material.AIR) continue;

                int amtToMove = 1;

                ItemStack item = inventory.getItem(i);

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

    private boolean doDrop(Farm farm, Material material) {
        Random random = new Random();

        CropType cropTypeData = CropType.getCropType(material);

        if (material == null || farm == null || cropTypeData == null) return false;


        ItemStack stack = new ItemStack(cropTypeData.getYieldMaterial());
        ItemStack seedStack = new ItemStack(cropTypeData.getSeedMaterial(), random.nextInt(3) + 1);

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
                if (stack.isSimilar(item) && stack.getAmount() < stack.getMaxStackSize()) {
                    return true;
                }
            }
        } catch (Exception e) {
            Debugger.runReport(e);
        }
        return false;
    }
}
