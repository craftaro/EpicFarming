package com.songoda.epicfarming.farming;

import com.songoda.epicfarming.EpicFarming;
import com.songoda.epicfarming.api.ILevel;

import java.util.ArrayList;
import java.util.List;

public class Level implements ILevel {

    private int level, costExperiance, costEconomy, radius;

    private double speedMultiplier;

    private boolean autoHarvest, autoReplant;

    private List<String> description = new ArrayList<>();

    public Level(int level, int costExperiance, int costEconomy, double speedMultiplier, int radius, boolean autoHarvest, boolean autoReplant) {
        this.level = level;
        this.costExperiance = costExperiance;
        this.costEconomy = costEconomy;
        this.speedMultiplier = speedMultiplier;
        this.radius = radius;
        this.autoHarvest = autoHarvest;
        this.autoReplant = autoReplant;

        EpicFarming instance = EpicFarming.getInstance();

        description.add(instance.getLocale().getMessage("interface.button.radius", radius));
        description.add(instance.getLocale().getMessage("interface.button.speed", speedMultiplier));

        if (autoHarvest)
            description.add(instance.getLocale().getMessage("interface.button.autoharvest", autoHarvest));

        if (autoReplant)
            description.add(instance.getLocale().getMessage("interface.button.autoreplant", autoReplant));

    }

    @Override
    public List<String> getDescription() {
        return new ArrayList<>(description);
    }

    @Override
    public int getLevel() {
        return level;
    }

    @Override
    public int getRadius() {
        return radius;
    }

    @Override
    public boolean isAutoHarvest() {
        return autoHarvest;
    }

    @Override
    public boolean isAutoReplant() {
        return autoReplant;
    }

    @Override
    public double getSpeedMultiplier() {
        return speedMultiplier;
    }

    @Override
    public int getCostExperiance() {
        return costExperiance;
    }

    @Override
    public int getCostEconomy() {
        return costEconomy;
    }
}
