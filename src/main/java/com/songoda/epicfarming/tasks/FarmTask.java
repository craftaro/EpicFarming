package com.songoda.epicfarming.tasks;

import com.songoda.core.compatibility.CompatibleMaterial;
import com.songoda.core.utils.BlockUtils;
import com.songoda.epicfarming.EpicFarming;
import com.songoda.epicfarming.boost.BoostData;
import com.songoda.epicfarming.farming.Crop;
import com.songoda.epicfarming.farming.Farm;
import com.songoda.epicfarming.utils.CropType;
import com.songoda.epicfarming.utils.Methods;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class FarmTask extends BukkitRunnable {

    private static FarmTask instance;
    private static EpicFarming plugin;
    Random random = new Random();

    public static FarmTask startTask(EpicFarming pl) {
        if (instance == null) {
            instance = new FarmTask();
            plugin = pl;
            instance.runTaskTimer(plugin, 0, plugin.getConfig().getInt("Main.Growth Tick Speed"));
        }

        return instance;
    }

    @Override
    public void run() {
        HashMap<Material, Boolean> hasDropped = new HashMap();
        for (Farm farm : plugin.getFarmManager().getFarms().values()) {
            if (!farm.isInLoadedChunk()) continue;

            hasDropped.clear();

            for (Block block : getCrops(farm, true)) {
                Material mat = block.getType();
                if (!CropType.isCrop(mat)) continue;

                if (!BlockUtils.isCropFullyGrown(block)) {
                    // Add to GrowthTask
                    plugin.getGrowthTask().addLiveCrop(block.getLocation(), new Crop(block.getLocation(), farm));
                } else if (farm.getLevel().isAutoHarvest() && !hasDropped.getOrDefault(mat, false) && doDrop(farm, mat)) {
                    hasDropped.put(mat, true);

                    if (farm.getLevel().isAutoReplant()) {
                        BlockUtils.resetGrowthStage(block);
                        continue;
                    }
                    block.setType(Material.AIR);
                }
            }
        }
    }

    private boolean doDrop(Farm farm, Material material) {
        CropType cropTypeData = CropType.getCropType(material);

        if (material == null || farm == null || cropTypeData == null) return false;

        BoostData boostData = plugin.getBoostManager().getBoost(farm.getPlacedBy());

        ItemStack stack = new ItemStack(cropTypeData.getYieldMaterial(), (useBoneMeal(farm) ? random.nextInt(2) + 2 : 1) * (boostData == null ? 1 : boostData.getMultiplier()));
        ItemStack seedStack = new ItemStack(cropTypeData.getSeedMaterial(), random.nextInt(3) + 1 + (useBoneMeal(farm) ? 1 : 0));

        if (!farm.willFit(stack) || !farm.willFit(seedStack)) return false;
        Methods.animate(farm.getLocation(), cropTypeData.getYieldMaterial());
        farm.addItem(stack);
        farm.addItem(seedStack);
        return true;
    }

    private boolean useBoneMeal(Farm farm) {
        for (ItemStack item : farm.getItems()) {
            if (item.getType() != Material.BONE_MEAL) continue;

            farm.removeMaterial(Material.BONE_MEAL, 1);

            return true;

        }
        return false;
    }

    public List<Block> getCrops(Farm farm, boolean add) {
        if (((System.currentTimeMillis() - farm.getLastCached()) > (30 * 1000)) || !add) {
            farm.setLastCached(System.currentTimeMillis());
            if (add) farm.clearCache();
            Block block = farm.getLocation().getBlock();
            int radius = farm.getLevel().getRadius();
            int bx = block.getX();
            int by = block.getY();
            int bz = block.getZ();
            for (int fx = -radius; fx <= radius; fx++) {
                for (int fy = -2; fy <= 1; fy++) {
                    for (int fz = -radius; fz <= radius; fz++) {
                        Block b2 = block.getWorld().getBlockAt(bx + fx, by + fy, bz + fz);
                        CompatibleMaterial mat = CompatibleMaterial.getMaterial(b2.getType());

                        if (!mat.isCrop()) continue;

                        if (add) {
                            farm.addCachedCrop(b2);
                            continue;
                        }
                        farm.removeCachedCrop(b2);
                        plugin.getGrowthTask().removeCropByLocation(b2.getLocation());
                    }
                }
            }
        }
        return farm.getCachedCrops();
    }

    private boolean canMove(Inventory inventory, ItemStack item) {
        if (inventory.firstEmpty() != -1) return true;

        for (ItemStack stack : inventory.getContents()) {
            if (stack.isSimilar(item) && stack.getAmount() < stack.getMaxStackSize()) {
                return true;
            }
        }
        return false;
    }

}