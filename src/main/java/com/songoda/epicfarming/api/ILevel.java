package com.songoda.epicfarming.api;

import java.util.List;

public interface ILevel {

    List<String> getDescription();

    int getLevel();

    int getRadius();

    boolean isAutoHarvest();

    boolean isAutoReplant();

    double getSpeedMultiplier();

    int getCostExperiance();

    int getCostEconomy();

}
