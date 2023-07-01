package com.songoda.epicfarming.listeners;

import com.craftaro.core.third_party.com.cryptomorin.xseries.XBlock;
import com.craftaro.core.third_party.com.cryptomorin.xseries.XMaterial;
import com.songoda.epicfarming.EpicFarming;
import com.songoda.epicfarming.farming.Farm;
import com.songoda.epicfarming.farming.levels.modules.Module;
import com.songoda.epicfarming.farming.levels.modules.ModuleAutoCollect;
import com.songoda.epicfarming.settings.Settings;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.entity.SheepRegrowWoolEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class EntityListeners implements Listener {
    private final EpicFarming plugin;

    public EntityListeners(EpicFarming plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (!entity.hasMetadata("EFA-TAGGED")) {
            return;
        }
        Location location = (Location) entity.getMetadata("EFA-TAGGED").get(0).value();
        Farm farm = this.plugin.getFarmManager().getFarm(location);

        boolean autoCollect = false;
        for (Module module : farm.getLevel().getRegisteredModules()) {
            if (module instanceof ModuleAutoCollect && ((ModuleAutoCollect) module).isEnabled(farm)) {
                autoCollect = true;
            }
        }

        if (autoCollect) {
            for (ItemStack item : event.getDrops()) {
                farm.addItem(item);
            }
            event.getDrops().clear();
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSpawn(ItemSpawnEvent event) {
        Item item = event.getEntity();

        if (item.getItemStack().getType() != Material.EGG) {
            return;
        }

        Location location = event.getEntity().getLocation();
        Collection<Entity> nearby = location.getWorld().getNearbyEntities(location, 0.01, 0.3, 0.01);

        Entity entity = null;
        for (Entity e : nearby) {
            if (e instanceof Player) {
                return;
            }
            if (e instanceof Chicken) {
                entity = e;
            }
        }

        if (ModuleAutoCollect.getTicksLived().containsKey(entity)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSpawn(SheepRegrowWoolEvent event) {
        if (ModuleAutoCollect.getTicksLived().containsKey(event.getEntity())) {
            event.setCancelled(true);
            Block block = event.getEntity().getLocation().getBlock().getRelative(BlockFace.DOWN);
            if (block.getType() == Material.DIRT) {
                XBlock.setType(block, XMaterial.GRASS_BLOCK);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlow(EntityExplodeEvent event) {
        List<Block> destroyed = event.blockList();
        Iterator<Block> it = destroyed.iterator();
        List<Block> toCancel = new ArrayList<>();
        while (it.hasNext()) {
            Block block = it.next();
            if (block.getType() != Settings.FARM_BLOCK_MATERIAL.getMaterial(XMaterial.END_ROD).parseMaterial()) {
                continue;
            }

            Farm farm = this.plugin.getFarmManager().getFarm(block.getLocation());
            if (farm == null) {
                continue;
            }

            toCancel.add(block);
        }

        for (Block block : toCancel) {
            event.blockList().remove(block);

            Farm farm = this.plugin.getFarmManager().removeFarm(block.getLocation());
            this.plugin.getDataManager().delete(farm);
            farm.forceMenuClose();

            this.plugin.getFarmTask().getCrops(farm, false);

            ItemStack item = this.plugin.makeFarmItem(farm.getLevel());

            block.setType(Material.AIR);
            block.getLocation().getWorld().dropItemNaturally(block.getLocation().add(.5, .5, .5), item);

            for (ItemStack itemStack : farm.getItems().toArray(new ItemStack[0])) {
                farm.getLocation().getWorld().dropItemNaturally(farm.getLocation().add(.5, .5, .5), itemStack);
            }
        }
    }
}
