package com.songoda.epicfarming.farming;

import com.songoda.epicfarming.EpicFarming;

import java.util.ArrayList;
import java.util.List;

public class Level {

    private int level, costExperiance, costEconomy, radius;

    private double speedMultiplier;

    private boolean autoHarvest, autoReplant, autoBreeding;

    private List<String> description = new ArrayList<>();

    Level(int level, int costExperiance, int costEconomy, double speedMultiplier, int radius, boolean autoHarvest, boolean autoReplant, boolean autoBreeding) {
        this.level = level;
        this.costExperiance = costExperiance;
        this.costEconomy = costEconomy;
        this.speedMultiplier = speedMultiplier;
        this.radius = radius;
        this.autoHarvest = autoHarvest;
        this.autoReplant = autoReplant;
        this.autoBreeding = autoBreeding;

        EpicFarming instance = EpicFarming.getInstance();

        description.add(instance
                .getLocale()
                .getMessage("interface.button.radius",
                        radius));
        description.add(instance.getLocale().getMessage("interface.button.speed", speedMultiplier));

        if (autoHarvest)
            description.add(instance.getLocale().getMessage("interface.button.autoharvest", autoHarvest));

        if (autoReplant)
            description.add(instance.getLocale().getMessage("interface.button.autoreplant", autoReplant));

        if (autoBreeding)
            description.add(instance.getLocale().getMessage("interface.button.autobreeding", autoBreeding));

    }

    public List<String> getDescription() {
        return new ArrayList<>(description);
    }

    public int getLevel() {
        return level;
    }

    public int getRadius() {
        return radius;
    }

    public boolean isAutoHarvest() {
        return autoHarvest;
    }

    public boolean isAutoReplant() {
        return autoReplant;
    }

    public boolean isAutoBreeding() {
        return autoBreeding;
    }

    public double getSpeedMultiplier() {
        return speedMultiplier;
    }

    public int getCostExperiance() {
        return costExperiance;
    }

    public int getCostEconomy() {
        return costEconomy;
    }
}
