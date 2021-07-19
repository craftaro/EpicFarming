package com.songoda.epicfarming.gui;

import com.songoda.core.compatibility.CompatibleMaterial;
import com.songoda.core.gui.CustomizableGui;
import com.songoda.core.gui.GuiUtils;
import com.songoda.epicfarming.EpicFarming;
import com.songoda.epicfarming.boost.BoostData;
import com.songoda.epicfarming.farming.Farm;
import com.songoda.epicfarming.farming.FarmType;
import com.songoda.epicfarming.farming.UpgradeType;
import com.songoda.epicfarming.farming.levels.Level;
import com.songoda.epicfarming.farming.levels.modules.Module;
import com.songoda.epicfarming.settings.Settings;
import com.songoda.epicfarming.utils.Methods;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class OverviewGui extends CustomizableGui {

    private static final EpicFarming plugin = EpicFarming.getInstance();
    private final Farm farm;
    private final Level level;
    private final Player player;

    private int task;

    public OverviewGui(Farm farm, Player player) {
        super(plugin, "overview");
        this.farm = farm;
        this.level = farm.getLevel();
        this.player = player;
        this.setRows(6);
        this.setTitle(Methods.formatName(level.getLevel()));
        this.setAcceptsItems(true);
        this.setUnlockedRange(3, 0, 5, 8);

        ItemStack glass1 = GuiUtils.getBorderItem(Settings.GLASS_TYPE_1.getMaterial());
        ItemStack glass2 = GuiUtils.getBorderItem(Settings.GLASS_TYPE_2.getMaterial());
        ItemStack glass3 = GuiUtils.getBorderItem(Settings.GLASS_TYPE_3.getMaterial());

        this.setDefaultItem(null);

        mirrorFill("mirrorfill_1", 0, 0, false, true, glass2);
        mirrorFill("mirrorfill_2", 0, 1, false, true, glass2);
        mirrorFill("mirrorfill_3", 0, 2, false, true, glass3);
        mirrorFill("mirrorfill_4", 1, 0, false, true, glass2);
        mirrorFill("mirrorfill_5", 1, 1, false, true, glass3);
        mirrorFill("mirrorfill_6", 2, 0, false, true, glass2);
        mirrorFill("mirrorfill_7", 2, 1, false, true, glass2);
        mirrorFill("mirrorfill_8", 2, 2, false, true, glass3);

        mirrorFill("mirrorfill_9", 0, 3, false, true, glass1);
        mirrorFill("mirrorfill_10", 0, 4, false, false, glass1);
        mirrorFill("mirrorfill_11", 1, 3, false, true, glass1);
        mirrorFill("mirrorfill_12", 1, 2, false, true, glass1);
        mirrorFill("mirrorfill_13", 2, 3, false, true, glass1);
        mirrorFill("mirrorfill_14", 2, 4, false, false, glass1);

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

        setItem("farm", 13, GuiUtils.createButtonItem(Settings.FARM_BLOCK_MATERIAL.getMaterial(CompatibleMaterial.END_ROD),
                plugin.getLocale().getMessage("general.nametag.farm")
                        .processPlaceholder("level", level.getLevel()).getMessage(),
                farmLore));

        if (player != null && Settings.UPGRADE_WITH_XP.getBoolean() && player.hasPermission("EpicFarming.Upgrade.XP")) {

            setButton("xp", 11, GuiUtils.createButtonItem(Settings.XP_ICON.getMaterial(CompatibleMaterial.EXPERIENCE_BOTTLE),
                            plugin.getLocale().getMessage("interface.button.upgradewithxp").getMessage(),
                            nextLevel != null
                                    ? plugin.getLocale().getMessage("interface.button.upgradewithxplore")
                                    .processPlaceholder("cost", nextLevel.getCostExperience()).getMessage()
                                    : plugin.getLocale().getMessage("event.upgrade.maxed").getMessage()),
                    event -> {
                        farm.upgrade(UpgradeType.EXPERIENCE, player);
                        onClose(guiManager, player);
                        farm.view(player, true);
                    });
        }

        if (Settings.UPGRADE_WITH_ECONOMY.getBoolean() && player != null && player.hasPermission("EpicFarming.Upgrade.ECO")) {

            setButton("eco", 15, GuiUtils.createButtonItem(Settings.ECO_ICON.getMaterial(CompatibleMaterial.SUNFLOWER),
                    plugin.getLocale().getMessage("interface.button.upgradewitheconomy").getMessage(),
                    nextLevel != null
                            ? plugin.getLocale().getMessage("interface.button.upgradewitheconomylore")
                            .processPlaceholder("cost", Methods.formatEconomy(nextLevel.getCostEconomy())).getMessage()
                            : plugin.getLocale().getMessage("event.upgrade.maxed").getMessage()), (event) -> {
                farm.upgrade(UpgradeType.ECONOMY, player);
                onClose(guiManager, player);
                farm.view(player, true);
            });
        }

        Material farmTypeMaterial = CompatibleMaterial.WHEAT.getMaterial();
        if (farm.getFarmType() == FarmType.LIVESTOCK)
            farmTypeMaterial = CompatibleMaterial.BEEF.getMaterial();
        else if (farm.getFarmType() == FarmType.BOTH)
            farmTypeMaterial = CompatibleMaterial.GOLD_NUGGET.getMaterial();

        ItemStack farmType = new ItemStack(farmTypeMaterial, 1);
        ItemMeta farmTypeMeta = farmType.getItemMeta();
        farmTypeMeta.setDisplayName(plugin.getLocale().getMessage("interface.button.farmtype")
                .processPlaceholder("type", farm.getFarmType().translate())
                .getMessage());
        farmTypeMeta.setLore(Collections.singletonList(plugin.getLocale().getMessage("interface.button.farmtypelore")
                .getMessage()));
        farmType.setItemMeta(farmTypeMeta);

        Map<Integer, Integer[]> layouts = new HashMap<>();
        layouts.put(1, new Integer[] {22});
        layouts.put(2, new Integer[] {22, 4});
        layouts.put(3, new Integer[] {22, 3, 5});
        layouts.put(4, new Integer[] {23, 3, 5, 21});
        layouts.put(5, new Integer[] {23, 3, 5, 21, 22});
        layouts.put(6, new Integer[] {23, 3, 4, 5, 21, 22});
        layouts.put(7, new Integer[] {23, 3, 4, 5, 21, 22, 12});
        layouts.put(8, new Integer[] {23, 3, 4, 5, 21, 22, 12, 14});
        layouts.put(9, new Integer[] {23, 3, 4, 5, 21, 22, 12, 14, 20});
        layouts.put(10, new Integer[] {23, 3, 4, 5, 21, 22, 12, 14, 20, 24});

        List<Module> modules = level.getRegisteredModules().stream().filter(module ->
                module.getGUIButton(farm) != null).collect(Collectors.toList());

        int amount = modules.size();

        if (amount > 0) amount++;

        Integer[] layout = layouts.get(amount);

        for (int ii = 0; ii < amount; ii++) {
            int slot = layout[ii];
            if (ii == 0 && level.getRegisteredModules().stream().map(Module::getName).anyMatch(s -> s.equals("AutoCollect"))) {
                if (level.getRegisteredModules().stream().map(Module::getName).anyMatch(s -> s.equals("AutoButcher")
                        || s.equals("AutoBreeding")))
                    setButton("toggle", slot, farmType,
                            (event) -> {
                                farm.toggleFarmType();
                                if (farm.getFarmType() != FarmType.LIVESTOCK)
                                    farm.tillLand();
                                showPage();
                            });
            } else {
                if (modules.isEmpty()) break;

                Module module = modules.get(0);
                modules.remove(module);
                setButton("module_" + module.getName().toLowerCase(), slot, module.getGUIButton(farm),
                        (event) -> module.runButtonPress(player, farm, event.clickType));
            }
        }
    }

    private void runTask() {
        task = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            updateFarm();
            showPage();
        }, 2L, 1L);
    }

    public void updateInventory() {
        List<ItemStack> items = farm.getItems();

        int j = (page - 1) * 27;
        for (int i = 27; i <= 54; ++i) {
            if (items.size() <= j) {
                setItem(i, null);
            } else {
                setItem(i, items.get(j));
            }

            ++j;
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
                if (item != null && item.getType() != Material.AIR) {
                    items.add(item);
                }
            } else {
                if (i >= farm.getItems().size()) {
                    continue;
                }
                items.add(farm.getItems().get(i));
            }
        }
        farm.setItems(items);
    }
}
