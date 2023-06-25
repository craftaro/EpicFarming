package com.songoda.epicfarming.farming.levels.modules;

import com.songoda.core.configuration.Config;
import com.songoda.epicfarming.EpicFarming;
import com.songoda.epicfarming.farming.Farm;
import com.songoda.epicfarming.utils.Methods;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Module {
    private static final Map<String, Config> CONFIGS = new HashMap<>();

    protected final EpicFarming plugin;
    private final Config config;
    private final Map<Farm, Integer> currentTicks = new HashMap<>();

    public Module(EpicFarming plugin) {
        this.plugin = plugin;
        if (!CONFIGS.containsKey(getName())) {
            Config config = new Config(plugin, File.separator + "modules", getName() + ".yml");
            CONFIGS.put(getName(), config);
            config.load();

        }
        this.config = CONFIGS.get(getName());
    }

    public abstract String getName();

    public abstract int runEveryXTicks();

    public void run(Farm farm, Collection<LivingEntity> entitiesAroundFarm, List<Block> crops) {
        if (!this.currentTicks.containsKey(farm)) {
            this.currentTicks.put(farm, 1);
        }

        int currentTick = this.currentTicks.get(farm);
        if (currentTick >= runEveryXTicks()) {
            runFinal(farm, entitiesAroundFarm, crops);
            currentTick = 0;
        }
        this.currentTicks.put(farm, currentTick + 1);
    }

    public abstract void runFinal(Farm farm, Collection<LivingEntity> entitiesAroundFarm, List<Block> crops);

    public abstract ItemStack getGUIButton(Farm farm);

    public abstract void runButtonPress(Player player, Farm farm, ClickType type);

    public abstract String getDescription();

    public void saveData(Farm farm, String setting, Object toCache) {
        saveData(farm, setting, toCache, toCache);
    }

    public void saveData(Farm farm, String setting, Object value, Object toCache) {
        this.config.set("data." + Methods.serializeLocation(farm.getLocation()) + "." + setting, value);
        modifyDataCache(farm, setting, toCache);
    }

    public void modifyDataCache(Farm farm, String setting, Object value) {
        farm.addDataToModuleCache(getName() + "." + setting, value);
    }

    protected Object getData(Farm farm, String setting) {
        String cacheStr = getName() + "." + setting;
        if (farm.isDataCachedInModuleCache(cacheStr)) {
            return farm.getDataFromModuleCache(cacheStr);
        }

        Object data = this.config.get("data." + Methods.serializeLocation(farm.getLocation()) + "." + setting);
        modifyDataCache(farm, setting, data);
        return data;
    }

    public void clearData(Farm farm) {
        this.config.set("data." + Methods.serializeLocation(farm.getLocation()), null);
        farm.clearModuleCache();
    }

    public void saveDataToFile() {
        this.config.save();
    }
}
