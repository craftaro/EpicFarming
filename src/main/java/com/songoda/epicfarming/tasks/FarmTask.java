package com.songoda.epicfarming.tasks;

import com.songoda.core.compatibility.CompatibleMaterial;
import com.songoda.core.utils.BlockUtils;
import com.songoda.epicfarming.EpicFarming;
import com.songoda.epicfarming.farming.Crop;
import com.songoda.epicfarming.farming.Farm;
import com.songoda.epicfarming.farming.FarmType;
import com.songoda.epicfarming.settings.Settings;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

public class FarmTask extends BukkitRunnable {

    private static FarmTask instance;
    private static EpicFarming plugin;

    private static Map<UUID, Collection<LivingEntity>> entityCache = new HashMap<>();

    public static FarmTask startTask(EpicFarming pl) {
        if (instance != null) {
            instance.cancel();
        }
        instance = new FarmTask();
        instance.runTaskTimerAsynchronously(plugin = pl, 0, Settings.FARM_TICK_SPEED.getInt());
        return instance;
    }

    public static List<Block> getCrops(Farm farm, boolean add) {
        if (((System.currentTimeMillis() - farm.getLastCached()) > (30 * 1000)) || !add) {
            Bukkit.getScheduler().runTask(plugin, () -> {
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
                            CompatibleMaterial mat = CompatibleMaterial.getBlockMaterial(b2.getType());

                            if (mat == null || !mat.isCrop()) {
                                if (mat == CompatibleMaterial.STONE || mat == CompatibleMaterial.DIRT || mat == CompatibleMaterial.GRASS_BLOCK)
                                    continue;
                                continue;
                            }

                            if (add) {
                                farm.addCachedCrop(b2);
                                continue;
                            }
                            farm.removeCachedCrop(b2);
                            plugin.getGrowthTask().removeCropByLocation(b2.getLocation());
                        }
                    }
                }
            });
        }
        return farm.getCachedCrops();
    }

    @Override
    public void run() {
        for (Farm farm : new ArrayList<>(plugin.getFarmManager().getFarms().values())) {
            try {
                if (!farm.isInLoadedChunk()) continue;

                Location location = farm.getLocation();
                location.add(.5, .5, .5);

                double radius = farm.getLevel().getRadius() + .5;
                Bukkit.getScheduler().runTask(plugin, () -> {
                    entityCache.remove(farm.getUniqueId());
                    entityCache.put(farm.getUniqueId(), plugin.getEntityUtils().getNearbyEntities(location, radius, false)
                            .stream().filter(e -> !(e instanceof Player) && e != null && !(e instanceof ArmorStand))
                            .collect(Collectors.toCollection(ArrayList::new)));
                });


                Collection<LivingEntity> entitiesAroundFarm = entityCache.get(farm.getUniqueId());

                if (entitiesAroundFarm == null) continue;

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
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Bukkit.getScheduler().runTask(plugin, () ->
                plugin.getEntityUtils().clearChunkCache());
    }
}