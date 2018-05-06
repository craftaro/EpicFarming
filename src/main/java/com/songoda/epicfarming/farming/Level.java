package com.songoda.epicfarming.farming;

import com.songoda.epicfarming.Lang;

import java.util.ArrayList;

public class Level {

    private int level, costExperiance, costEconomy, radius;

    private double speedMultiplier;

    private boolean autoHarvest, autoReplant;

    private ArrayList<String> description = new ArrayList<>();

    public Level(int level, int costExperiance, int costEconomy, double speedMultiplier, int radius, boolean autoHarvest, boolean autoReplant) {
        this.level = level;
        this.costExperiance = costExperiance;
        this.costEconomy = costEconomy;
        this.speedMultiplier = speedMultiplier;
        this.radius = radius;
        this.autoHarvest = autoHarvest;
        this.autoReplant = autoReplant;

        description.add(Lang.NEXT_RADIUS.getConfigValue(radius));
        description.add(Lang.NEXT_SPEED.getConfigValue(speedMultiplier));

        if (autoHarvest)
            description.add(Lang.NEXT_AUTO_HARVEST.getConfigValue(autoHarvest));

        if (autoReplant)
            description.add(Lang.NEXT_AUTO_REPLANT.getConfigValue(autoReplant));

    }

    public ArrayList<String> getDescription() {
        return (ArrayList<String>)description.clone();
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
