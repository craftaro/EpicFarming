package com.songoda.epicfarming;

import com.songoda.epicfarming.api.EpicFarming;
import com.songoda.epicfarming.api.farming.Level;
import com.songoda.epicfarming.boost.BoostData;
import com.songoda.epicfarming.boost.BoostManager;
import com.songoda.epicfarming.command.CommandManager;
import com.songoda.epicfarming.farming.EFarm;
import com.songoda.epicfarming.farming.EFarmManager;
import com.songoda.epicfarming.farming.ELevelManager;
import com.songoda.epicfarming.hook.HookManager;
import com.songoda.epicfarming.listeners.BlockListeners;
import com.songoda.epicfarming.listeners.InteractListeners;
import com.songoda.epicfarming.listeners.InventoryListeners;
import com.songoda.epicfarming.player.PlayerActionManager;
import com.songoda.epicfarming.player.PlayerData;
import com.songoda.epicfarming.storage.Storage;
import com.songoda.epicfarming.storage.StorageRow;
import com.songoda.epicfarming.storage.types.StorageMysql;
import com.songoda.epicfarming.storage.types.StorageYaml;
import com.songoda.epicfarming.tasks.EntityTask;
import com.songoda.epicfarming.tasks.FarmTask;
import com.songoda.epicfarming.tasks.GrowthTask;
import com.songoda.epicfarming.tasks.HopperTask;
import com.songoda.epicfarming.utils.*;
import com.songoda.epicfarming.utils.updateModules.LocaleModule;
import com.songoda.update.Plugin;
import com.songoda.update.SongodaUpdate;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Created by songoda on 1/23/2018.
 */
public class EpicFarmingPlugin extends JavaPlugin implements EpicFarming {

    private static EpicFarmingPlugin INSTANCE;

    private SettingsManager settingsManager;
    private References references;
    private ConfigWrapper hooksFile = new ConfigWrapper(this, "", "hooks.yml");
    private ConfigWrapper dataFile = new ConfigWrapper(this, "", "data.yml");
    private Locale locale;
    private EFarmManager farmManager;
    private ELevelManager levelManager;
    private PlayerActionManager playerActionManager;
    private CommandManager commandManager;
    private BoostManager boostManager;
    private HookManager hookManager;

    private GrowthTask growthTask;
    private FarmTask farmTask;
    private EntityTask entityTask;

    private Storage storage;

    public static EpicFarmingPlugin getInstance() {
        return INSTANCE;
    }

    private boolean checkVersion() {
        int workingVersion = 13;
        int currentVersion = Integer.parseInt(Bukkit.getServer().getClass()
                .getPackage().getName().split("\\.")[3].split("_")[1]);

        if (currentVersion < workingVersion) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(this, () -> {
                Bukkit.getConsoleSender().sendMessage("");
                Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "You installed the 1." + workingVersion + "+ only version of " + this.getDescription().getName() + " on a 1." + currentVersion + " server. Since you are on the wrong version we disabled the plugin for you. Please install correct version to continue using " + this.getDescription().getName() + ".");
                Bukkit.getConsoleSender().sendMessage("");
            }, 20L);
            return false;
        }
        return true;
    }

    @Override
    public void onEnable() {
        // Check to make sure the Bukkit version is compatible.
        if (!checkVersion()) return;
        checkStorage();
        INSTANCE = this;

        CommandSender console = Bukkit.getConsoleSender();
        console.sendMessage(Methods.formatText("&a============================="));
        console.sendMessage(Methods.formatText("&7EpicFarming " + this.getDescription().getVersion() + " by &5Songoda <3&7!"));
        console.sendMessage(Methods.formatText("&7Action: &aEnabling&7..."));

        this.settingsManager = new SettingsManager(this);
        this.setupConfig();

        // Setup language
        String langMode = getConfig().getString("System.Language Mode");
        Locale.init(this);
        Locale.saveDefaultLocale("en_US");
        this.locale = Locale.getLocale(getConfig().getString("System.Language Mode", langMode));

        //Running Songoda Updater
        Plugin plugin = new Plugin(this, 21);
        plugin.addModule(new LocaleModule());
        SongodaUpdate.load(plugin);

        dataFile.createNewFile("Loading Data File", "EpicFarming Data File");
        this.loadDataFile();

        this.loadLevelManager();

        this.farmManager = new EFarmManager();
        this.playerActionManager = new PlayerActionManager();
        this.boostManager = new BoostManager();
        this.commandManager = new CommandManager(this);
        this.hookManager = new HookManager(this);

        /*
         * Register Farms into FarmManger from configuration
         */
        Bukkit.getScheduler().runTaskLater(this, () -> {
            if (storage.containsGroup("farms")) {
                for (StorageRow row : storage.getRowsByGroup("farms")) {
                    Location location = Methods.unserializeLocation(row.getKey());
                    if (location == null || location.getBlock() == null) return;

                    int level = row.get("level").asInt();
                    List<ItemStack> items =row.get("contents").asItemStackList();
                    UUID placedBY = UUID.fromString(row.get("placedby").asString());
                    EFarm farm = new EFarm(location,levelManager.getLevel(level),placedBY);
                    farm.loadInventory(items);
                    farmManager.addFarm(location,farm);
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

        this.references = new References();

        PluginManager pluginManager = Bukkit.getPluginManager();

        // Register Listeners
        pluginManager.registerEvents(new BlockListeners(this), this);
        pluginManager.registerEvents(new InteractListeners(this), this);
        pluginManager.registerEvents(new InventoryListeners(this), this);

        // Start tasks
        this.growthTask = GrowthTask.startTask(this);
        HopperTask.startTask(this);
        this.farmTask = FarmTask.startTask(this);
        this.entityTask = EntityTask.startTask(this);

        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, this::saveToFile, 6000, 6000);

        // Start Metrics
        new Metrics(this);

        console.sendMessage(Methods.formatText("&a============================="));
    }

    private void checkStorage() {
        if (getConfig().getBoolean("Database.Activate Mysql Support")) {
            this.storage = new StorageMysql(this);
        } else {
            this.storage = new StorageYaml(this);
        }
    }

    public void onDisable() {
        saveToFile();
        this.storage.closeConnection();
        for (PlayerData playerData : playerActionManager.getRegisteredPlayers()) {
            if (playerData.getPlayer() != null)
                playerData.getPlayer().closeInventory();
        }
        CommandSender console = Bukkit.getConsoleSender();
        console.sendMessage(Methods.formatText("&a============================="));
        console.sendMessage(Methods.formatText("&7EpicFarming " + this.getDescription().getVersion() + " by &5Brianna <3!"));
        console.sendMessage(Methods.formatText("&7Action: &cDisabling&7..."));
        console.sendMessage(Methods.formatText("&a============================="));
    }

    private void loadLevelManager() {
        // Load an instance of LevelManager
        levelManager = new ELevelManager();

        /*
         * Register Levels into LevelManager from configuration.
         */
        levelManager.clear();
        for (String levelName : getConfig().getConfigurationSection("settings.levels").getKeys(false)) {
            int level = Integer.valueOf(levelName.split("-")[1]);
            int costExperiance = getConfig().getInt("settings.levels." + levelName + ".Cost-xp");
            int costEconomy = getConfig().getInt("settings.levels." + levelName + ".Cost-eco");
            int radius = getConfig().getInt("settings.levels." + levelName + ".Radius");
            double speedMultiplier = getConfig().getDouble("settings.levels." + levelName + ".Speed-Multiplier");
            boolean autoHarvest = getConfig().getBoolean("settings.levels." + levelName + ".Auto-Harvest");
            boolean autoReplant = getConfig().getBoolean("settings.levels." + levelName + ".Auto-Replant");
            boolean autoBreeding = getConfig().getBoolean("settings.levels." + levelName + ".Auto-Breeding");
            levelManager.addLevel(level, costExperiance, costEconomy, speedMultiplier, radius, autoHarvest, autoReplant, autoBreeding);
        }
    }

    /*
     * Saves registered farms to file.
     */
    private void saveToFile() {
        checkStorage();

        storage.doSave();
    }

    public void reload() {
        locale.reloadMessages();
        references = new References();
        this.hookManager = new HookManager(this);
        this.setupConfig();
        saveConfig();
    }

    private void setupConfig() {
        settingsManager.updateSettings();

        if (!getConfig().contains("settings.levels.Level-1")) {
            ConfigurationSection levels =
                    getConfig().createSection("settings.levels");

            levels.set("Level-1.Radius", 1);
            levels.set("Level-1.Speed-Multiplier", 1);
            levels.set("Level-1.Cost-xp", 20);
            levels.set("Level-1.Cost-eco", 5000);

            levels.set("Level-2.Radius", 2);
            levels.set("Level-2.Speed-Multiplier", 1.5);
            levels.set("Level-2.Auto-Harvest", true);
            levels.set("Level-2.Cost-xp", 20);
            levels.set("Level-2.Cost-eco", 5000);

            levels.set("Level-3.Radius", 3);
            levels.set("Level-3.Speed-Multiplier", 1.5);
            levels.set("Level-3.Auto-Harvest", true);
            levels.set("Level-3.Auto-Replant", true);
            levels.set("Level-3.Auto-Breeding", true);
            levels.set("Level-3.Cost-xp", 25);
            levels.set("Level-3.Cost-eco", 7500);

            levels.set("Level-4.Radius", 3);
            levels.set("Level-4.Speed-Multiplier", 2);
            levels.set("Level-4.Auto-Harvest", true);
            levels.set("Level-4.Auto-Replant", true);
            levels.set("Level-4.Auto-Breeding", true);
            levels.set("Level-4.Cost-xp", 30);
            levels.set("Level-4.Cost-eco", 10000);

            levels.set("Level-5.Radius", 3);
            levels.set("Level-5.Speed-Multiplier", 2.5);
            levels.set("Level-5.Auto-Harvest", true);
            levels.set("Level-5.Auto-Replant", true);
            levels.set("Level-5.Auto-Breeding", true);
            levels.set("Level-5.Cost-xp", 35);
            levels.set("Level-5.Cost-eco", 12000);

            levels.set("Level-6.Radius", 4);
            levels.set("Level-6.Speed-Multiplier", 3);
            levels.set("Level-6.Auto-Harvest", true);
            levels.set("Level-6.Auto-Replant", true);
            levels.set("Level-6.Auto-Breeding", true);
            levels.set("Level-6.Cost-xp", 40);
            levels.set("Level-6.Cost-eco", 25000);
        }

        getConfig().options().copyDefaults(true);
        saveConfig();
    }

    private void loadDataFile() {
        dataFile.getConfig().options().copyDefaults(true);
        dataFile.saveConfig();
    }

    public Locale getLocale() {
        return locale;
    }

    @Override
    public int getLevelFromItem(ItemStack item) {
        try {
            if (!item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) return 0;
            if (item.getItemMeta().getDisplayName().contains(":")) {
                return NumberUtils.toInt(item.getItemMeta().getDisplayName().replace("\u00A7", "").split(":")[0], 0);
            }
        } catch (Exception ex) {
            Debugger.runReport(ex);
        }
        return 0;
    }

    @Override
    public ItemStack makeFarmItem(Level level) {
        ItemStack item = new ItemStack(Material.valueOf(EpicFarmingPlugin.getInstance().getConfig().getString("Main.Farm Block Material")), 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(Methods.formatText(Methods.formatName(level.getLevel(), true)));
        String line = getLocale().getMessage("general.nametag.lore");
        if (!line.equals("")) meta.setLore(Arrays.asList(line));
        item.setItemMeta(meta);
        return item;
    }

    @Override
    public EFarmManager getFarmManager() {
        return farmManager;
    }

    @Override
    public ELevelManager getLevelManager() {
        return levelManager;
    }

    public References getReferences() {
        return references;
    }

    public SettingsManager getSettingsManager() {
        return settingsManager;
    }

    public CommandManager getCommandManager() {
        return commandManager;
    }

    public BoostManager getBoostManager() {
        return boostManager;
    }

    public PlayerActionManager getPlayerActionManager() {
        return playerActionManager;
    }

    public HookManager getHookManager() {
        return hookManager;
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
}