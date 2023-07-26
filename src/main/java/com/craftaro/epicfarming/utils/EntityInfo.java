package com.craftaro.epicfarming.utils;

import com.craftaro.core.third_party.com.cryptomorin.xseries.XMaterial;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;

public enum EntityInfo {
    CHICKEN(EntityType.CHICKEN, XMaterial.WHEAT_SEEDS),
    COW(EntityType.COW, XMaterial.WHEAT),
    PIG(EntityType.PIG, XMaterial.CARROT),
    SHEEP(EntityType.SHEEP, XMaterial.WHEAT);

    private final EntityType entityType;
    private final Material material;

    EntityInfo(EntityType entityType, XMaterial material) {
        this.entityType = entityType;
        this.material = material.parseMaterial();
    }

    public static EntityInfo of(EntityType entityType) {
        for (EntityInfo entityInfo : EntityInfo.values()) {
            if (entityInfo.entityType == entityType) {
                return entityInfo;
            }
        }

        return null;
    }

    public EntityType getEntityType() {
        return this.entityType;
    }

    public Material getMaterial() {
        return this.material;
    }
}
