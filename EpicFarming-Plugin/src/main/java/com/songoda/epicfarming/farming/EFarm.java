package com.songoda.epicfarming.farming;

import com.songoda.arconix.api.methods.formatting.TextComponent;
import com.songoda.arconix.api.methods.formatting.TimeComponent;
import com.songoda.arconix.plugin.Arconix;
import com.songoda.epicfarming.EpicFarmingPlugin;
import com.songoda.epicfarming.api.farming.Farm;
import com.songoda.epicfarming.api.farming.Level;
import com.songoda.epicfarming.api.farming.UpgradeType;
import com.songoda.epicfarming.boost.BoostData;
import com.songoda.epicfarming.player.PlayerData;
import com.songoda.epicfarming.utils.Debugger;
import com.songoda.epicfarming.utils.Methods;
import com.songoda.epicfarming.api.farming.Farm;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.*;

public class EFarm implements Farm {

    private Location location;
    private Level level;
    private Inventory inventory;
    private UUID placedBy;

    private long lastCached = 0;
    private final List<Block> cachedCrops = new ArrayList<>();

    public EFarm(Location location, Level level, UUID placedBy) {
        this.location = location;
        this.level = level;
        this.placedBy = placedBy;
        this.inventory = Bukkit.createInventory(null, 54, Methods.formatName(level.getLevel(),false));
    }

    public void view(Player player) {
        try {
            if (!player.hasPermission("epicfarming.view"))
                return;

            setupOverview(player);

            player.openInventory(inventory);
            PlayerData playerData = EpicFarmingPlugin.getInstance().getPlayerActionManager().getPlayerAction(player);

            playerData.setFarm(this);

            getInventory();

        } catch (Exception ex) {
            Debugger.runReport(ex);
        }
    }

    private void setupOverview(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 54, Methods.formatName(level.getLevel(),false));
        inventory.setContents(this.inventory.getContents());
        this.inventory = inventory;

        EpicFarmingPlugin instance = EpicFarmingPlugin.getInstance();

        ELevel nextLevel = instance.getLevelManager().getHighestLevel().getLevel() > level.getLevel() ? instance.getLevelManager().getLevel(level.getLevel()+1) : null;

        int level = this.level.getLevel();

        ItemStack item = new ItemStack(Material.valueOf(instance.getConfig().getString("Main.Farm Block Material")), 1);
        ItemMeta itemmeta = item.getItemMeta();
        itemmeta.setDisplayName(instance.getLocale().getMessage("general.nametag.farm", level));
        List<String> lore = this.level.getDescription();
        lore.add("");
        if (nextLevel == null) lore.add(instance.getLocale().getMessage("event.upgrade.maxed"));
        else {
            lore.add(instance.getLocale().getMessage("interface.button.level", nextLevel.getLevel()));
            lore.addAll(nextLevel.getDescription());
        }

        BoostData boostData = instance.getBoostManager().getBoost(placedBy);
        if (boostData != null) {
            String[] parts = instance.getLocale().getMessage("interface.button.boostedstats", Integer.toString(boostData.getMultiplier()), TimeComponent.makeReadable(boostData.getEndTime() - System.currentTimeMillis())).split("\\|");
            lore.add("");
            for (String line : parts)
                lore.add(TextComponent.formatText(line));
        }

        itemmeta.setLore(lore);
        item.setItemMeta(itemmeta);

        ItemStack itemXP = new ItemStack(Material.valueOf(instance.getConfig().getString("Interfaces.XP Icon")), 1);
        ItemMeta itemmetaXP = itemXP.getItemMeta();
        itemmetaXP.setDisplayName(instance.getLocale().getMessage("interface.button.upgradewithxp"));
        ArrayList<String> loreXP = new ArrayList<>();
        if (nextLevel != null)
            loreXP.add(instance.getLocale().getMessage("interface.button.upgradewithxplore", nextLevel.getCostExperiance()));
        else
            loreXP.add(instance.getLocale().getMessage("event.upgrade.maxed"));
        itemmetaXP.setLore(loreXP);
        itemXP.setItemMeta(itemmetaXP);

        ItemStack itemECO = new ItemStack(Material.valueOf(instance.getConfig().getString("Interfaces.Economy Icon")), 1);
        ItemMeta itemmetaECO = itemECO.getItemMeta();
        itemmetaECO.setDisplayName(instance.getLocale().getMessage("interface.button.upgradewitheconomy"));
        ArrayList<String> loreECO = new ArrayList<>();
        if (nextLevel != null)
            loreECO.add(instance.getLocale().getMessage("interface.button.upgradewitheconomylore", Arconix.pl().getApi().format().formatEconomy(nextLevel.getCostEconomy())));
        else
            loreECO.add(instance.getLocale().getMessage("event.upgrade.maxed"));
        itemmetaECO.setLore(loreECO);
        itemECO.setItemMeta(itemmetaECO);

        int nu = 0;
        while (nu != 27) {
            inventory.setItem(nu, Methods.getGlass());
            nu++;
        }
        if (instance.getConfig().getBoolean("Main.Upgrade With XP") && player != null && player.hasPermission("EpicFarming.Upgrade.XP")) {
            inventory.setItem(11, itemXP);
        }

        inventory.setItem(13, item);

        if (instance.getConfig().getBoolean("Main.Upgrade With Economy") && player != null && player.hasPermission("EpicFarming.Upgrade.ECO")) {
            inventory.setItem(15, itemECO);
        }

        inventory.setItem(0, Methods.getBackgroundGlass(true));
        inventory.setItem(1, Methods.getBackgroundGlass(true));
        inventory.setItem(2, Methods.getBackgroundGlass(false));
        inventory.setItem(6, Methods.getBackgroundGlass(false));
        inventory.setItem(7, Methods.getBackgroundGlass(true));
        inventory.setItem(8, Methods.getBackgroundGlass(true));
        inventory.setItem(9, Methods.getBackgroundGlass(true));
        inventory.setItem(10, Methods.getBackgroundGlass(false));
        inventory.setItem(16, Methods.getBackgroundGlass(false));
        inventory.setItem(17, Methods.getBackgroundGlass(true));
        inventory.setItem(18, Methods.getBackgroundGlass(true));
        inventory.setItem(19, Methods.getBackgroundGlass(true));
        inventory.setItem(20, Methods.getBackgroundGlass(false));
        inventory.setItem(24, Methods.getBackgroundGlass(false));
        inventory.setItem(25, Methods.getBackgroundGlass(true));
        inventory.setItem(26, Methods.getBackgroundGlass(true));
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public void loadInventory(List<ItemStack> items) {
        setupOverview(null);
        int i = 27;
        for (ItemStack item : items) {
            inventory.setItem(i++, item);
        }
    }

    public List<ItemStack> dumpInventory() {
        List<ItemStack> items = new ArrayList<>();

        for(int i=27; i < inventory.getSize(); i++) {
            items.add(inventory.getItem(i));
        }

        return items;
    }

    public void upgrade(UpgradeType type, Player player) {
        try {
            EpicFarmingPlugin instance = EpicFarmingPlugin.getInstance();
            if (instance.getLevelManager().getLevels().containsKey(this.level.getLevel()+1)) {

                com.songoda.epicfarming.api.farming.Level level = instance.getLevelManager().getLevel(this.level.getLevel()+1);
                int cost;
                if (type == UpgradeType.EXPERIENCE) {
                    cost = level.getCostExperiance();
                } else {
                    cost = level.getCostEconomy();
                }

                if (type == UpgradeType.ECONOMY) {
                    if (instance.getServer().getPluginManager().getPlugin("Vault") != null) {
                        RegisteredServiceProvider<Economy> rsp = instance.getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
                        net.milkbowl.vault.economy.Economy econ = rsp.getProvider();
                        if (econ.has(player, cost)) {
                            econ.withdrawPlayer(player, cost);
                            upgradeFinal(level, player);
                        } else {
                            player.sendMessage(instance.getLocale().getMessage("event.upgrade.cannotafford"));
                        }
                    } else {
                        player.sendMessage("Vault is not installed.");
                    }
                } else if (type == UpgradeType.EXPERIENCE) {
                    if (player.getLevel() >= cost || player.getGameMode() == GameMode.CREATIVE) {
                        if (player.getGameMode() != GameMode.CREATIVE) {
                            player.setLevel(player.getLevel() - cost);
                        }
                        upgradeFinal(level, player);
                    } else {
                        player.sendMessage(instance.getLocale().getMessage("event.upgrade.cannotafford"));
                    }
                }
            }
        } catch (Exception ex) {
            Debugger.runReport(ex);
        }
    }


    private void upgradeFinal(Level level, Player player) {
        try {
            EpicFarmingPlugin instance = EpicFarmingPlugin.getInstance();
            this.level = level;
            if (instance.getLevelManager().getHighestLevel() != level) {
                player.sendMessage(instance.getLocale().getMessage("event.upgrade.success", level.getLevel()));
            } else {
                player.sendMessage(instance.getLocale().getMessage("event.upgrade.successmaxed", level.getLevel()));
            }
            Location loc = location.clone().add(.5, .5, .5);
                player.getWorld().spawnParticle(org.bukkit.Particle.valueOf(instance.getConfig().getString("Main.Upgrade Particle Type")), loc, 200, .5, .5, .5);

            if (instance.getConfig().getBoolean("Main.Sounds Enabled")) {
                if (instance.getLevelManager().getHighestLevel() != level) {
                        player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 0.6F, 15.0F);

                } else {
                        player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 2F, 25.0F);
                }
            }
            tillLand(location);
        } catch (Exception ex) {
            Debugger.runReport(ex);
        }
    }

    private static final Random random = new Random();

    public boolean tillLand(Location location) {
        Player player = Bukkit.getPlayer(placedBy);
        EpicFarmingPlugin instance = EpicFarmingPlugin.getInstance();
        if (instance.getConfig().getBoolean("Main.Disable Auto Til Land")) return true;
        Block block = location.getBlock();
        int radius = level.getRadius();
        int bx = block.getX();
        int by = block.getY();
        int bz = block.getZ();
        for (int fx = -radius; fx <= radius; fx++) {
            for (int fy = -2; fy <= 1; fy++) {
                for (int fz = -radius; fz <= radius; fz++) {
                    if (!instance.canBuild(player, location)) continue;
                    Block b2 = block.getWorld().getBlockAt(bx + fx, by + fy, bz + fz);

                    // ToDo: enum for all flowers.
                    if (b2.getType() == Material.TALL_GRASS || b2.getType() == Material.GRASS || b2.getType().name().contains("TULIP") || b2.getType() == Material.AZURE_BLUET ||
                            b2.getType() == Material.BLUE_ORCHID || b2.getType() == Material.ALLIUM ||  b2.getType() == Material.POPPY || b2.getType() == Material.DANDELION) {
                        Bukkit.getScheduler().runTaskLater(EpicFarmingPlugin.getInstance(), () -> {
                            b2.getRelative(BlockFace.DOWN).setType(Material.LEGACY_SOIL);
                            b2.breakNaturally();
                            if (instance.getConfig().getBoolean("Main.Sounds Enabled")) {
                                    b2.getWorld().playSound(b2.getLocation(), org.bukkit.Sound.BLOCK_GRASS_BREAK, 10, 15);
                            }
                        }, random.nextInt(30) + 1);
                    }
                    if ((b2.getType() == Material.GRASS_BLOCK || b2.getType() == Material.DIRT) && b2.getRelative(BlockFace.UP).getType() == Material.AIR) {
                        Bukkit.getScheduler().runTaskLater(EpicFarmingPlugin.getInstance(), () -> {
                            b2.setType(Material.LEGACY_SOIL);
                            if (instance.getConfig().getBoolean("Main.Sounds Enabled")) {
                                    b2.getWorld().playSound(b2.getLocation(), org.bukkit.Sound.BLOCK_GRASS_BREAK, 10, 15);
                            }
                        }, random.nextInt(30) + 1);
                    }

                }
            }
        }
        return false;
    }

    public void addCachedCrop(Block block) {
        cachedCrops.add(block);
    }

    public void removeCachedCrop(Block block) {
        cachedCrops.remove(block);
    }

    @Override
    public List<Block> getCachedCrops() {
        return new ArrayList<>(cachedCrops);
    }

    public void clearCache() {
        cachedCrops.clear();
    }

    public long getLastCached() {
        return lastCached;
    }

    public void setLastCached(long lastCached) {
        this.lastCached = lastCached;
    }

    @Override
    public Location getLocation() {
        return location.clone();
    }

    @Override
    public UUID getPlacedBy() {
        return placedBy;
    }

    @Override
    public void setLocation(Location location) {
        this.location = location;
    }

    @Override
    public Level getLevel() {
        return level;
    }
}