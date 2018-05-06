package com.songoda.epicfarming.events;

import com.songoda.epicfarming.utils.Debugger;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;

public class EntityListeners implements Listener {

    @EventHandler
    public void onHop(InventoryMoveItemEvent e) {
        try {

        } catch (Exception ex) {
            Debugger.runReport(ex);
        }
    }

    @EventHandler
    public void ondrop(EntityDeathEvent e) {
        try {
        } catch (Exception ex) {
            Debugger.runReport(ex);
        }
    }
}