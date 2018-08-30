package com.songoda.epicfarming.player;

import com.songoda.epicfarming.farming.EFarm;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class PlayerData {

    private final UUID playerUUID;
    private EFarm farm = null;

    public PlayerData(UUID playerUUID) {
        this.playerUUID = playerUUID;
    }

    public EFarm getFarm() {
        return farm;
    }

    public void setFarm(EFarm farm) {
        this.farm = farm;
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(playerUUID);
    }
}
