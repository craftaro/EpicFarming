package com.songoda.epicfarming.farming;

import com.songoda.epicfarming.api.farming.Level;
import com.songoda.epicfarming.api.farming.LevelManager;

import java.util.Collections;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

public class ELevelManager implements LevelManager {

    private final NavigableMap<Integer, ELevel> registeredLevels = new TreeMap<>();

    @Override
    public void addLevel(int level, int costExperiance, int costEconomy, double speedMultiplier, int radius, boolean autoHarvest, boolean autoReplant) {
        registeredLevels.put(level, new ELevel(level, costExperiance, costEconomy, speedMultiplier, radius, autoHarvest, autoReplant));
    }

    @Override
    public ELevel getLevel(int level) {
        return registeredLevels.get(level);
    }

    @Override
    public ELevel getLowestLevel() {
        return registeredLevels.firstEntry().getValue();
    }

    @Override
    public ELevel getHighestLevel() {
        return registeredLevels.lastEntry().getValue();
    }

    @Override
    public boolean isLevel(int level) {
        return registeredLevels.containsKey(level);
    }

    @Override
    public Map<Integer, Level> getLevels() {
        return Collections.unmodifiableMap(registeredLevels);
    }

    @Override
    public void clear() {
        registeredLevels.clear();
    }
}
