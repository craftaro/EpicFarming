package com.songoda.epicfarming.storage;

import com.songoda.arconix.api.utils.ConfigWrapper;
import com.songoda.epicfarming.EpicFarmingPlugin;
import com.songoda.epicfarming.api.EpicFarming;

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

    public abstract void doSave();

    public abstract void closeConnection();

}
