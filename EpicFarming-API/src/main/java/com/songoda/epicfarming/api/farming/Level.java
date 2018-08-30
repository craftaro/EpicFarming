package com.songoda.epicfarming.api.farming;

import java.util.List;

public interface Level {
    List<String> getDescription();

    int getLevel();

    int getRadius();

    boolean isAutoHarvest();

    boolean isAutoReplant();

    double getSpeedMultiplier();

    int getCostExperiance();

    int getCostEconomy();
}
