package com.songoda.epicfarming.storage;

import com.songoda.core.configuration.Config;
import com.songoda.epicfarming.EpicFarming;
import com.songoda.epicfarming.boost.BoostData;
import com.songoda.epicfarming.farming.Farm;
import com.songoda.epicFarming.EpicFarming;
import com.songoda.epicFarming.boost.BoostData;
import com.songoda.epicFarming.furnace.Furnace;
import com.songoda.epicFarming.utils.Methods;
import com.songoda.epicfarming.utils.Methods;

import java.util.List;

public abstract class Storage {

    protected final EpicFarming plugin;
    protected final Config dataFile;

    public Storage(EpicFarming plugin) {
        this.plugin = plugin;
        this.dataFile = new Config(plugin, "data.yml");
        this.dataFile.load();
    }

    public abstract boolean containsGroup(String group);

    public abstract List<StorageRow> getRowsByGroup(String group);

    public abstract void prepareSaveItem(String group, StorageItem... items);

    public void updateData(EpicFarming instance) {
        /*
         * Dump FarmManager to file.
         */
        for (Farm farm : instance.getFarmManager().getFarms().values()) {
            if (farm.getLocation() == null
                    || farm.getLocation().getWorld() == null) continue;
            String locstr = Methods.serializeLocation(farm.getLocation());
            prepareSaveItem("farms",new StorageItem("location",locstr),
                    new StorageItem("level",farm.getLevel().getLevel()),
                    new StorageItem("placedby",farm.getPlacedBy().toString()),
                    new StorageItem("contents",((Farm)farm).dumpInventory()));
        }

        /*
         * Dump BoostManager to file.
         */
        for (BoostData boostData : instance.getBoostManager().getBoosts()) {
            String endTime = String.valueOf(boostData.getEndTime());
            prepareSaveItem("boosts",new StorageItem("endtime",endTime),
                    new StorageItem("amount",boostData.getMultiplier()),
                    new StorageItem("player",boostData.getPlayer()));
        }
    }

    public abstract void doSave();

    public abstract void save();

    public abstract void makeBackup();

    public abstract void closeConnection();

}