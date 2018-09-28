package com.songoda.epicfarming.tasks;

import com.songoda.epicfarming.EpicFarmingPlugin;
import com.songoda.epicfarming.api.EpicFarming;
import com.songoda.epicfarming.api.farming.Farm;
import com.songoda.epicfarming.boost.BoostData;
import com.songoda.epicfarming.farming.Crop;
import com.songoda.epicfarming.utils.CropType;
import com.songoda.epicfarming.utils.Debugger;
import com.songoda.epicfarming.utils.Methods;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Crops;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class FarmTask extends BukkitRunnable {

    private static FarmTask instance;
    private static EpicFarmingPlugin plugin;

    private static final Random random = new Random();

    public static FarmTask startTask(EpicFarmingPlugin pl) {
        if (instance == null) {
            instance = new FarmTask();
            plugin = pl;
            instance.runTaskTimer(plugin, 0, plugin.getConfig().getInt("Main.Growth Tick Speed"));
        }

        return instance;
    }

    @Override
    public void run() {
        for (Farm farm : plugin.getFarmManager().getFarms().values()) {
            if (farm.getLocation() == null) continue;

            for (Block block : getCrops(farm, true)) {
                Crops crop = (Crops) block.getState().getData();

                // Add to GrowthTask
                plugin.getGrowthTask().addLiveCrop(block.getLocation(), new Crop(block.getLocation(), farm));

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
    }

    private boolean doDrop(Farm farm, Material material) {
        Random random = new Random();

        CropType cropTypeData = CropType.getCropType(material);

        if (material == null || farm == null || cropTypeData == null) return false;

        BoostData boostData = plugin.getBoostManager().getBoost(farm.getPlacedBy());

        ItemStack stack = new ItemStack(cropTypeData.getYieldMaterial(), (useBoneMeal(farm) ? random.nextInt(2) + 2 : 1) * (boostData == null ? 1 : boostData.getMultiplier()));
        ItemStack seedStack = new ItemStack(cropTypeData.getSeedMaterial(), random.nextInt(3) + 1 + (useBoneMeal(farm) ? 1 : 0));

        if (!canMove(farm.getInventory(), stack)) return false;
        Methods.animate(farm.getLocation(), cropTypeData.getYieldMaterial());
        farm.getInventory().addItem(stack);
        farm.getInventory().addItem(seedStack);
        return true;
    }

    private boolean useBoneMeal(Farm farm) {
        Inventory inventory = farm.getInventory();

        for (int i = 27; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null || inventory.getItem(i).getType() == Material.AIR) continue;

            ItemStack item = inventory.getItem(i);

            if (item.getType() != Material.BONE_MEAL) continue;

            int newAmt = item.getAmount() - 1;

            if (newAmt <= 0)
                inventory.setItem(i, null);
            else
                item.setAmount(newAmt);

            return true;

        }
        return false;
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

                    if (add) {
                        crops.add(b2);
                        continue;
                    }
                    plugin.getGrowthTask().removeCropByLocation(b2.getLocation());

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