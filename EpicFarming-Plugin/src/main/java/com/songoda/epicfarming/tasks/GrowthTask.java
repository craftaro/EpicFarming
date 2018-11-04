package com.songoda.epicfarming.tasks;

import com.songoda.epicfarming.EpicFarmingPlugin;
import com.songoda.epicfarming.farming.Crop;
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

    public static GrowthTask startTask(EpicFarmingPlugin plugin) {
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

            if (!(crop.getLocation().getBlock().getState().getData() instanceof Crops)) {
                toRemove.add(crop);
                continue;
            }

            //ToDO: This should be in config.
            int cap = (int)Math.ceil(60 / crop.getFarm().getLevel().getSpeedMultiplier()) - crop.getTicksLived();
            if (cap > 2) {
                int rand = random.nextInt(cap) + 1;

                crop.setTicksLived(crop.getTicksLived() + 1);
                if (rand != cap - 1 && crop.getTicksLived() != cap / 2) continue;

            }

            BlockState cropState = crop.getLocation().getBlock().getState();
            Crops cropData = (Crops) cropState.getData();

            Material material = crop.getLocation().getBlock().getType();

            switch(cropData.getState()) {
                case SEEDED:
                    if (material == Material.BEETROOTS)
                        cropData.setState(CropState.VERY_SMALL);
                    else
                        cropData.setState(CropState.GERMINATED);
                    break;
                case GERMINATED:
                    cropData.setState(CropState.VERY_SMALL);
                    break;
                case VERY_SMALL:
                    cropData.setState(CropState.SMALL);
                    break;
                case SMALL:
                    cropData.setState(CropState.MEDIUM);
                    break;
                case MEDIUM:
                    cropData.setState(CropState.TALL);
                    break;
                case TALL:
                    cropData.setState(CropState.VERY_TALL);
                    break;
                case VERY_TALL:
                    cropData.setState(CropState.RIPE);
                    break;
                case RIPE:
                    break;
            }
            cropState.setData(cropData);
            cropState.update();
            crop.setTicksLived(1);

        }
        for (Crop crop : toRemove)
            liveCrops.remove(crop);
    }

    public void addLiveCrop(Location location, Crop crop) {
        if (!liveCrops.containsKey(location))
            liveCrops.put(location, crop);
    }

    public void removeCropByLocation(Location location) {
        liveCrops.remove(location);
    }

}