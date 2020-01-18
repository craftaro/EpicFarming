package com.songoda.epicfarming.tasks;

import com.songoda.core.compatibility.CompatibleMaterial;
import com.songoda.core.utils.BlockUtils;
import com.songoda.epicfarming.EpicFarming;
import com.songoda.epicfarming.farming.Crop;
import com.songoda.epicfarming.farming.Farm;
import com.songoda.epicfarming.farming.FarmType;
import com.songoda.epicfarming.settings.Settings;
import com.songoda.epicfarming.utils.CropType;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class FarmTask extends BukkitRunnable {

    private static FarmTask instance;
    private static EpicFarming plugin;

    public static FarmTask startTask(EpicFarming pl) {
        if (instance != null) {
            instance.cancel();
        }
        instance = new FarmTask();
        instance.runTaskTimer(plugin = pl, 0, Settings.FARM_TICK_SPEED.getInt());
        return instance;
    }

    @Override
    public void run() {
        for (Farm farm : plugin.getFarmManager().getFarms().values()) {
            if (!farm.isInLoadedChunk()) continue;

            Location location = farm.getLocation();
            location.add(.5, .5, .5);

            double radius = farm.getLevel().getRadius() + .5;
            Collection<LivingEntity> entitiesAroundFarm = location.getWorld().getNearbyEntities(location, radius, radius, radius)
                    .stream().filter(e -> !(e instanceof Player) && e instanceof LivingEntity && !(e instanceof ArmorStand))
                    .map(entity -> (LivingEntity) entity).collect(Collectors.toCollection(ArrayList::new));

            List<Block> crops = getCrops(farm, true);

            if (farm.getFarmType() != FarmType.LIVESTOCK)
                for (Block block : crops)
                    if (!BlockUtils.isCropFullyGrown(block)) {
                        // Add to GrowthTask
                        plugin.getGrowthTask().addLiveCrop(block.getLocation(), new Crop(block.getLocation(), farm));
                    }

            // Cycle through modules.
            farm.getLevel().getRegisteredModules().stream()
                    .filter(Objects::nonNull)
                    .forEach(module -> {
                        // Run Module
                        module.run(farm, entitiesAroundFarm, crops);
                    });
        }
    }

    public static List<Block> getCrops(Farm farm, boolean add) {
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
                        CompatibleMaterial mat = CompatibleMaterial.getMaterial(b2);

                        if (!mat.isCrop() || !CropType.isGrowableCrop(mat.getBlockMaterial())) continue;

                        if (add) {
                            farm.addCachedCrop(b2);
                            continue;
                        }
                        farm.removeCachedCrop(b2);
                        EpicFarming.getInstance().getGrowthTask().removeCropByLocation(b2.getLocation());
                    }
                }
            }
        }
        return farm.getCachedCrops();
    }
}