package com.songoda.epicfarming.storage;

import com.songoda.epicfarming.api.farming.Farm;
import com.songoda.epicfarming.boost.BoostData;
import com.songoda.epicfarming.farming.EFarm;
import com.songoda.epicfarming.utils.Methods;
import com.songoda.epicfarming.EpicFarmingPlugin;
import com.songoda.epicfarming.utils.ConfigWrapper;
import com.songoda.epicfarming.utils.Methods;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

public abstract class Storage {

    protected final EpicFarmingPlugin instance;
    protected final ConfigWrapper dataFile;

    public Storage(EpicFarmingPlugin instance) {
        this.instance = instance;
        this.dataFile = new ConfigWrapper(instance, "", "data.yml");
        this.dataFile.createNewFile(null, "EpicFarming Data File");
        this.dataFile.getConfig().options().copyDefaults(true);
        this.dataFile.saveConfig();
    }

    public abstract boolean containsGroup(String group);

    public abstract List<StorageRow> getRowsByGroup(String group);

    public abstract void prepareSaveItem(String group, StorageItem... items);

    public void updateData(EpicFarmingPlugin instance) {
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
                        new StorageItem("contents",((EFarm)farm).dumpInventory()));
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
