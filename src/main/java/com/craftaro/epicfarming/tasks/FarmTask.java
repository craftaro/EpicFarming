package com.craftaro.epicfarming.tasks;

import com.craftaro.core.compatibility.CompatibleMaterial;
import com.craftaro.core.compatibility.ServerVersion;
import com.craftaro.core.third_party.com.cryptomorin.xseries.XBlock;
import com.craftaro.core.third_party.com.cryptomorin.xseries.XMaterial;
import com.craftaro.core.utils.BlockUtils;
import com.craftaro.epicfarming.EpicFarming;
import com.craftaro.epicfarming.farming.Crop;
import com.craftaro.epicfarming.farming.Farm;
import com.craftaro.epicfarming.farming.FarmType;
import com.craftaro.epicfarming.settings.Settings;
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
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class FarmTask extends BukkitRunnable {
    private final EpicFarming plugin;

    private final Map<UUID, Collection<LivingEntity>> entityCache = new HashMap<>();

    public FarmTask(EpicFarming plugin) {
        this.plugin = plugin;

        runTaskTimerAsynchronously(this.plugin, 0, Settings.FARM_TICK_SPEED.getInt());
    }

    public List<Block> getCrops(Farm farm, boolean add) {
        if (((System.currentTimeMillis() - farm.getLastCached()) > (30 * 1000)) || !add) {
            Bukkit.getScheduler().runTask(this.plugin, () -> {
                farm.setLastCached(System.currentTimeMillis());
                if (add) {
                    farm.clearCache();
                }
                Block block = farm.getLocation().getBlock();
                int radius = farm.getLevel().getRadius();
                int bx = block.getX();
                int by = block.getY();
                int bz = block.getZ();
                for (int fx = -radius; fx <= radius; fx++) {
                    for (int fy = -2; fy <= 1; fy++) {
                        for (int fz = -radius; fz <= radius; fz++) {
                            Block b2 = block.getWorld().getBlockAt(bx + fx, by + fy, bz + fz);
                            Optional<XMaterial> mat = CompatibleMaterial.getMaterial(b2.getType());

                            if (!mat.isPresent() || !XBlock.isCrop(mat.get())) {
                                continue;
                            }

                            if (add) {
                                farm.addCachedCrop(b2);
                                continue;
                            }
                            farm.removeCachedCrop(b2);
                            this.plugin.getGrowthTask().removeCropByLocation(b2.getLocation());
                        }
                    }
                }
            });
        }

        return farm.getCachedCrops();
    }

    @Override
    public void run() {
        GrowthTask growthTask = this.plugin.getGrowthTask();

        if (ServerVersion.isServerVersionAtLeast(ServerVersion.V1_9) && growthTask.isCancelled()) {
            return;
        }

        for (Farm farm : new ArrayList<>(this.plugin.getFarmManager().getFarms().values())) {
            if (!this.plugin.isEnabled()) {
                return;    // Prevent registering a task on plugin disable
            }

            try {
                if (!farm.isInLoadedChunk()) {
                    continue;
                }

                Location location = farm.getLocation();
                location.add(.5, .5, .5);

                double radius = farm.getLevel().getRadius() + .5;
                Bukkit.getScheduler().runTask(this.plugin, () -> {
                    this.entityCache.remove(farm.getFarmUUID());
                    this.entityCache.put(farm.getFarmUUID(), this.plugin.getEntityUtils().getNearbyEntities(location, radius, false)
                            .stream().filter(e -> !(e instanceof Player) && e != null && !(e instanceof ArmorStand))
                            .collect(Collectors.toCollection(ArrayList::new)));
                });

                Collection<LivingEntity> entitiesAroundFarm = this.entityCache.get(farm.getFarmUUID());

                if (entitiesAroundFarm == null) {
                    continue;
                }

                List<Block> crops = getCrops(farm, true);

                if (farm.getFarmType() != FarmType.LIVESTOCK) {
                    for (Block block : crops) {
                        if (!BlockUtils.isCropFullyGrown(block)) {
                            // Add to GrowthTask
                            growthTask.addLiveCrop(block.getLocation(), new Crop(block.getLocation(), farm));
                        }
                    }
                }

                // Cycle through modules.
                farm.getLevel().getRegisteredModules().stream()
                        .filter(Objects::nonNull)
                        .forEach(module -> {
                            // Run Module
                            module.run(farm, entitiesAroundFarm, crops);
                        });
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        Bukkit.getScheduler().runTask(this.plugin, () -> this.plugin.getEntityUtils().clearChunkCache());
    }
}
