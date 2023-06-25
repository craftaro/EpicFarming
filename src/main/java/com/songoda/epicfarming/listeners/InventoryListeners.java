package com.songoda.epicfarming.listeners;

import com.songoda.epicfarming.settings.Settings;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

public class InventoryListeners implements Listener {
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getCurrentItem() == null) {
            return;
        }
        if (event.getRawSlot() > event.getView().getTopInventory().getSize() - 1) {
            return;
        }
        if (!event.getCurrentItem().hasItemMeta()) {
            return;
        }

        if (event.getSlot() != 64537 &&
                event.getInventory().getType() == InventoryType.ANVIL &&
                event.getAction() != InventoryAction.NOTHING &&
                event.getCurrentItem().getType() != Material.AIR) {
            ItemStack item = event.getCurrentItem();
            if (item.getType() == Settings.FARM_BLOCK_MATERIAL.getMaterial().parseMaterial()) {
                event.setCancelled(true);
            }
        }
    }
}
