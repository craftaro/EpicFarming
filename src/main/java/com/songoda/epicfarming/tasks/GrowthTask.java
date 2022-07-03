package com.songoda.epicfarming.tasks;

import com.songoda.core.compatibility.CompatibleMaterial;
import com.songoda.core.utils.BlockUtils;
import com.songoda.epicfarming.EpicFarming;
import com.songoda.epicfarming.farming.Crop;
import com.songoda.epicfarming.farming.FarmType;
import com.songoda.epicfarming.settings.Settings;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class GrowthTask extends BukkitRunnable {
    private final Map<Location, Crop> liveCrops = new HashMap<>();

    private static final Random random = new Random();

    public GrowthTask(EpicFarming plugin) {
        runTaskTimer(plugin, 0, Settings.GROWTH_TICK_SPEED.getInt());
    }

    @Override
    public synchronized void run() {
        List<Crop> toRemove = new ArrayList<>();

        for (Crop crop : liveCrops.values()) {
            if (crop.getFarm().getFarmType() == FarmType.LIVESTOCK
                    || !crop.getFarm().isInLoadedChunk())
                continue;

            CompatibleMaterial blockMat = CompatibleMaterial.getBlockMaterial(crop.getLocation().getBlock().getType());
            if (!blockMat.isCrop()) {
                toRemove.add(crop);
                continue;
            }

            // TODO: This should be in config.
            // TODO: What does cap stand for? What needs to be in the config? (asked by Sprax)
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

    public synchronized void addLiveCrop(Location location, Crop crop) {
        if (!liveCrops.containsKey(location))
            liveCrops.put(location, crop);
    }

    public synchronized void removeCropByLocation(Location location) {
        liveCrops.remove(location);
    }
}
