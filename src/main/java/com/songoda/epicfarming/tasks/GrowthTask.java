package com.songoda.epicfarming.tasks;

import com.songoda.core.compatibility.CompatibleMaterial;
import com.songoda.core.utils.BlockUtils;
import com.songoda.epicfarming.EpicFarming;
import com.songoda.epicfarming.farming.Crop;
import com.songoda.epicfarming.farming.FarmType;
import com.songoda.epicfarming.settings.Settings;
import com.songoda.epicfarming.utils.CropType;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class GrowthTask extends BukkitRunnable {

    private static GrowthTask instance;
    private static EpicFarming plugin;

    private final Map<Location, Crop> liveCrops = new HashMap<>();

    private static final Random random = new Random();

    public static GrowthTask startTask(EpicFarming pl) {
        if (instance != null) {
            instance.cancel();
        }
        instance = new GrowthTask();
        instance.runTaskTimer(plugin = pl, 0, Settings.GROWTH_TICK_SPEED.getInt());
        return instance;
    }

    @Override
    public void run() {
        List<Crop> toRemove = new ArrayList<>();

        for (Crop crop : liveCrops.values()) {
            if (crop.getFarm().getFarmType() == FarmType.LIVESTOCK
                    || !crop.getFarm().isInLoadedChunk())
                continue;

            CompatibleMaterial blockMat = CompatibleMaterial.getMaterial(crop.getLocation().getBlock());
            if (!blockMat.isCrop() || !CropType.isGrowableCrop(blockMat.getBlockMaterial())) {
                toRemove.add(crop);
                continue;
            }

            // TODO: This should be in config.
            int cap = (int) Math.ceil(60 / crop.getFarm().getLevel().getSpeedMultiplier()) - crop.getTicksLived();
            if (cap > 2) {
                int rand = random.nextInt(cap) + 1;

                crop.setTicksLived(crop.getTicksLived() + 1);
                if (rand != cap - 1 && crop.getTicksLived() != cap / 2) continue;
            }

            BlockUtils.incrementGrowthStage(crop.getLocation().getBlock());
            crop.setTicksLived(1);
        }

        for (Crop crop : toRemove)
            liveCrops.remove(crop.getLocation());
    }


    public void addLiveCrop(Location location, Crop crop) {
        if (!liveCrops.containsKey(location))
            liveCrops.put(location, crop);
    }

    public void removeCropByLocation(Location location) {
        liveCrops.remove(location);
    }

}