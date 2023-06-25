package com.songoda.epicfarming.farming;

import org.bukkit.Location;

public class Crop {
    private final Location location;

    private int ticksLived = 1;
    private final Farm farm;

    public Crop(Location location, Farm farm) {
        this.location = location;
        this.farm = farm;
    }

    public int getTicksLived() {
        return this.ticksLived;
    }

    public void setTicksLived(int ticksLived) {
        this.ticksLived = ticksLived;
    }

    public Farm getFarm() {
        return this.farm;
    }

    public Location getLocation() {
        return this.location;
    }
}
