package com.songoda.epicfarming;

import com.google.common.base.Preconditions;
import com.songoda.arconix.api.mcupdate.MCUpdate;
import com.songoda.arconix.api.utils.ConfigWrapper;
import com.songoda.arconix.plugin.Arconix;
import com.songoda.epicfarming.api.EpicFarming;
import com.songoda.epicfarming.api.farming.Farm;
import com.songoda.epicfarming.api.farming.Level;
import com.songoda.epicfarming.api.utils.ClaimableProtectionPluginHook;
import com.songoda.epicfarming.api.utils.ProtectionPluginHook;
import com.songoda.epicfarming.boost.BoostData;
import com.songoda.epicfarming.boost.BoostManager;
import com.songoda.epicfarming.command.CommandManager;
import com.songoda.epicfarming.farming.EFarm;
import com.songoda.epicfarming.farming.EFarmManager;
import com.songoda.epicfarming.farming.ELevelManager;
import com.songoda.epicfarming.hooks.*;
import com.songoda.epicfarming.listeners.BlockListeners;
import com.songoda.epicfarming.listeners.InteractListeners;
import com.songoda.epicfarming.listeners.InventoryListeners;
import com.songoda.epicfarming.player.PlayerActionManager;
import com.songoda.epicfarming.player.PlayerData;
import com.songoda.epicfarming.tasks.EntityTask;
import com.songoda.epicfarming.tasks.FarmTask;
import com.songoda.epicfarming.tasks.GrowthTask;
import com.songoda.epicfarming.tasks.HopperTask;
import com.songoda.epicfarming.utils.Debugger;
import com.songoda.epicfarming.utils.Methods;
import com.songoda.epicfarming.utils.SettingsManager;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Created by songoda on 1/23/2018.
 */
public class EpicFarmingPlugin extends JavaPlugin implements EpicFarming {

    private static EpicFarmingPlugin INSTANCE;

    private List<ProtectionPluginHook> protectionHooks = new ArrayList<>();
    private ClaimableProtectionPluginHook factionsHook, townyHook, aSkyblockHook, uSkyblockHook;

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

    private GrowthTask growthTask;
    private HopperTask hopperTask;
    private FarmTask farmTask;
    private EntityTask entityTask;

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

        String langMode = getConfig().getString("System.Language Mode");
        Locale.init(this);
        Locale.saveDefaultLocale("en_US");
        this.locale = Locale.getLocale(getConfig().getString("System.Language Mode", langMode));

        if (getConfig().getBoolean("System.Download Needed Data Files")) {
            this.update();
        }

        this.settingsManager = new SettingsManager(this);
        setupConfig();

        dataFile.createNewFile("Loading Data File", "EpicFarming Data File");
        loadDataFile();

        loadLevelManager();

        this.farmManager = new EFarmManager();
        this.playerActionManager = new PlayerActionManager();
        this.boostManager = new BoostManager();
        this.commandManager = new CommandManager(this);

        /*
         * Register Farms into FarmManger from configuration
         */
        Bukkit.getScheduler().runTaskLater(this, () -> {
            if (dataFile.getConfig().contains("Farms")) {
                for (String locationStr : dataFile.getConfig().getConfigurationSection("Farms").getKeys(false)) {
                    Location location = Arconix.pl().getApi().serialize().unserializeLocation(locationStr);
                    if (location == null || location.getWorld() == null) continue;
                    int level = dataFile.getConfig().getInt("Farms." + locationStr + ".level");

                    List<ItemStack> items = (List<ItemStack>) dataFile.getConfig().getList("Farms." + locationStr + ".Contents");

                    String placedByStr = dataFile.getConfig().getString("Farms." + locationStr + ".placedBy");

                    UUID placedBy = placedByStr == null ? null : UUID.fromString(placedByStr);

                    EFarm farm = new EFarm(location, levelManager.getLevel(level), placedBy);
                    farm.loadInventory(items);

                    farmManager.addFarm(location, farm);
                }
            }

            // Adding in Boosts
            if (dataFile.getConfig().contains("data.boosts")) {
                for (String key : dataFile.getConfig().getConfigurationSection("data.boosts").getKeys(false)) {
                    if (!dataFile.getConfig().contains("data.boosts." + key + ".Player")) continue;
                    BoostData boostData = new BoostData(
                            dataFile.getConfig().getInt("data.boosts." + key + ".Amount"),
                            Long.parseLong(key),
                            UUID.fromString(dataFile.getConfig().getString("data.boosts." + key + ".Player")));

                    this.boostManager.addBoostToPlayer(boostData);
                }
            }
        }, 10);

        this.references = new References();

        PluginManager pluginManager = Bukkit.getPluginManager();

        // Register Listeners
        pluginManager.registerEvents(new BlockListeners(this), this);
        pluginManager.registerEvents(new InteractListeners(this), this);
        pluginManager.registerEvents(new InventoryListeners(this), this);

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

        // Start tasks
        this.growthTask = GrowthTask.startTask(this);
        this.hopperTask = HopperTask.startTask(this);
        this.farmTask = FarmTask.startTask(this);
        this.entityTask = entityTask.startTask(this);

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
            boolean autoBreeding = getConfig().getBoolean("settings.levels." + levelName + ".Auto-Breeding");
            levelManager.addLevel(level, costExperiance, costEconomy, speedMultiplier, radius, autoHarvest, autoReplant, autoBreeding);
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
            dataFile.getConfig().set("Farms." + locationStr + ".placedBy", farm.getPlacedBy() == null ? null : farm.getPlacedBy().toString());
            dataFile.getConfig().set("Farms." + locationStr + ".Contents", ((EFarm) farm).dumpInventory());
        }

        /*
         * Dump BoostManager to file.
         */
        for (BoostData boostData : boostManager.getBoosts()) {
            String endTime = String.valueOf(boostData.getEndTime());
            dataFile.getConfig().set("data.boosts." + endTime + ".Player", boostData.getPlayer().toString());
            dataFile.getConfig().set("data.boosts." + endTime + ".Amount", boostData.getMultiplier());
        }

        //Save to file
        dataFile.saveConfig();

    }

    private void update() {
        try {
            URL url = new URL("http://update.songoda.com/index.php?plugin=" + getDescription().getName() + "&version=" + getDescription().getVersion());
            URLConnection urlConnection = url.openConnection();
            InputStream is = urlConnection.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);

            int numCharsRead;
            char[] charArray = new char[1024];
            StringBuffer sb = new StringBuffer();
            while ((numCharsRead = isr.read(charArray)) > 0) {
                sb.append(charArray, 0, numCharsRead);
            }
            String jsonString = sb.toString();
            JSONObject json = (JSONObject) new JSONParser().parse(jsonString);

            JSONArray files = (JSONArray) json.get("neededFiles");
            for (Object o : files) {
                JSONObject file = (JSONObject) o;

                switch ((String)file.get("type")) {
                    case "locale":
                        InputStream in = new URL((String) file.get("link")).openStream();
                        Locale.saveDefaultLocale(in, (String) file.get("name"));
                        break;
                }
            }
        } catch (Exception e) {
            System.out.println("Failed to update.");
            //e.printStackTrace();
        }
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