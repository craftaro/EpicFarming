package com.songoda.epicfarming.boost;

import com.songoda.epicfarming.EpicFarming;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class BoostManager {
    private final Set<BoostData> registeredBoosts = new HashSet<>();

    public void addBoostToPlayer(BoostData data) {
        this.registeredBoosts.add(data);
    }

    public void removeBoostFromPlayer(BoostData data) {
        this.registeredBoosts.remove(data);
    }

    public void addBoosts(Collection<BoostData> boosts) {
        this.registeredBoosts.addAll(boosts);
    }

    public Set<BoostData> getBoosts() {
        return Collections.unmodifiableSet(this.registeredBoosts);
    }

    public BoostData getBoost(UUID player) {
        if (player == null) {
            return null;
        }

        for (BoostData boostData : this.registeredBoosts) {
            if (boostData.getPlayer().toString().equals(player.toString())) {
                if (System.currentTimeMillis() >= boostData.getEndTime()) {
                    EpicFarming.getPlugin(EpicFarming.class).getDataManager().delete(boostData);
                    removeBoostFromPlayer(boostData);
                }
                return boostData;
            }
        }

        return null;
    }
}
