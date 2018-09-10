package com.songoda.epicfarming;

import com.google.common.base.Preconditions;
import com.songoda.arconix.api.mcupdate.MCUpdate;
import com.songoda.arconix.api.utils.ConfigWrapper;
import com.songoda.arconix.plugin.Arconix;
import com.songoda.epicfarming.api.farming.Farm;
import com.songoda.epicfarming.api.farming.Level;
import com.songoda.epicfarming.api.utils.ClaimableProtectionPluginHook;
import com.songoda.epicfarming.api.utils.ProtectionPluginHook;
import com.songoda.epicfarming.events.BlockListeners;
import com.songoda.epicfarming.events.EntityListeners;
import com.songoda.epicfarming.events.InteractListeners;
import com.songoda.epicfarming.events.InventoryListeners;
import com.songoda.epicfarming.farming.EFarm;
import com.songoda.epicfarming.farming.EFarmManager;
import com.songoda.epicfarming.farming.ELevel;
import com.songoda.epicfarming.farming.ELevelManager;
import com.songoda.epicfarming.handlers.CommandHandler;
import com.songoda.epicfarming.handlers.FarmingHandler;
import com.songoda.epicfarming.handlers.GrowthHandler;
import com.songoda.epicfarming.hooks.*;
import com.songoda.epicfarming.player.PlayerActionManager;
import com.songoda.epicfarming.player.PlayerData;
import com.songoda.epicfarming.utils.Debugger;
import com.songoda.epicfarming.utils.Methods;
import com.songoda.epicfarming.utils.SettingsManager;
import com.songoda.epicfarming.api.EpicFarming;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Created by songoda on 1/23/2018.
 */
public class EpicFarmingPlugin extends JavaPlugin implements EpicFarming {

    private static EpicFarmingPlugin INSTANCE;

    private List<ProtectionPluginHook> protectionHooks = new ArrayList<>();
    private ClaimableProtectionPluginHook factionsHook, townyHook, aSkyblockHook, uSkyblockHook;

    public SettingsManager settingsManager;
    public References references;
    private ConfigWrapper hooksFile = new ConfigWrapper(this, "", "hooks.yml");
    public ConfigWrapper dataFile = new ConfigWrapper(this, "", "data.yml");
    private Locale locale;
    private FarmingHandler farmingHandler;
    private GrowthHandler growthHandler;
    private EFarmManager farmManager;
    private ELevelManager levelManager;
    private PlayerActionManager playerActionManager;

    public static EpicFarmingPlugin pl() {
        return INSTANCE;
    }

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

        INSTANCE = this;
        Arconix.pl().hook(this);

        CommandSender console = Bukkit.getConsoleSender();
        console.sendMessage(Arconix.pl().getApi().format().formatText("&a============================="));
        console.sendMessage(Arconix.pl().getApi().format().formatText("&7EpicFarming " + this.getDescription().getVersion() + " by &5Brianna <3&7!"));
        console.sendMessage(Arconix.pl().getApi().format().formatText("&7Action: &aEnabling&7..."));

        // Locales
        Locale.init(this);
        Locale.saveDefaultLocale("en_US");
        this.locale = Locale.getLocale(this.getConfig().getString("Locale", "en_US"));

        settingsManager = new SettingsManager(this);
        setupConfig();

        dataFile.createNewFile("Loading Data File", "EpicFarming Data File");
        loadDataFile();

        loadLevelManager();

        farmManager = new EFarmManager();

        /*
         * Register Farms into FarmManger from configuration
         */
        if (dataFile.getConfig().contains("Farms")) {
            for (String locationStr : dataFile.getConfig().getConfigurationSection("Farms").getKeys(false)) {
                Location location = Arconix.pl().getApi().serialize().unserializeLocation(locationStr);
                if (location == null || location.getWorld() == null) continue;
                int level = dataFile.getConfig().getInt("Farms." + locationStr + ".level");

                List<ItemStack> items = (List<ItemStack>) dataFile.getConfig().getList("Farms." + locationStr + ".Contents");

                EFarm farm = new EFarm(location, levelManager.getLevel(level));
                farm.loadInventory(items);

                farmManager.addFarm(location, farm);
            }
        }
        playerActionManager = new PlayerActionManager();


        farmingHandler = new FarmingHandler(this);
        growthHandler = new GrowthHandler(this);
        references = new References();

        this.getCommand("EpicFarming").setExecutor(new CommandHandler(this));

        PluginManager pluginManager = Bukkit.getPluginManager();

        // Register Listeners
        pluginManager.registerEvents(new BlockListeners(this), this);
        pluginManager.registerEvents(new InteractListeners(this), this);
        pluginManager.registerEvents(new InventoryListeners(this), this);
        pluginManager.registerEvents(new EntityListeners(), this);
        
        // Register default hooks
        if (pluginManager.isPluginEnabled("ASkyBlock")) this.register(HookASkyBlock::new);
        if (pluginManager.isPluginEnabled("FactionsFramework")) this.register(HookFactions::new);
        if (pluginManager.isPluginEnabled("GriefPrevention")) this.register(HookGriefPrevention::new);
        if (pluginManager.isPluginEnabled("Kingdoms")) this.register(HookKingdoms::new);
        if (pluginManager.isPluginEnabled("PlotSquared")) this.register(HookPlotSquared::new);
        if (pluginManager.isPluginEnabled("RedProtect")) this.register(HookRedProtect::new);
        if (pluginManager.isPluginEnabled("Towny")) this.register(HookTowny::new);
        if (pluginManager.isPluginEnabled("USkyBlock")) this.register(HookUSkyBlock::new);
        if (pluginManager.isPluginEnabled("WorldGuard")) this.register(HookWorldGuard::new);

        
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, this::saveToFile, 6000, 6000);

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
            levelManager.addLevel(level, costExperiance, costEconomy, speedMultiplier, radius, autoHarvest, autoReplant);
        }
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
            if (farm.getLocation() == null
                    || farm.getLocation().getWorld() == null) continue;
            String locationStr = Arconix.pl().getApi().serialize().serializeLocation(farm.getLocation());
            dataFile.getConfig().set("Farms." + locationStr + ".level", farm.getLevel().getLevel());
            dataFile.getConfig().set("Farms." + locationStr + ".Contents", ((EFarm)farm).dumpInventory());
        }

        //Save to file
        dataFile.saveConfig();

    }

    public void reload() {
        locale.reloadMessages();
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

    private void loadDataFile() {
        dataFile.getConfig().options().copyDefaults(true);
        dataFile.saveConfig();
    }

    public Locale getLocale() {
        return locale;
    }


    private void register(Supplier<ProtectionPluginHook> hookSupplier) {
        this.registerProtectionHook(hookSupplier.get());
    }


    @Override
    public void registerProtectionHook(ProtectionPluginHook hook) {
        Preconditions.checkNotNull(hook, "Cannot register null hook");
        Preconditions.checkNotNull(hook.getPlugin(), "Protection plugin hook returns null plugin instance (#getPlugin())");

        JavaPlugin hookPlugin = hook.getPlugin();
        for (ProtectionPluginHook existingHook : protectionHooks) {
            if (existingHook.getPlugin().equals(hookPlugin)) {
                throw new IllegalArgumentException("Hook already registered");
            }
        }

        this.hooksFile.getConfig().addDefault("hooks." + hookPlugin.getName(), true);
        if (!hooksFile.getConfig().getBoolean("hooks." + hookPlugin.getName(), true)) return;
        this.hooksFile.getConfig().options().copyDefaults(true);
        this.hooksFile.saveConfig();

        this.protectionHooks.add(hook);
        this.getLogger().info("Registered protection hook for plugin: " + hook.getPlugin().getName());
    }

    public boolean canBuild(Player player, Location location) {
        if (player.hasPermission(getDescription().getName() + ".bypass")) {
            return true;
        }

        for (ProtectionPluginHook hook : protectionHooks)
            if (!hook.canBuild(player, location)) return false;
        return true;
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
        meta.setDisplayName(Arconix.pl().getApi().format().formatText(Methods.formatName(level.getLevel(), true)));
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