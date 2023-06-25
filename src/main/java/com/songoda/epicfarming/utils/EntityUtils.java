package com.songoda.epicfarming.utils;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class EntityUtils {
    private final Map<CachedChunk, Entity[]> cachedChunks = new HashMap<>();

    public void clearChunkCache() {
        this.cachedChunks.clear();
    }

    private Set<CachedChunk> getNearbyChunks(Location location, double radius, boolean singleChunk) {
        World world = location.getWorld();
        Set<CachedChunk> chunks = new HashSet<>();
        if (world == null) {
            return chunks;
        }

        CachedChunk firstChunk = new CachedChunk(location);
        chunks.add(firstChunk);
        if (singleChunk) {
            return chunks;
        }

        int minX = (int) Math.floor(((location.getX() - radius) - 2.0D) / 16.0D);
        int maxX = (int) Math.floor(((location.getX() + radius) + 2.0D) / 16.0D);
        int minZ = (int) Math.floor(((location.getZ() - radius) - 2.0D) / 16.0D);
        int maxZ = (int) Math.floor(((location.getZ() + radius) + 2.0D) / 16.0D);

        for (int x = minX; x <= maxX; ++x) {
            for (int z = minZ; z <= maxZ; ++z) {
                if (firstChunk.getX() == x && firstChunk.getZ() == z) {
                    continue;
                }

                chunks.add(new CachedChunk(world.getName(), x, z));
            }
        }
        return chunks;
    }

    public List<LivingEntity> getNearbyEntities(Location location, double radius, boolean singleChunk) {
        List<LivingEntity> entities = new ArrayList<>();
        for (CachedChunk chunk : getNearbyChunks(location, radius, singleChunk)) {
            Entity[] entityArray;
            if (this.cachedChunks.containsKey(chunk)) {
                entityArray = this.cachedChunks.get(chunk);
            } else {
                entityArray = chunk.getEntities();
                this.cachedChunks.put(chunk, entityArray);
            }
            for (Entity e : entityArray) {
                if (e.getWorld() != location.getWorld() ||
                        !(e instanceof LivingEntity) ||
                        (!singleChunk && location.distanceSquared(e.getLocation()) >= radius * radius)) {
                    continue;
                }
                entities.add((LivingEntity) e);
            }
        }
        return entities;
    }
}
