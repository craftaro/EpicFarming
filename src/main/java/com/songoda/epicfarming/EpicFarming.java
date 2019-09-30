package com.songoda.epicfarming;

import com.songoda.core.SongodaCore;
import com.songoda.core.SongodaPlugin;
import com.songoda.core.commands.CommandManager;
import com.songoda.core.compatibility.CompatibleMaterial;
import com.songoda.core.configuration.Config;
import com.songoda.core.gui.GuiManager;
import com.songoda.core.hooks.EconomyManager;
import com.songoda.epicfarming.boost.BoostData;
import com.songoda.epicfarming.boost.BoostManager;
import com.songoda.epicfarming.commands.*;
import com.songoda.epicfarming.farming.Farm;
import com.songoda.epicfarming.farming.FarmManager;
import com.songoda.epicfarming.farming.Level;
import com.songoda.epicfarming.farming.LevelManager;
import com.songoda.epicfarming.listeners.BlockListeners;
import com.songoda.epicfarming.listeners.InteractListeners;
import com.songoda.epicfarming.settings.Setting;
import com.songoda.epicfarming.storage.Storage;
import com.songoda.epicfarming.storage.StorageRow;
import com.songoda.epicfarming.storage.types.StorageYaml;
import com.songoda.epicfarming.tasks.EntityTask;
import com.songoda.epicfarming.tasks.FarmTask;
import com.songoda.epicfarming.tasks.GrowthTask;
import com.songoda.epicfarming.tasks.HopperTask;
import com.songoda.epicfarming.utils.Methods;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.PluginManager;

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
    private EntityTask entityTask;

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
        Setting.setupConfig();
        this.setLocale(Setting.LANGUGE_MODE.getString(), false);

        // Set economy preference
        EconomyManager.getManager().setPreferredHook(Setting.ECONOMY_PLUGIN.getString());

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
        Bukkit.getScheduler().runTaskLater(this, () -> {
            if (storage.containsGroup("farms")) {
                for (StorageRow row : storage.getRowsByGroup("farms")) {
                    Location location = Methods.unserializeLocation(row.getKey());
                    if (location == null || location.getWorld() == null) return;

                    int level = row.get("level").asInt();
                    List<ItemStack> items = row.get("contents").asItemStackList();
                    UUID placedBY = UUID.fromString(row.get("placedby").asString());
                    Farm farm = new Farm(location, levelManager.getLevel(level), placedBY);
                    farm.setItems(items);
                    farmManager.addFarm(location, farm);
                }
            }

            // Adding in Boosts
            if (storage.containsGroup("boosts")) {
                for (StorageRow row : storage.getRowsByGroup("boosts")) {
                    if (row.getItems().get("uuid").asObject() != null)
                        continue;

                    BoostData boostData = new BoostData(
                            row.get("amount").asInt(),
                            Long.parseLong(row.getKey()),
                            UUID.fromString(row.get("player").asString()));

                    this.boostManager.addBoostToPlayer(boostData);
                }
            }

            // Save data initially so that if the person reloads again fast they don't lose all their data.
            this.saveToFile();
        }, 10);


        // Register Listeners
        guiManager.init();
        PluginManager pluginManager = Bukkit.getPluginManager();
        pluginManager.registerEvents(new BlockListeners(this), this);
        pluginManager.registerEvents(new InteractListeners(this), this);

        // Start tasks
        this.growthTask = GrowthTask.startTask(this);
        this.farmTask = FarmTask.startTask(this);
        this.entityTask = EntityTask.startTask(this);

        Bukkit.getScheduler().runTaskLater(this, () -> {
            if (!Bukkit.getPluginManager().isPluginEnabled("EpicHoppers"))
                HopperTask.startTask(this);
        }, 20L);

        // Start auto save
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, this::saveToFile, 6000, 6000);
    }

    @Override
    public void onConfigReload() {
        this.setLocale(getConfig().getString("System.Language Mode"), true);
        this.locale.reloadMessages();
        loadLevelManager();
    }

    @Override
    public List<Config> getExtraConfig() {
        return Arrays.asList(levelsFile);
    }

    private void loadLevelManager() {
        if (!levelsFile.getFile().exists())
            this.saveResource("levels.yml", false);
        levelsFile.load();

        // Load an instance of LevelManager
        levelManager = new LevelManager();

        /*
         * Register Levels into LevelManager from configuration.
         */
        for (String levelName : levelsFile.getKeys(false)) {
            ConfigurationSection levels = levelsFile.getConfigurationSection(levelName);
            
            int level = Integer.valueOf(levelName.split("-")[1]);
            int costExperiance = levels.getInt("Cost-xp");
            int costEconomy = levels.getInt("Cost-eco");
            int radius = levels.getInt("Radius");
            double speedMultiplier = levels.getDouble("Speed-Multiplier");
            boolean autoHarvest = levels.getBoolean("Auto-Harvest");
            boolean autoReplant = levels.getBoolean("Auto-Replant");
            boolean autoBreeding = levels.getBoolean("Auto-Breeding");
            levelManager.addLevel(level, costExperiance, costEconomy, speedMultiplier, radius, autoHarvest, autoReplant, autoBreeding);
        }
    }

    /*
     * Saves registered farms to file.
     */
    private void saveToFile() {
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
        ItemStack item = Setting.FARM_BLOCK_MATERIAL.getMaterial().getItem();
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

    public EntityTask getEntityTask() {
        return entityTask;
    }

    public GuiManager getGuiManager() {
        return guiManager;
    }
}