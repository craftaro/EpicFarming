package com.songoda.epicfarming.gui;

import com.songoda.core.compatibility.CompatibleMaterial;
import com.songoda.core.gui.Gui;
import com.songoda.core.gui.GuiUtils;
import com.songoda.epicfarming.EpicFarming;
import com.songoda.epicfarming.boost.BoostData;
import com.songoda.epicfarming.farming.Farm;
import com.songoda.epicfarming.farming.Level;
import com.songoda.epicfarming.farming.UpgradeType;
import com.songoda.epicfarming.settings.Setting;
import com.songoda.epicfarming.utils.Methods;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class OverviewGui extends Gui {

    private EpicFarming plugin;
    private Farm farm;
    private Level level;
    private Player player;

    public OverviewGui(Farm farm, Player player) {
        this.plugin = EpicFarming.getInstance();
        this.farm = farm;
        this.level = farm.getLevel();
        this.player = player;
        this.setRows(6);
        this.setTitle(Methods.formatName(level.getLevel(), false));
        this.setAcceptsItems(true);
        this.setUnlockedRange(3, 0, 5, 8);

        ItemStack glass1 = GuiUtils.getBorderItem(Setting.GLASS_TYPE_1.getMaterial());
        ItemStack glass2 = GuiUtils.getBorderItem(Setting.GLASS_TYPE_2.getMaterial());
        ItemStack glass3 = GuiUtils.getBorderItem(Setting.GLASS_TYPE_3.getMaterial());

        this.setDefaultItem(null);

        GuiUtils.mirrorFill(this, 0, 0, false, true, glass2);
        GuiUtils.mirrorFill(this, 0, 1, false, true, glass2);
        GuiUtils.mirrorFill(this, 0, 2, false, true, glass3);
        GuiUtils.mirrorFill(this, 1, 0, false, true, glass2);
        GuiUtils.mirrorFill(this, 1, 1, false, true, glass3);
        GuiUtils.mirrorFill(this, 2, 0, false, true, glass2);
        GuiUtils.mirrorFill(this, 2, 1, false, true, glass2);
        GuiUtils.mirrorFill(this, 2, 2, false, true, glass3);

        GuiUtils.mirrorFill(this, 0, 3, false, true, glass1);
        GuiUtils.mirrorFill(this, 0, 4, false, false, glass1);
        GuiUtils.mirrorFill(this, 1, 3, false, true, glass1);
        GuiUtils.mirrorFill(this, 1, 2, false, true, glass1);
        GuiUtils.mirrorFill(this, 2, 3, false, true, glass1);
        GuiUtils.mirrorFill(this, 2, 4, false, false, glass1);

        // enable page events
        if (level.getPages() > 1) {
            this.pages = level.getPages();
            setPrevPage(2, 0, GuiUtils.createButtonItem(CompatibleMaterial.ARROW, plugin.getLocale().getMessage("general.interface.previous").getMessage()));
            setNextPage(2, 8, GuiUtils.createButtonItem(CompatibleMaterial.ARROW, plugin.getLocale().getMessage("general.interface.next").getMessage()));
            setOnPage((event) -> updateInventory());
        }

        showPage();
    }

    private void showPage() {

        Level nextLevel = plugin.getLevelManager().getHighestLevel().getLevel() > level.getLevel() ? plugin.getLevelManager().getLevel(level.getLevel() + 1) : null;

        ItemStack item = new ItemStack(Material.valueOf(plugin.getConfig().getString("Main.Farm Block Material")), 1);
        ItemMeta itemmeta = item.getItemMeta();
        itemmeta.setDisplayName(plugin.getLocale().getMessage("general.nametag.farm")
                .processPlaceholder("level", level.getLevel()).getMessage());
        List<String> lore = level.getDescription();
        lore.add("");
        if (nextLevel == null) lore.add(plugin.getLocale().getMessage("event.upgrade.maxed").getMessage());
        else {
            lore.add(plugin.getLocale().getMessage("interface.button.level")
                    .processPlaceholder("level", nextLevel.getLevel()).getMessage());
            lore.addAll(nextLevel.getDescription());
        }

        BoostData boostData = plugin.getBoostManager().getBoost(farm.getPlacedBy());
        if (boostData != null) {
            String[] parts = plugin.getLocale().getMessage("interface.button.boostedstats")
                    .processPlaceholder("amount", Integer.toString(boostData.getMultiplier()))
                    .processPlaceholder("time", Methods.makeReadable(boostData.getEndTime() - System.currentTimeMillis()))
                    .getMessage().split("\\|");
            lore.add("");
            for (String line : parts)
                lore.add(Methods.formatText(line));
        }

        itemmeta.setLore(lore);
        item.setItemMeta(itemmeta);

        ItemStack itemXP = Setting.XP_ICON.getMaterial().getItem();
        ItemMeta itemmetaXP = itemXP.getItemMeta();
        itemmetaXP.setDisplayName(plugin.getLocale().getMessage("interface.button.upgradewithxp").getMessage());
        ArrayList<String> loreXP = new ArrayList<>();
        if (nextLevel != null)
            loreXP.add(plugin.getLocale().getMessage("interface.button.upgradewithxplore")
                    .processPlaceholder("cost", nextLevel.getCostExperiance()).getMessage());
        else
            loreXP.add(plugin.getLocale().getMessage("event.upgrade.maxed").getMessage());
        itemmetaXP.setLore(loreXP);
        itemXP.setItemMeta(itemmetaXP);

        ItemStack itemECO = Setting.ECO_ICON.getMaterial().getItem();
        ItemMeta itemmetaECO = itemECO.getItemMeta();
        itemmetaECO.setDisplayName(plugin.getLocale().getMessage("interface.button.upgradewitheconomy").getMessage());
        ArrayList<String> loreECO = new ArrayList<>();
        if (nextLevel != null)
            loreECO.add(plugin.getLocale().getMessage("interface.button.upgradewitheconomylore")
                    .processPlaceholder("cost", Methods.formatEconomy(nextLevel.getCostEconomy()))
                    .getMessage());
        else
            loreECO.add(plugin.getLocale().getMessage("event.upgrade.maxed").getMessage());
        itemmetaECO.setLore(loreECO);
        itemECO.setItemMeta(itemmetaECO);

        if (plugin.getConfig().getBoolean("Main.Upgrade With XP") && player != null && player.hasPermission("EpicFarming.Upgrade.XP")) {
            setButton(11, itemXP, (event) -> {
                farm.upgrade(UpgradeType.EXPERIENCE, player);
                farm.view(player, true);
            });
        }

        setItem(13, item);

        if (plugin.getConfig().getBoolean("Main.Upgrade With Economy") && player != null && player.hasPermission("EpicFarming.Upgrade.ECO")) {
            setButton(15, itemECO, (event) -> {
                farm.upgrade(UpgradeType.ECONOMY, player);
                farm.view(player, true);
            });
        }

        // events
        this.setOnOpen((event) -> updateInventory());
        this.setDefaultAction((event) ->
                Bukkit.getScheduler().runTaskLater(plugin, this::updateFarm, 0L));
        this.setOnClose((event) -> farm.close());

    }

    public void updateInventory() {
        int j = (page - 1) * 27;
        for (int i = 27; i <= 54; i++) {
            if (farm.getItems().size() <= (j))
                setItem(i, null);
            else
                setItem(i, farm.getItems().get(j));
            j++;
        }
    }

    public void updateFarm() {
        List<ItemStack> items = new ArrayList<>();
        int start = 27 * (page - 1);
        int j = 27;
        for (int i = 0; i <= 27 * pages; i++) {
            if (i > start && i < start + 27) {
                ItemStack item = getItem(j);
                j ++;
                if (item != null && item.getType() != Material.AIR)
                    items.add(item);
            } else {
                if (i > farm.getItems().size())
                    continue;
                items.add(farm.getItems().get(i));
            }
        }
        farm.setItems(items);
    }
}
