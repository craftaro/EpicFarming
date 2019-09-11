package com.songoda.epicfarming.tasks;

import com.songoda.epicfarming.EpicFarming;
import com.songoda.epicfarming.farming.Crop;
import com.songoda.epicfarming.utils.CropHandler;
import com.songoda.epicfarming.utils.CropType;
import org.bukkit.CropState;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.material.Crops;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class GrowthTask extends BukkitRunnable {

    private static GrowthTask instance;

    private Map<Location, Crop> liveCrops = new HashMap<>();

    private static final Random random = new Random();

    public static GrowthTask startTask(EpicFarming plugin) {
        if (instance == null) {
            instance = new GrowthTask();
            instance.runTaskTimer(plugin, 0, plugin.getConfig().getInt("Main.Growth Tick Speed"));
        }

        return instance;
    }

    @Override
    public void run() {
        List<Crop> toRemove =  new ArrayList<>();

        for (Crop crop : liveCrops.values()) {
            Location cropLocation = crop.getLocation();

            int x = cropLocation.getBlockX() >> 4;
            int z = cropLocation.getBlockZ() >> 4;

            if (!cropLocation.getWorld().isChunkLoaded(x, z)) {
                continue;
            }

            if (!CropType.isGrowableCrop(crop.getLocation().getBlock().getType())) {
                toRemove.add(crop);
                continue;
            }

            // TODO: This should be in config.
            int cap = (int)Math.ceil(60 / crop.getFarm().getLevel().getSpeedMultiplier()) - crop.getTicksLived();
            if (cap > 2) {
                int rand = random.nextInt(cap) + 1;

                crop.setTicksLived(crop.getTicksLived() + 1);
                if (rand != cap - 1 && crop.getTicksLived() != cap / 2) continue;
            }

            CropType.grow(crop);

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