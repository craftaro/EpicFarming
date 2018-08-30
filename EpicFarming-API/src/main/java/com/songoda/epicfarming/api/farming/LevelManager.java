package com.songoda.epicfarming.api.farming;

import java.util.Map;

public interface LevelManager {

    void addLevel(int level, int costExperiance, int costEconomy, double speedMultiplier, int radius, boolean autoHarvest, boolean autoReplant);

    Level getLevel(int level);

    Level getLowestLevel();

    Level getHighestLevel();

    boolean isLevel(int level);

    Map<Integer, Level> getLevels();

    void clear();
}
