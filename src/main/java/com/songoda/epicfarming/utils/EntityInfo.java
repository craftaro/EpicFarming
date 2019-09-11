package com.songoda.epicfarming.utils;

import org.bukkit.Material;

public enum EntityInfo {

    CHICKEN(Material.WHEAT_SEEDS),
    COW(Material.WHEAT),
    PIG(Material.CARROT),
    SHEEP(Material.WHEAT);

    Material material;

    EntityInfo(Material material) {
        this.material = material;
    }

    public Material getMaterial() {
        return material;
    }
}
