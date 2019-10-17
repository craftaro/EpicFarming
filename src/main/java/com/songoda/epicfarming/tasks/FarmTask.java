package com.songoda.epicfarming.tasks;

import com.songoda.epicfarming.EpicFarming;
import com.songoda.epicfarming.farming.Farm;
import com.songoda.epicfarming.settings.Settings;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

public class FarmTask extends BukkitRunnable {

    private static FarmTask instance;
    private static EpicFarming plugin;

    public static FarmTask startTask(EpicFarming pl) {
        if (instance != null && !instance.isCancelled()) {
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

            // Cycle through modules.
            farm.getLevel().getRegisteredModules().stream()
                    .filter(module -> module != null)
                    .forEach(module -> {
                        // Run Module
                        module.run(farm, entitiesAroundFarm);
                    });
        }
    }
}