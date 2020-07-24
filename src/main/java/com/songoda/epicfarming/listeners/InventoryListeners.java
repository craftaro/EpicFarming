package com.songoda.epicfarming.listeners;

import com.songoda.epicfarming.settings.Settings;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

/**
 * Created by songoda on 3/14/2017.
 */
public class InventoryListeners implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        if (event.getCurrentItem() == null) return;

        if (event.getRawSlot() > event.getView().getTopInventory().getSize() - 1) return;

        if (!event.getCurrentItem().hasItemMeta()) return;

        if (event.getSlot() != 64537
                && event.getInventory().getType() == InventoryType.ANVIL
                && event.getAction() != InventoryAction.NOTHING
                && event.getCurrentItem().getType() != Material.AIR) {
            ItemStack item = event.getCurrentItem();
            if (item.getType() == Settings.FARM_BLOCK_MATERIAL.getMaterial().getMaterial()) {
                event.setCancelled(true);
            }
        }
    }
}
