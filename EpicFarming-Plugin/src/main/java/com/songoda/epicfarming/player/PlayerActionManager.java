package com.songoda.epicfarming.player;

import org.bukkit.entity.Player;

import java.util.*;

public class PlayerActionManager {

    private final Map<UUID, PlayerData> registeredPlayers = new HashMap<>();

    public PlayerData getPlayerAction(Player player) {
        return (player != null) ? registeredPlayers.computeIfAbsent(player.getUniqueId(), p -> new PlayerData(player.getUniqueId())) : null;
    }

    public Collection<PlayerData> getRegisteredPlayers() {
        return Collections.unmodifiableCollection(registeredPlayers.values());
    }
}