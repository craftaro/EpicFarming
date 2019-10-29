package com.songoda.epicfarming.farming.levels.modules;

import com.songoda.core.configuration.Config;
import com.songoda.epicfarming.EpicFarming;
import com.songoda.epicfarming.farming.Farm;
import com.songoda.epicfarming.utils.Methods;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public abstract class Module {

    private static final Map<String, Config> configs = new HashMap<>();

    protected final EpicFarming plugin;
    private final Config config;
    private final Map<Farm, Integer> currentTicks = new HashMap<>();

    public Module(EpicFarming plugin) {
        this.plugin = plugin;
        if (!configs.containsKey(getName())) {
            Config config = new Config(plugin, File.separator + "modules", getName() + ".yml");
            configs.put(getName(), config);
            config.load();

        }
        this.config = configs.get(getName());
    }

    public abstract String getName();

    public abstract int runEveryXTicks();

    public void run(Farm farm, Collection<LivingEntity> entitiesAroundFarm) {
        if (!currentTicks.containsKey(farm))
            currentTicks.put(farm, 1);
        int currentTick = currentTicks.get(farm);
        if (currentTick >= runEveryXTicks()) {
            runFinal(farm, entitiesAroundFarm);
            currentTick = 0;
        }
        currentTicks.put(farm, currentTick + 1);
    }

    public abstract void runFinal(Farm farm, Collection<LivingEntity> entitiesAroundFarm);

    public abstract ItemStack getGUIButton(Farm farm);

    public abstract void runButtonPress(Player player, Farm farm, ClickType type);

    public abstract String getDescription();

    public void saveData(Farm farm, String setting, Object value) {
        saveData(farm, setting, value, value);
    }

    public void saveData(Farm farm, String setting, Object value, Object toCache) {
        config.set("data." + Methods.serializeLocation(farm.getLocation()) + "." + setting, value);
        modifyDataCache(farm, setting, toCache);
    }

    public void modifyDataCache(Farm farm, String setting, Object value) {
        farm.addDataToModuleCache(getName() + "." + setting, value);
    }

    protected Object getData(Farm farm, String setting) {
        String cacheStr = getName() + "." + setting;
        if (farm.isDataCachedInModuleCache(cacheStr))
            return farm.getDataFromModuleCache(cacheStr);

        Object data = config.get("data." + Methods.serializeLocation(farm.getLocation()) + "." + setting);
        modifyDataCache(farm, setting, data);
        return data;
    }

    public void clearData(Farm farm) {
        config.set("data." + Methods.serializeLocation(farm.getLocation()), null);
        farm.clearModuleCache();
    }

    public void saveDataToFile() {
        config.save();
    }
}
