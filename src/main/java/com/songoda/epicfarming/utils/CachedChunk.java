package com.songoda.epicfarming.utils;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;

import java.util.Objects;

public class CachedChunk {
    private final String world;
    private final int x;
    private final int z;

    public CachedChunk(Chunk chunk) {
        this(chunk.getWorld().getName(), chunk.getX(), chunk.getZ());
    }

    public CachedChunk(Location location) {
        this(location.getWorld().getName(), (int) location.getX() >> 4, (int) location.getZ() >> 4);
    }

    public CachedChunk(String world, int x, int z) {
        this.world = world;
        this.x = x;
        this.z = z;
    }

    public String getWorld() {
        return this.world;
    }

    public int getX() {
        return this.x;
    }

    public int getZ() {
        return this.z;
    }

    public Chunk getChunk() {
        World world = Bukkit.getWorld(this.world);
        if (world == null) {
            return null;
        }
        return world.getChunkAt(this.x, this.z);
    }

    public Entity[] getEntities() {
        World world = Bukkit.getWorld(this.world);
        if (world == null || !world.isChunkLoaded(this.x, this.z)) {
            return new Entity[0];
        }
        return getChunk().getEntities();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Chunk) {
            Chunk other = (Chunk) o;
            return this.world.equals(other.getWorld().getName()) && this.x == other.getX() && this.z == other.getZ();
        }
        if (o instanceof CachedChunk) {
            CachedChunk other = (CachedChunk) o;
            return this.world.equals(other.getWorld()) && this.x == other.getX() && this.z == other.getZ();
        }

        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.world, this.x, this.z);
    }
}
