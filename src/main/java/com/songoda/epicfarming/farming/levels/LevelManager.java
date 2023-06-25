package com.songoda.epicfarming.farming.levels;

import com.songoda.epicfarming.farming.levels.modules.Module;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

public class LevelManager {

    private final NavigableMap<Integer, Level> registeredLevels = new TreeMap<>();

    public void addLevel(int level, int costExperience, int costEconomy, double speedMultiplier, int radius, boolean autoCollect, boolean autoReplant, int pages, ArrayList<Module> modules) {
        this.registeredLevels.put(level, new Level(level, costExperience, costEconomy, speedMultiplier, radius, autoReplant, pages, modules));
    }

    public Level getLevel(int level) {
        return this.registeredLevels.get(level);
    }

    public Level getLowestLevel() {
        return this.registeredLevels.firstEntry().getValue();
    }

    public Level getHighestLevel() {
        return this.registeredLevels.lastEntry().getValue();
    }

    public boolean isLevel(int level) {
        return this.registeredLevels.containsKey(level);
    }

    public Map<Integer, Level> getLevels() {
        return Collections.unmodifiableMap(this.registeredLevels);
    }

    public void clear() {
        this.registeredLevels.clear();
    }
}
