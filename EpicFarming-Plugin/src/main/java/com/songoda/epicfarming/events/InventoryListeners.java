package com.songoda.epicfarming.events;

import com.songoda.epicfarming.EpicFarmingPlugin;
import com.songoda.epicfarming.api.farming.UpgradeType;
import com.songoda.epicfarming.farming.EFarm;
import com.songoda.epicfarming.player.PlayerData;
import com.songoda.epicfarming.utils.Debugger;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
/**
 * Created by songoda on 3/14/2017.
 */
public class InventoryListeners implements Listener {

    private EpicFarmingPlugin instance;

    public InventoryListeners(EpicFarmingPlugin instance) {
        this.instance = instance;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        try {
            if (instance.getPlayerActionManager().getPlayerAction((Player)event.getWhoClicked()).getFarm() == null
                    || event.getInventory() == null || event.getRawSlot() >= event.getView().getTopInventory().getSize()) return;

            if (event.getInventory().getType() != InventoryType.CHEST) return;

            PlayerData playerData = instance.getPlayerActionManager().getPlayerAction((Player)event.getWhoClicked());
            EFarm farm = playerData.getFarm();
            if (event.getSlot() <= 26) {
                event.setCancelled(true);
            }

            Player player = (Player)event.getWhoClicked();

            if (event.getSlot() == 11 && player.hasPermission("EpicFarming.Upgrade.XP")) {
                if (!event.getCurrentItem().getItemMeta().getDisplayName().equals("§l")) {
                    farm.upgrade(UpgradeType.EXPERIENCE, player);
                    player.closeInventory();
                }
            } else if (event.getSlot() == 15 && player.hasPermission("EpicFarming.Upgrade.ECO")) {
                if (!event.getCurrentItem().getItemMeta().getDisplayName().equals("§l")) {
                    farm.upgrade(UpgradeType.ECONOMY, player);
                    player.closeInventory();
                }
            }

        } catch (Exception ex) {
            Debugger.runReport(ex);
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        try {
            instance.getPlayerActionManager().getPlayerAction((Player)event.getPlayer()).setFarm(null);
        } catch (Exception ex) {
            Debugger.runReport(ex);
        }
    }
}