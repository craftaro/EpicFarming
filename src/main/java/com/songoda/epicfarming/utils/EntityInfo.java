package com.songoda.epicfarming.utils;

import com.songoda.core.compatibility.CompatibleMaterial;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;

public enum EntityInfo {
    CHICKEN(EntityType.CHICKEN, CompatibleMaterial.WHEAT_SEEDS),
    COW(EntityType.COW, CompatibleMaterial.WHEAT),
    PIG(EntityType.PIG, CompatibleMaterial.CARROT),
    SHEEP(EntityType.SHEEP, CompatibleMaterial.WHEAT);

    private final EntityType entityType;
    private final Material material;

    EntityInfo(EntityType entityType, CompatibleMaterial material) {
        this.entityType = entityType;
        this.material = material.getMaterial();
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
