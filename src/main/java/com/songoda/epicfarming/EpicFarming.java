package com.songoda.epicfarming;

import com.songoda.core.SongodaCore;
import com.songoda.core.SongodaPlugin;
import com.songoda.core.commands.CommandManager;
import com.songoda.core.compatibility.CompatibleMaterial;
import com.songoda.core.configuration.Config;
import com.songoda.epicfarming.boost.BoostData;
import com.songoda.epicfarming.boost.BoostManager;
import com.songoda.epicfarming.farming.Farm;
import com.songoda.epicfarming.farming.FarmManager;
import com.songoda.epicfarming.farming.Level;
import com.songoda.epicfarming.farming.LevelManager;
import com.songoda.epicfarming.listeners.BlockListeners;
import com.songoda.epicfarming.listeners.InteractListeners;
import com.songoda.epicfarming.listeners.InventoryListeners;
import com.songoda.epicfarming.player.PlayerActionManager;
import com.songoda.epicfarming.player.PlayerData;
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
import org.bukkit.Material;
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

    private FarmManager farmManager;
    private LevelManager levelManager;
    private PlayerActionManager playerActionManager;
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
        for (PlayerData playerData : playerActionManager.getRegisteredPlayers()) {
            if (playerData.getPlayer() != null)
                playerData.getPlayer().closeInventory();
        }
    }

    @Override
    public void onPluginEnable() {
        // Run Songoda Updater
        SongodaCore.registerPlugin(this, 21, CompatibleMaterial.WHEAT);

        checkStorage();

        this.setupConfig();

        // Setup language
        String langMode = getConfig().getString("System.Language Mode");
        Locale.init(this);
        Locale.saveDefaultLocale("en_US");
        this.locale = Locale.getLocale(getConfig().getString("System.Language Mode", langMode));

        dataFile.createNewFile("Loading Data File", "EpicFarming Data File");
        this.loadDataFile();

        this.loadLevelManager();

        this.farmManager = new FarmManager();
        this.playerActionManager = new PlayerActionManager();
        this.boostManager = new BoostManager();
        this.commandManager = new CommandManager(this);

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
                    farm.loadInventory(items);
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

        this.references = new References();

        PluginManager pluginManager = Bukkit.getPluginManager();

        // Register Listeners
        pluginManager.registerEvents(new BlockListeners(this), this);
        pluginManager.registerEvents(new InteractListeners(this), this);
        pluginManager.registerEvents(new InventoryListeners(this), this);

        // Start tasks
        this.growthTask = GrowthTask.startTask(this);
        this.farmTask = FarmTask.startTask(this);
        this.entityTask = EntityTask.startTask(this);

        Bukkit.getScheduler().runTaskLater(this, () -> {
            if (!Bukkit.getPluginManager().isPluginEnabled("EpicHoppers"))
                HopperTask.startTask(this);
        }, 20L);

        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, this::saveToFile, 6000, 6000);
    }

    @Override
    public void onConfigReload() {
        this.setLocale(getConfig().getString("System.Language Mode"), true);
        this.locale.reloadMessages();
        this.blacklistHandler.reload();
        loadLevelManager();
    }

    @Override
    public List<Config> getExtraConfig() {
        return Arrays.asList(levelsFile);
    }

    private void checkStorage() {
        this.storage = new StorageYaml(this);
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

    private void loadDataFile() {
        dataFile.getConfig().options().copyDefaults(true);
        dataFile.saveConfig();
    }

    public Locale getLocale() {
        return locale;
    }

    public int getLevelFromItem(ItemStack item) {
        if (!item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) return 0;
        if (item.getItemMeta().getDisplayName().contains(":")) {
            return NumberUtils.toInt(item.getItemMeta().getDisplayName().replace("\u00A7", "").split(":")[0], 0);
        }
        return 0;
    }

    public ItemStack makeFarmItem(Level level) {
        ItemStack item = new ItemStack(Material.valueOf(EpicFarming.getInstance().getConfig().getString("Main.Farm Block Material")), 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(Methods.formatText(Methods.formatName(level.getLevel(), true)));
        String line = getLocale().getMessage("general.nametag.lore");
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

    public PlayerActionManager getPlayerActionManager() {
        return playerActionManager;
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