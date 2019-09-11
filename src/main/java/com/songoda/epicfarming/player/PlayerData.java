package com.songoda.epicfarming.player;

import com.songoda.epicfarming.farming.Farm;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class PlayerData {

    private final UUID playerUUID;
    private Farm farm = null;

    PlayerData(UUID playerUUID) {
        this.playerUUID = playerUUID;
    }

    public Farm getFarm() {
        return farm;
    }

    public void setFarm(Farm farm) {
        this.farm = farm;
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(playerUUID);
    }
}
