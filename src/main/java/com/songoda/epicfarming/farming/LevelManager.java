package com.songoda.epicfarming.farming;

import java.util.Collections;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

public class LevelManager {

    private final NavigableMap<Integer, Level> registeredLevels = new TreeMap<>();

    public void addLevel(int level, int costExperiance, int costEconomy, double speedMultiplier, int radius, boolean autoHarvest, boolean autoReplant) {
        registeredLevels.put(level, new Level(level, costExperiance, costEconomy, speedMultiplier, radius, autoHarvest, autoReplant));
    }

    public Level getLevel(int level) {
        return registeredLevels.get(level);
    }

    public Level getLowestLevel() {
        return registeredLevels.firstEntry().getValue();
    }

    public Level getHighestLevel() {
        return registeredLevels.lastEntry().getValue();
    }
    public Map<Integer, Level> getLevels() {
        return Collections.unmodifiableMap(registeredLevels);
    }
}
