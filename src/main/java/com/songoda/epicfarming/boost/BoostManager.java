package com.songoda.epicfarming.boost;

import com.songoda.epicfarming.EpicFarming;

import java.util.*;

public class BoostManager {

    private final Set<BoostData> registeredBoosts = new HashSet<>();

    public void addBoostToPlayer(BoostData data) {
        this.registeredBoosts.add(data);
    }

    public void removeBoostFromPlayer(BoostData data) {
        this.registeredBoosts.remove(data);
    }

    public void addBoosts(Collection<BoostData> boosts) {
        for (BoostData boost : boosts)
            this.registeredBoosts.add(boost);
    }

    public Set<BoostData> getBoosts() {
        return Collections.unmodifiableSet(registeredBoosts);
    }

    public BoostData getBoost(UUID player) {
        if (player == null) return null;
        for (BoostData boostData : registeredBoosts) {
            if (boostData.getPlayer().toString().equals(player.toString())) {
                if (System.currentTimeMillis() >= boostData.getEndTime()) {
                    EpicFarming.getInstance().getDataManager().deleteBoost(boostData);
                    removeBoostFromPlayer(boostData);
                }
                return boostData;
            }
        }
        return null;
    }
}
