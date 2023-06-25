package com.songoda.epicfarming.farming;

import com.craftaro.core.third_party.com.cryptomorin.xseries.XMaterial;
import com.songoda.epicfarming.farming.levels.Level;
import com.songoda.epicfarming.farming.levels.LevelManager;
import com.songoda.epicfarming.settings.Settings;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class FarmManager {
    private final LevelManager levelManager;

    public FarmManager(LevelManager levelManager) {
        this.levelManager = levelManager;
    }

    private final Map<Location, Farm> registeredFarms = new HashMap<>();

    public void addFarm(Location location, Farm farm) {
        this.registeredFarms.put(roundLocation(location), farm);
    }

    public void addFarms(Collection<Farm> farms) {
        for (Farm farm : farms) {
            this.registeredFarms.put(farm.getLocation(), farm);
        }
    }

    public Farm removeFarm(Location location) {
        return this.registeredFarms.remove(roundLocation(location));
    }

    public Farm getFarm(Location location) {
        return this.registeredFarms.get(roundLocation(location));
    }

    public Farm getFarm(Block block) {
        return getFarm(block.getLocation());
    }

    public Farm checkForFarm(Location location) {
        Material farmBlock = Settings.FARM_BLOCK_MATERIAL.getMaterial(XMaterial.END_ROD).parseMaterial();

        Block block = location.getBlock();
        for (Level level : this.levelManager.getLevels().values()) {
            int radius = level.getRadius();
            int bx = block.getX();
            int by = block.getY();
            int bz = block.getZ();

            for (int fx = -radius; fx <= radius; fx++) {
                for (int fy = -2; fy <= 2; fy++) {
                    for (int fz = -radius; fz <= radius; fz++) {
                        Block b2 = block.getWorld().getBlockAt(bx + fx, by + fy, bz + fz);
                        if (b2.getType() == farmBlock) {
                            Farm farm = getFarms().get(b2.getLocation());
                            if (farm == null) {
                                continue;
                            }
                            if (level.getRadius() != getFarm(b2.getLocation()).getLevel().getRadius()) {
                                continue;
                            }
                            return farm;
                        }
                    }
                }
            }
        }
        return null;
    }

    public Map<Location, Farm> getFarms() {
        return Collections.unmodifiableMap(this.registeredFarms);
    }

    private Location roundLocation(Location location) {
        location = location.clone();
        location.setX(location.getBlockX());
        location.setY(location.getBlockY());
        location.setZ(location.getBlockZ());
        return location;
    }
}
