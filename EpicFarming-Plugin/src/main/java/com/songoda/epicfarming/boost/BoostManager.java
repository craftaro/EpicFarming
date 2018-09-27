package com.songoda.epicfarming.boost;

import com.songoda.epicfarming.EpicFarmingPlugin;
import org.bukkit.Bukkit;

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

    public Set<BoostData> getBoosts() {
        return Collections.unmodifiableSet(registeredBoosts);
    }

    public BoostData getBoost(UUID player) {
        if (player == null) return null;
        for (BoostData boostData : registeredBoosts) {
            if (boostData.getPlayer().toString().equals(player.toString())) {
                if (System.currentTimeMillis() >= boostData.getEndTime()) {
                    Bukkit.getScheduler().scheduleSyncDelayedTask(EpicFarmingPlugin.getInstance(), () -> removeBoostFromPlayer(boostData), 1);
                }
                return boostData;
            }
        }
        return null;
    }
}
