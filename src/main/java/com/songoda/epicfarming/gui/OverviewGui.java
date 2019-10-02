package com.songoda.epicfarming.gui;

import com.songoda.core.compatibility.CompatibleMaterial;
import com.songoda.core.gui.Gui;
import com.songoda.core.gui.GuiUtils;
import com.songoda.epicfarming.EpicFarming;
import com.songoda.epicfarming.boost.BoostData;
import com.songoda.epicfarming.farming.Farm;
import com.songoda.epicfarming.farming.Level;
import com.songoda.epicfarming.farming.UpgradeType;
import com.songoda.epicfarming.settings.Settings;
import com.songoda.epicfarming.utils.Methods;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class OverviewGui extends Gui {

    private final EpicFarming plugin;
    private final Farm farm;
    private final Level level;
    private final Player player;

    private int task;

    public OverviewGui(Farm farm, Player player) {
        this.plugin = EpicFarming.getInstance();
        this.farm = farm;
        this.level = farm.getLevel();
        this.player = player;
        this.setRows(6);
        this.setTitle(Methods.formatName(level.getLevel(), false));
        this.setAcceptsItems(true);
        this.setUnlockedRange(3, 0, 5, 8);

        ItemStack glass1 = GuiUtils.getBorderItem(Settings.GLASS_TYPE_1.getMaterial());
        ItemStack glass2 = GuiUtils.getBorderItem(Settings.GLASS_TYPE_2.getMaterial());
        ItemStack glass3 = GuiUtils.getBorderItem(Settings.GLASS_TYPE_3.getMaterial());

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
            setPages(level.getPages());
            setPrevPage(2, 0, GuiUtils.createButtonItem(CompatibleMaterial.ARROW, plugin.getLocale().getMessage("general.interface.previous").getMessage()));
            setNextPage(2, 8, GuiUtils.createButtonItem(CompatibleMaterial.ARROW, plugin.getLocale().getMessage("general.interface.next").getMessage()));
            setOnPage((event) -> updateInventory());
        }

        // events
        this.setOnOpen((event) -> updateInventory());
        this.setDefaultAction((event) ->
                Bukkit.getScheduler().runTaskLater(plugin, this::updateFarm, 0L));
        runTask();
        this.setOnClose((event) -> {
            updateFarm();
            farm.close();
            Bukkit.getScheduler().cancelTask(task);
        });

        showPage();
    }

    private void showPage() {

        Level nextLevel = plugin.getLevelManager().getHighestLevel().getLevel() > level.getLevel() ? plugin.getLevelManager().getLevel(level.getLevel() + 1) : null;

        List<String> farmLore = level.getDescription();
        farmLore.add("");
        if (nextLevel == null) farmLore.add(plugin.getLocale().getMessage("event.upgrade.maxed").getMessage());
        else {
            farmLore.add(plugin.getLocale().getMessage("interface.button.level")
                    .processPlaceholder("level", nextLevel.getLevel()).getMessage());
            farmLore.addAll(nextLevel.getDescription());
        }

        BoostData boostData = plugin.getBoostManager().getBoost(farm.getPlacedBy());
        if (boostData != null) {
            String[] parts = plugin.getLocale().getMessage("interface.button.boostedstats")
                    .processPlaceholder("amount", Integer.toString(boostData.getMultiplier()))
                    .processPlaceholder("time", Methods.makeReadable(boostData.getEndTime() - System.currentTimeMillis()))
                    .getMessage().split("\\|");
            farmLore.add("");
            for (String line : parts)
                farmLore.add(Methods.formatText(line));
        }

        setItem(13, GuiUtils.createButtonItem(Settings.FARM_BLOCK_MATERIAL.getMaterial(CompatibleMaterial.END_ROD),
                plugin.getLocale().getMessage("general.nametag.farm")
                .processPlaceholder("level", level.getLevel()).getMessage(),
                farmLore));

        if (player != null && Settings.UPGRADE_WITH_XP.getBoolean() && player.hasPermission("EpicFarming.Upgrade.XP")) {

            setButton(11, GuiUtils.createButtonItem(Settings.XP_ICON.getMaterial(CompatibleMaterial.EXPERIENCE_BOTTLE),
                    plugin.getLocale().getMessage("interface.button.upgradewithxp").getMessage(),
                    nextLevel != null
                            ? plugin.getLocale().getMessage("interface.button.upgradewithxplore")
                            .processPlaceholder("cost", nextLevel.getCostExperiance()).getMessage()
                            : plugin.getLocale().getMessage("event.upgrade.maxed").getMessage()),
                    event -> {
                        farm.upgrade(UpgradeType.EXPERIENCE, player);
                        farm.view(player, true);
                    });

        }

        if (plugin.getConfig().getBoolean("Main.Upgrade With Economy") && player != null && player.hasPermission("EpicFarming.Upgrade.ECO")) {

            setButton(15, GuiUtils.createButtonItem(Settings.ECO_ICON.getMaterial(CompatibleMaterial.SUNFLOWER),
                    plugin.getLocale().getMessage("interface.button.upgradewitheconomy").getMessage(),
                    nextLevel != null
                            ? plugin.getLocale().getMessage("interface.button.upgradewitheconomylore")
                            .processPlaceholder("cost", Methods.formatEconomy(nextLevel.getCostEconomy())).getMessage()
                            : plugin.getLocale().getMessage("event.upgrade.maxed").getMessage()), (event) -> {
                        farm.upgrade(UpgradeType.ECONOMY, player);
                        farm.view(player, true);
                    });

        }
    }

    private void runTask() {
        task = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this::updateFarm, 2L, 1L);
    }

    public void updateInventory() {
        List<ItemStack> items = farm.getItems();

        int j = (page - 1) * 27;
        for (int i = 27; i <= 54; i++) {
            if (items.size() <= (j))
                setItem(i, null);
            else
                setItem(i, items.get(j));
            j++;
        }
    }

    private void updateFarm() {
        List<ItemStack> items = new ArrayList<>();
        int start = 27 * (page - 1);
        int j = 27;
        for (int i = 0; i <= 27 * pages; i++) {
            if (i >= start && i < start + 27) {
                ItemStack item = getItem(j);
                j++;
                if (item != null && item.getType() != Material.AIR)
                    items.add(item);
            } else {
                if (i >= farm.getItems().size())
                    continue;
                items.add(farm.getItems().get(i));
            }
        }
        farm.setItems(items);
    }
}