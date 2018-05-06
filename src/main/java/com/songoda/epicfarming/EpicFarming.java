package com.songoda.epicfarming;

import com.songoda.arconix.api.mcupdate.MCUpdate;
import com.songoda.arconix.api.utils.ConfigWrapper;
import com.songoda.arconix.plugin.Arconix;
import com.songoda.epicfarming.events.BlockListeners;
import com.songoda.epicfarming.events.EntityListeners;
import com.songoda.epicfarming.events.InteractListeners;
import com.songoda.epicfarming.events.InventoryListeners;
import com.songoda.epicfarming.farming.Farm;
import com.songoda.epicfarming.farming.FarmManager;
import com.songoda.epicfarming.farming.LevelManager;
import com.songoda.epicfarming.handlers.CommandHandler;
import com.songoda.epicfarming.handlers.FarmingHandler;
import com.songoda.epicfarming.handlers.GrowthHandler;
import com.songoda.epicfarming.handlers.HookHandler;
import com.songoda.epicfarming.player.PlayerActionManager;
import com.songoda.epicfarming.player.PlayerData;
import com.songoda.epicfarming.utils.SettingsManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

/**
 * Created by songoda on 1/23/2018.
 */
public class EpicFarming extends JavaPlugin implements Listener {

    private static EpicFarming INSTANCE;

    public SettingsManager settingsManager;
    public References references;
    public HookHandler hooks;

    private ConfigWrapper langFile = new ConfigWrapper(this, "", "lang.yml");

    public boolean v1_10 = Bukkit.getServer().getClass().getPackage().getName().contains("1_10");
    public boolean v1_9 = Bukkit.getServer().getClass().getPackage().getName().contains("1_9");
    public boolean v1_7 = Bukkit.getServer().getClass().getPackage().getName().contains("1_7");
    public boolean v1_8 = Bukkit.getServer().getClass().getPackage().getName().contains("1_8");


    private FarmingHandler farmingHandler;
    private GrowthHandler growthHandler;

    private FarmManager farmManager;
    private LevelManager levelManager;
    private PlayerActionManager playerActionManager;

    public ConfigWrapper dataFile = new ConfigWrapper(this, "", "data.yml");

    public void onEnable() {
        INSTANCE = this;
        Arconix.pl().hook(this);

        CommandSender console = Bukkit.getConsoleSender();
        console.sendMessage(Arconix.pl().getApi().format().formatText("&a============================="));
        console.sendMessage(Arconix.pl().getApi().format().formatText("&7EpicFarming " + this.getDescription().getVersion() + " by &5Brianna <3&7!"));
        console.sendMessage(Arconix.pl().getApi().format().formatText("&7Action: &aEnabling&7..."));

        langFile.createNewFile("Loading Language File", "EpicFarming Language File");
        loadLanguageFile();

        settingsManager = new SettingsManager(this);
        setupConfig();
        levelManager = new LevelManager();

        dataFile.createNewFile("Loading Data File", "EpicFarming Data File");
        loadDataFile();

        /*
         * Register Levels into LevelManager from configuration.
         */
        for (String levelName : getConfig().getConfigurationSection("settings.levels").getKeys(false)) {
            int level = Integer.valueOf(levelName.split("-")[1]);
            int radius = getConfig().getInt("settings.levels." + levelName + ".Radius");
            int costExperiance = getConfig().getInt("settings.levels." + levelName + ".Cost-xp");
            int costEconomy = getConfig().getInt("settings.levels." + levelName + ".Cost-eco");
            double speedMultiplier = getConfig().getDouble("settings.levels." + levelName + ".Speed-Multiplier");
            boolean autoHarvest = getConfig().getBoolean("settings.levels." + levelName + ".Auto-Harvest");
            boolean autoReplant = getConfig().getBoolean("settings.levels." + levelName + ".Auto-Replant");
            levelManager.addLevel(level, costExperiance, costEconomy, speedMultiplier, radius, autoHarvest, autoReplant);
        }

        farmManager = new FarmManager();

        /*
         * Register Farms into FarmManger from configuration
         */
        if (dataFile.getConfig().contains("Farms")) {
            for (String locationStr : dataFile.getConfig().getConfigurationSection("Farms").getKeys(false)) {
                Location location = Arconix.pl().getApi().serialize().unserializeLocation(locationStr);
                int level = dataFile.getConfig().getInt("Farms." + locationStr + ".level");

                List<ItemStack> items = (List<ItemStack>)dataFile.getConfig().getList("Farms." + locationStr + ".Contents");

                Farm farm = new Farm(location, levelManager.getLevel(level));
                farm.loadInventory(items);

                farmManager.addFarm(location, farm);
            }
        }
        playerActionManager = new PlayerActionManager();

        hooks = new HookHandler();
        hooks.hook();

        farmingHandler = new FarmingHandler(this);
        growthHandler = new GrowthHandler(this);
        references = new References();

        this.getCommand("EpicFarming").setExecutor(new CommandHandler(this));

        getServer().getPluginManager().registerEvents(new BlockListeners(this), this);
        getServer().getPluginManager().registerEvents(new InteractListeners(this), this);
        getServer().getPluginManager().registerEvents(new InventoryListeners(this), this);
        getServer().getPluginManager().registerEvents(new EntityListeners(), this);

        this.getServer().getPluginManager().registerEvents(this, this);

        new MCUpdate(this, true);

        console.sendMessage(Arconix.pl().getApi().format().formatText("&a============================="));
    }

    public void onDisable() {
        saveToFile();
        for (PlayerData playerData : playerActionManager.getRegisteredPlayers()) {
            if (playerData.getPlayer() != null)
                playerData.getPlayer().closeInventory();
        }
        CommandSender console = Bukkit.getConsoleSender();
        dataFile.saveConfig();
        console.sendMessage(Arconix.pl().getApi().format().formatText("&a============================="));
        console.sendMessage(Arconix.pl().getApi().format().formatText("&7EpicFarming " + this.getDescription().getVersion() + " by &5Brianna <3!"));
        console.sendMessage(Arconix.pl().getApi().format().formatText("&7Action: &cDisabling&7..."));
        console.sendMessage(Arconix.pl().getApi().format().formatText("&a============================="));
    }

    /*
     * Saves registered farms to file.
     */
    private void saveToFile() {

        // Wipe old kit information
        dataFile.getConfig().set("Farms", null);

        /*
         * Dump FarmManager to file.
         */
        for (Farm farm : farmManager.getFarms().values()) {
            String locationStr = Arconix.pl().getApi().serialize().serializeLocation(farm.getLocation());
            dataFile.getConfig().set("Farms." + locationStr + ".level", farm.getLevel().getLevel());
            dataFile.getConfig().set("Farms." + locationStr + ".Contents", farm.dumpInventory());
        }

        //Save to file
        dataFile.saveConfig();

    }

    public void reload() {
        langFile.createNewFile("Loading Language File", "EpicFarming Language File");
        hooks.hooksFile.createNewFile("Loading hooks File", "EpicFarming hooks File");
        hooks = new HookHandler();
        hooks.hook();
        loadLanguageFile();
        references = new References();
        reloadConfig();
        saveConfig();
    }

    private void setupConfig() {
        settingsManager.updateSettings();

        if (!getConfig().contains("settings.levels.Level-1")) {
            getConfig().addDefault("settings.levels.Level-1.Radius", 1);
            getConfig().addDefault("settings.levels.Level-1.Speed-Multiplier", 1);
            getConfig().addDefault("settings.levels.Level-1.Cost-xp", 20);
            getConfig().addDefault("settings.levels.Level-1.Cost-eco", 5000);

            getConfig().addDefault("settings.levels.Level-2.Radius", 2);
            getConfig().addDefault("settings.levels.Level-2.Speed-Multiplier", 1.5);
            getConfig().addDefault("settings.levels.Level-2.Auto-Harvest", true);
            getConfig().addDefault("settings.levels.Level-2.Cost-xp", 20);
            getConfig().addDefault("settings.levels.Level-2.Cost-eco", 5000);

            getConfig().addDefault("settings.levels.Level-3.Radius", 3);
            getConfig().addDefault("settings.levels.Level-3.Speed-Multiplier", 1.5);
            getConfig().addDefault("settings.levels.Level-3.Auto-Harvest", true);
            getConfig().addDefault("settings.levels.Level-3.Auto-Replant", true);
            getConfig().addDefault("settings.levels.Level-3.Cost-xp", 25);
            getConfig().addDefault("settings.levels.Level-3.Cost-eco", 7500);

            getConfig().addDefault("settings.levels.Level-4.Radius", 3);
            getConfig().addDefault("settings.levels.Level-4.Speed-Multiplier", 2);
            getConfig().addDefault("settings.levels.Level-4.Auto-Harvest", true);
            getConfig().addDefault("settings.levels.Level-4.Auto-Replant", true);
            getConfig().addDefault("settings.levels.Level-4.Cost-xp", 30);
            getConfig().addDefault("settings.levels.Level-4.Cost-eco", 10000);

            getConfig().addDefault("settings.levels.Level-5.Radius", 3);
            getConfig().addDefault("settings.levels.Level-5.Speed-Multiplier", 2.5);
            getConfig().addDefault("settings.levels.Level-5.Auto-Harvest", true);
            getConfig().addDefault("settings.levels.Level-5.Auto-Replant", true);
            getConfig().addDefault("settings.levels.Level-5.Cost-xp", 35);
            getConfig().addDefault("settings.levels.Level-5.Cost-eco", 12000);

            getConfig().addDefault("settings.levels.Level-6.Radius", 4);
            getConfig().addDefault("settings.levels.Level-6.Speed-Multiplier", 3);
            getConfig().addDefault("settings.levels.Level-6.Auto-Harvest", true);
            getConfig().addDefault("settings.levels.Level-6.Auto-Replant", true);
            getConfig().addDefault("settings.levels.Level-6.Cost-xp", 40);
            getConfig().addDefault("settings.levels.Level-6.Cost-eco", 25000);
        }

        getConfig().options().copyDefaults(true);
        saveConfig();
    }

    private void loadLanguageFile() {
        Lang.setFile(langFile.getConfig());

        for (final Lang value : Lang.values()) {
            langFile.getConfig().addDefault(value.getPath(), value.getDefault());
        }

        langFile.getConfig().options().copyDefaults(true);
        langFile.saveConfig();
    }

    private void loadDataFile() {
        dataFile.getConfig().options().copyDefaults(true);
        dataFile.saveConfig();
    }

    public static EpicFarming pl() {
        return INSTANCE;
    }

    public static EpicFarming getInstance() {
        return INSTANCE;
    }

    public FarmManager getFarmManager() {
        return farmManager;
    }

    public LevelManager getLevelManager() {
        return levelManager;
    }

    public FarmingHandler getFarmingHandler() {
        return farmingHandler;
    }

    public PlayerActionManager getPlayerActionManager() {
        return playerActionManager;
    }

    public GrowthHandler getGrowthHandler() {
        return growthHandler;
    }
}
