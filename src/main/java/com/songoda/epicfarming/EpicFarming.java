package com.songoda.epicfarming;

import com.songoda.core.SongodaCore;
import com.songoda.core.SongodaPlugin;
import com.songoda.core.commands.CommandManager;
import com.songoda.core.compatibility.CompatibleMaterial;
import com.songoda.core.configuration.Config;
import com.songoda.core.gui.GuiManager;
import com.songoda.core.hooks.EconomyManager;
import com.songoda.core.hooks.EntityStackerManager;
import com.songoda.epicfarming.boost.BoostData;
import com.songoda.epicfarming.boost.BoostManager;
import com.songoda.epicfarming.commands.*;
import com.songoda.epicfarming.farming.Farm;
import com.songoda.epicfarming.farming.FarmManager;
import com.songoda.epicfarming.farming.FarmType;
import com.songoda.epicfarming.farming.levels.Level;
import com.songoda.epicfarming.farming.levels.LevelManager;
import com.songoda.epicfarming.farming.levels.modules.Module;
import com.songoda.epicfarming.farming.levels.modules.ModuleAutoBreeding;
import com.songoda.epicfarming.farming.levels.modules.ModuleAutoButcher;
import com.songoda.epicfarming.farming.levels.modules.ModuleAutoCollect;
import com.songoda.epicfarming.listeners.BlockListeners;
import com.songoda.epicfarming.listeners.EntityListeners;
import com.songoda.epicfarming.listeners.InteractListeners;
import com.songoda.epicfarming.listeners.UnloadListeners;
import com.songoda.epicfarming.settings.Settings;
import com.songoda.epicfarming.storage.Storage;
import com.songoda.epicfarming.storage.StorageRow;
import com.songoda.epicfarming.storage.types.StorageYaml;
import com.songoda.epicfarming.tasks.FarmTask;
import com.songoda.epicfarming.tasks.GrowthTask;
import com.songoda.epicfarming.tasks.HopperTask;
import com.songoda.epicfarming.utils.EntityUtils;
import com.songoda.epicfarming.utils.Methods;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.PluginManager;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Created by songoda on 1/23/2018.
 */
public class EpicFarming extends SongodaPlugin {

    private static EpicFarming INSTANCE;

    private final Config dataConfig = new Config(this, "data.yml");
    private final Config levelsFile = new Config(this, "levels.yml");

    private final GuiManager guiManager = new GuiManager(this);
    private FarmManager farmManager;
    private LevelManager levelManager;
    private CommandManager commandManager;
    private BoostManager boostManager;

    private GrowthTask growthTask;
    private FarmTask farmTask;

    private EntityUtils entityUtils;

    private Storage storage;

    public static EpicFarming getInstance() {
        return INSTANCE;
    }

    @Override
    public void onPluginLoad() {
        INSTANCE = this;
    }

    @Override
    public void onPluginDisable() {
        saveToFile();
        this.storage.closeConnection();
    }

    @Override
    public void onPluginEnable() {
        // Run Songoda Updater
        SongodaCore.registerPlugin(this, 21, CompatibleMaterial.WHEAT);

        // Load Economy
        EconomyManager.load();

        // Setup Config
        Settings.setupConfig();
        this.setLocale(Settings.LANGUGE_MODE.getString(), false);

        // Set economy preference
        EconomyManager.getManager().setPreferredHook(Settings.ECONOMY_PLUGIN.getString());

        // Load entity stack manager.
        EntityStackerManager.load();

        this.entityUtils = new EntityUtils();

        // Register commands
        this.commandManager = new CommandManager(this);
        this.commandManager.addCommand(new CommandEpicFarming(this))
                .addSubCommands(
                        new CommandBoost(this),
                        new CommandGiveFarmItem(this),
                        new CommandReload(this),
                        new CommandSettings(this)
                );

        dataConfig.load();

        this.storage = new StorageYaml(this);

        this.loadLevelManager();

        this.farmManager = new FarmManager();
        this.boostManager = new BoostManager();

        /*
         * Register Farms into FarmManger from configuration
         */
        Bukkit.getScheduler().runTaskLaterAsynchronously(this, () -> {
            if (storage.containsGroup("farms")) {
                for (StorageRow row : storage.getRowsByGroup("farms")) {
                    Location location = Methods.unserializeLocation(row.getKey());
                    if (location == null || location.getWorld() == null) continue;

                    int level = 1;
                    int configLevel = row.get("level").asInt();
                    if (configLevel > 0) {
                        level = configLevel;
                    }
                    List<ItemStack> items = new ArrayList<ItemStack>();
                    List<ItemStack> configItems = row.get("contents").asItemStackList();
                    if (configItems != null && configItems.size() > 0) {
                        items = configItems;
                    }
                    UUID placedBY = null;
                    String configPlacedBy = row.get("placedby").asString();
                    if (configPlacedBy != null) {
                        placedBY = UUID.fromString(configPlacedBy);
                    }

                    FarmType farmType = FarmType.BOTH;
                    String farmTypeStr = row.get("farmtype").asString();
                    if (farmTypeStr != null)
                        farmType = FarmType.valueOf(farmTypeStr);

                    Farm farm = new Farm(location, levelManager.getLevel(level), placedBY);
                    farm.setFarmType(farmType);
                    farm.setItems(items);
                    Bukkit.getScheduler().runTask(EpicFarming.getInstance(), () ->
                            farmManager.addFarm(location, farm));
                }
            }

            // Adding in Boosts
            if (storage.containsGroup("boosts")) {
                for (StorageRow row : storage.getRowsByGroup("boosts")) {

                    BoostData boostData = new BoostData(
                            row.get("amount").asInt(),
                            Long.parseLong(row.getKey()),
                            UUID.fromString(row.get("player").asString()));

                    Bukkit.getScheduler().runTask(EpicFarming.getInstance(), () -> {
                        this.boostManager.addBoostToPlayer(boostData);
                    });
                }
            }

            // Save data initially so that if the person reloads again fast they don't lose all their data.
            this.saveToFile();
        }, 10);


        // Register Listeners
        guiManager.init();
        PluginManager pluginManager = Bukkit.getPluginManager();
        pluginManager.registerEvents(new EntityListeners(this), this);
        pluginManager.registerEvents(new BlockListeners(this), this);
        pluginManager.registerEvents(new InteractListeners(this), this);
        pluginManager.registerEvents(new UnloadListeners(this), this);

        // Start tasks
        this.growthTask = GrowthTask.startTask(this);
        this.farmTask = FarmTask.startTask(this);

        Bukkit.getScheduler().runTaskLater(this, () -> {
            if (!Bukkit.getPluginManager().isPluginEnabled("EpicHoppers"))
                HopperTask.startTask(this);
        }, 20L);

        // Start auto save
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, this::saveToFile, 6000, 6000);
    }

    @Override
    public void onConfigReload() {
        this.setLocale(Settings.LANGUGE_MODE.getString(), true);
        this.locale.reloadMessages();
        loadLevelManager();
    }

    @Override
    public List<Config> getExtraConfig() {
        return Arrays.asList(levelsFile);
    }

    private void loadLevelManager() {
        if (!new File(this.getDataFolder(), "levels.yml").exists())
            this.saveResource("levels.yml", false);
        levelsFile.load();

        // Load an instance of LevelManager
        levelManager = new LevelManager();

        /*
         * Register Levels into LevelManager from configuration.
         */
        for (String levelName : levelsFile.getKeys(false)) {
            ConfigurationSection levels = levelsFile.getConfigurationSection(levelName);

            if (levels.get("Auto-Harvest") != null) {
                levels.set("Auto-Collect", levels.getBoolean("Auto-Harvest"));
                levels.set("Auto-Harvest", null);
            }

            int level = Integer.parseInt(levelName.split("-")[1]);
            int costExperiance = levels.getInt("Cost-xp");
            int costEconomy = levels.getInt("Cost-eco");
            int radius = levels.getInt("Radius");
            double speedMultiplier = levels.getDouble("Speed-Multiplier");
            boolean autoCollect = levels.getBoolean("Auto-Collect");
            boolean autoReplant = levels.getBoolean("Auto-Replant");
            int pages = levels.getInt("Pages", 1);

            if (levels.get("Auto-Breeding") instanceof Boolean) {
                levels.set("Auto-Breeding", 15);
            }

            ArrayList<Module> modules = new ArrayList<>();

            for (String key : levels.getKeys(false)) {
                if (key.equals("Auto-Breeding") && levels.getInt("Auto-Breeding") != 0) {
                    modules.add(new ModuleAutoBreeding(this, levels.getInt("Auto-Breeding")));
                } else if (key.equals("Auto-Butcher") && levels.getInt("Auto-Butcher") != 0) {
                    modules.add(new ModuleAutoButcher(this, levels.getInt("Auto-Butcher")));
                } else if (key.equals("Auto-Collect")) {
                    modules.add(new ModuleAutoCollect(this));
                }
            }
            levelManager.addLevel(level, costExperiance, costEconomy, speedMultiplier, radius, autoCollect, autoReplant, pages, modules);
        }
        levelsFile.saveChanges();
    }

    /*
     * Saves registered farms to file.
     */
    private void saveToFile() {
        if (levelManager != null) {
            for (Level level : levelManager.getLevels().values())
                for (Module module : level.getRegisteredModules())
                    module.saveDataToFile();
        }

        storage.doSave();
    }

    public int getLevelFromItem(ItemStack item) {
        if (!item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) return 0;
        if (item.getItemMeta().getDisplayName().contains(":")) {
            return NumberUtils.toInt(item.getItemMeta().getDisplayName().replace("\u00A7", "").split(":")[0], 0);
        }
        return 0;
    }

    public ItemStack makeFarmItem(Level level) {
        ItemStack item = Settings.FARM_BLOCK_MATERIAL.getMaterial().getItem();
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(Methods.formatText(Methods.formatName(level.getLevel(), true)));
        String line = getLocale().getMessage("general.nametag.lore").getMessage();
        if (!line.equals("")) meta.setLore(Arrays.asList(line));
        item.setItemMeta(meta);
        return item;
    }

    public FarmManager getFarmManager() {
        return farmManager;
    }

    public LevelManager getLevelManager() {
        return levelManager;
    }

    public CommandManager getCommandManager() {
        return commandManager;
    }

    public BoostManager getBoostManager() {
        return boostManager;
    }

    public GrowthTask getGrowthTask() {
        return growthTask;
    }

    public FarmTask getFarmTask() {
        return farmTask;
    }

    public GuiManager getGuiManager() {
        return guiManager;
    }

    public EntityUtils getEntityUtils() {
        return entityUtils;
    }
}