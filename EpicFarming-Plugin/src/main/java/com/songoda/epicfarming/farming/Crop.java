package com.songoda.epicfarming.farming;

import com.songoda.epicfarming.api.farming.Farm;
import org.bukkit.Location;

public class Crop {

    private Location location;

    private int ticksLived = 1;
    private Farm farm;

    public Crop(Location location, Farm farm) {
        this.location = location;
        this.farm = farm;
    }

    public int getTicksLived() {
        return ticksLived;
    }

    public void setTicksLived(int ticksLived) {
        this.ticksLived = ticksLived;
    }

    public Farm getFarm() {
        return farm;
    }

    public Location getLocation() {
        return location;
    }
}
