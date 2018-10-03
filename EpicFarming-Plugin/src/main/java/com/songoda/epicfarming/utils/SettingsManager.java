package com.songoda.epicfarming.utils;

import com.songoda.arconix.api.methods.formatting.TextComponent;
import com.songoda.arconix.api.utils.ConfigWrapper;
import com.songoda.epicfarming.EpicFarmingPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by songo on 6/4/2017.
 */
public class SettingsManager implements Listener {

    private String pluginName = "EpicFarming";

    private static final Pattern SETTINGS_PATTERN = Pattern.compile("(.{1,28}(?:\\s|$))|(.{0,28})", Pattern.DOTALL);

    private static ConfigWrapper defs;

    private Map<Player, String> cat = new HashMap<>();

    private final EpicFarmingPlugin instance;

    public SettingsManager(EpicFarmingPlugin plugin) {
        this.instance = plugin;

        plugin.saveResource("SettingDefinitions.yml", true);
        defs = new ConfigWrapper(plugin, "", "SettingDefinitions.yml");
        defs.createNewFile("Loading data file", pluginName + " SettingDefinitions file");
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    private Map<Player, String> current = new HashMap<>();

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        ItemStack clickedItem = event.getCurrentItem();

        if (event.getInventory() != event.getWhoClicked().getOpenInventory().getTopInventory()
                || clickedItem == null || !clickedItem.hasItemMeta()
                || !clickedItem.getItemMeta().hasDisplayName()) {
            return;
        }

        if (event.getInventory().getTitle().equals(pluginName + " Settings Manager")) {
            event.setCancelled(true);
            if (clickedItem.getType().name().contains("STAINED_GLASS")) return;

            String type = ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName());
            this.cat.put((Player) event.getWhoClicked(), type);
            this.openEditor((Player) event.getWhoClicked());
        } else if (event.getInventory().getTitle().equals(pluginName + " Settings Editor")) {
            event.setCancelled(true);
            if (clickedItem.getType().name().contains("STAINED_GLASS")) return;

            Player player = (Player) event.getWhoClicked();

            String key = cat.get(player) + "." + ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName());

            if (instance.getConfig().get(key).getClass().getName().equals("java.lang.Boolean")) {
                this.instance.getConfig().set(key, !instance.getConfig().getBoolean(key));
                this.finishEditing(player);
            } else {
                this.editObject(player, key);
            }
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (!current.containsKey(player)) return;

        String value = current.get(player);
        FileConfiguration config = instance.getConfig();
        if (config.isInt(value)) {
            config.set(value, Integer.parseInt(event.getMessage()));
        } else if (config.isDouble(value)) {
            config.set(value, Double.parseDouble(event.getMessage()));
        } else if (config.isString(value)) {
            config.set(value, event.getMessage());
        }

        this.finishEditing(player);
        event.setCancelled(true);
    }

    public void finishEditing(Player player) {
        this.current.remove(player);
        this.instance.saveConfig();
        this.openEditor(player);
    }


    public void editObject(Player player, String current) {
        this.current.put(player, ChatColor.stripColor(current));

        player.closeInventory();
        player.sendMessage("");
        player.sendMessage(TextComponent.formatText("&7Please enter a value for &6" + current + "&7."));
        if (instance.getConfig().isInt(current) || instance.getConfig().isDouble(current)) {
            player.sendMessage(TextComponent.formatText("&cUse only numbers."));
        }
        player.sendMessage("");
    }

    public void openSettingsManager(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 27, pluginName + " Settings Manager");
        ItemStack glass = Methods.getGlass();
        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, glass);
        }

        int slot = 10;
        for (String key : instance.getConfig().getDefaultSection().getKeys(false)) {
            ItemStack item = new ItemStack(Material.WHITE_WOOL, 1, (byte) (slot - 9)); //ToDo: Make this function as it was meant to.
            ItemMeta meta = item.getItemMeta();
            meta.setLore(Collections.singletonList(TextComponent.formatText("&6Click To Edit This Category.")));
            meta.setDisplayName(TextComponent.formatText("&f&l" + key));
            item.setItemMeta(meta);
            inventory.setItem(slot, item);
            slot++;
        }

        player.openInventory(inventory);
    }

    public void openEditor(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 54, pluginName + " Settings Editor");
        FileConfiguration config = instance.getConfig();

        int slot = 0;
        for (String key : config.getConfigurationSection(cat.get(player)).getKeys(true)) {
            String fKey = cat.get(player) + "." + key;
            ItemStack item = new ItemStack(Material.DIAMOND_HELMET);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(TextComponent.formatText("&6" + key));

            List<String> lore = new ArrayList<>();
            if (config.isBoolean(fKey)) {
                item.setType(Material.LEVER);
                lore.add(TextComponent.formatText(config.getBoolean(fKey) ? "&atrue" : "&cfalse"));
            } else if (config.isString(fKey)) {
                item.setType(Material.PAPER);
                lore.add(TextComponent.formatText("&9" + config.getString(fKey)));
            } else if (config.isInt(fKey)) {
                item.setType(Material.CLOCK);
                lore.add(TextComponent.formatText("&5" + config.getInt(fKey)));
            }

            if (defs.getConfig().contains(fKey)) {
                String text = defs.getConfig().getString(key);

                Matcher m = SETTINGS_PATTERN.matcher(text);
                while (m.find()) {
                    if (m.end() != text.length() || m.group().length() != 0)
                        lore.add(TextComponent.formatText("&7" + m.group()));
                }
            }

            meta.setLore(lore);
            item.setItemMeta(meta);

            inventory.setItem(slot, item);
            slot++;
        }

        player.openInventory(inventory);
    }

    public void updateSettings() {
        for (settings s : settings.values()) {
            instance.getConfig().addDefault(s.setting, s.option);
        }
    }

    public enum settings {
        o1("Main.Upgrade With Economy", true),
        o2("Main.Upgrade With XP", true),
        o3("Main.Upgrade Particle Type", "SPELL_WITCH"),
        o4("Main.Sounds Enabled", true),
        o5("Main.Farm Tick Speed", 70),
        o6("Main.Entity Tick Speed", 100),
        o7("Main.Growth Tick Speed", 20),
        o8("Main.Farm Block Material", "END_ROD"),
        o9("Main.Allow Non Command Issued Farm Items", false),
        o10("Main.Auto Breeding Cap", 15),
        o11("Main.Animate", true),
        o123("Main.Disable Auto Til Land", false),

        o12("Interfaces.Economy Icon", "SUNFLOWER"),
        o13("Interfaces.XP Icon", "EXPERIENCE_BOTTLE"),
        o14("Interfaces.Glass Type 1", 7),
        o15("Interfaces.Glass Type 2", 11),
        o16("Interfaces.Glass Type 3", 3),

        DOWNLOAD_FILES("System.Download Needed Data Files", true),
        LANGUGE_MODE("System.Language Mode", "en_US"),
        o17("System.Debugger Enabled", false);

        private String setting;
        private Object option;

        settings(String setting, Object option) {
            this.setting = setting;
            this.option = option;
        }

    }
}