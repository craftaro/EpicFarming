package com.songoda.epicfarming.farming.levels;

import com.songoda.epicfarming.farming.levels.modules.Module;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

public class LevelManager {

    private final NavigableMap<Integer, Level> registeredLevels = new TreeMap<>();

    public void addLevel(int level, int costExperiance, int costEconomy, double speedMultiplier, int radius, boolean autoCollect, boolean autoReplant, int pages, ArrayList<Module> modules) {
        registeredLevels.put(level, new Level(level, costExperiance, costEconomy, speedMultiplier, radius, autoReplant, pages, modules));
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

    public boolean isLevel(int level) {
        return registeredLevels.containsKey(level);
    }

    public Map<Integer, Level> getLevels() {
        return Collections.unmodifiableMap(registeredLevels);
    }

    public void clear() {
        registeredLevels.clear();
    }
}
