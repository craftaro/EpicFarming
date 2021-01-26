package com.songoda.epicfarming.listeners;

import com.songoda.core.compatibility.CompatibleMaterial;
import com.songoda.epicfarming.EpicFarming;
import com.songoda.epicfarming.farming.Farm;
import com.songoda.epicfarming.farming.FarmType;
import com.songoda.epicfarming.settings.Settings;
import com.songoda.epicfarming.tasks.FarmTask;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.PermissionAttachmentInfo;

/**
 * Created by songoda on 3/14/2017.
 */
@SuppressWarnings("Duplicates")
public class BlockListeners implements Listener {

    private final EpicFarming plugin;

    public BlockListeners(EpicFarming plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockFade(BlockFadeEvent e) {
        Farm farm = plugin.getFarmManager().checkForFarm(e.getBlock().getLocation());
        if (farm != null && farm.getFarmType() != FarmType.LIVESTOCK)
            e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onGrow(BlockGrowEvent e) {
        Farm farm = plugin.getFarmManager().checkForFarm(e.getBlock().getLocation());
        if (farm != null && farm.getFarmType() != FarmType.LIVESTOCK)
            e.setCancelled(true);
    }

    private int maxFarms(Player player) {
        int limit = -1;
        for (PermissionAttachmentInfo permissionAttachmentInfo : player.getEffectivePermissions()) {
            if (!permissionAttachmentInfo.getPermission().toLowerCase().startsWith("epicfarming.limit")) continue;
            int num = Integer.parseInt(permissionAttachmentInfo.getPermission().split("\\.")[2]);
            if (num > limit)
                limit = num;
        }
        return limit;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent e) {
        Material farmBlock = Settings.FARM_BLOCK_MATERIAL.getMaterial(CompatibleMaterial.END_ROD).getBlockMaterial();

        if (e.getPlayer().getItemInHand().getType() != farmBlock
                || plugin.getLevelFromItem(e.getItemInHand()) == 0 && !Settings.NON_COMMAND_FARMS.getBoolean())
            return;

        if (e.getBlockAgainst().getType() == farmBlock) e.setCancelled(true);

        int amt = 0;
        for (Farm farmm : plugin.getFarmManager().getFarms().values()) {
            if (farmm.getPlacedBy() == null || !farmm.getPlacedBy().equals(e.getPlayer().getUniqueId())) continue;
            amt++;
        }
        int limit = maxFarms(e.getPlayer());

        if (limit != -1 && amt >= limit) {
            e.setCancelled(true);
            plugin.getLocale().getMessage("event.limit.hit")
                    .processPlaceholder("limit", limit).sendPrefixedMessage(e.getPlayer());
            return;
        }

        Location location = e.getBlock().getLocation();
        if (e.getBlockPlaced().getType().equals(Material.MELON_SEEDS) || e.getBlockPlaced().getType().equals(Material.PUMPKIN_SEEDS)) {
            if (plugin.getFarmManager().checkForFarm(location) != null) {
                plugin.getLocale().getMessage("event.warning.noauto").sendPrefixedMessage(e.getPlayer());
            }
        }
        int level = plugin.getLevelFromItem(e.getItemInHand());
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (location.getBlock().getType() != farmBlock) return;

            Farm farm = new Farm(location, plugin.getLevelManager().getLevel(level == 0 ? 1 : level), e.getPlayer().getUniqueId());
            plugin.getFarmManager().addFarm(location, farm);
            plugin.getDataManager().createFarm(farm);

            farm.tillLand();
        }, 1);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.getBlock().getType() != Settings.FARM_BLOCK_MATERIAL.getMaterial(CompatibleMaterial.END_ROD).getMaterial())
            return;

        Farm farm = plugin.getFarmManager().removeFarm(event.getBlock().getLocation());

        if (farm == null) return;

        plugin.getDataManager().deleteFarm(farm);
        farm.forceMenuClose();

        FarmTask.getCrops(farm, false);

        event.setCancelled(true);

        ItemStack item = plugin.makeFarmItem(farm.getLevel());

        Block block = event.getBlock();

        block.setType(Material.AIR);
        block.getLocation().getWorld().dropItemNaturally(block.getLocation().add(.5, .5, .5), item);

        for (ItemStack itemStack : farm.getItems()) {
            farm.getLocation().getWorld().dropItemNaturally(farm.getLocation().add(.5, .5, .5), itemStack);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent event) {
        if (event.getBlock().getType() != Settings.FARM_BLOCK_MATERIAL.getMaterial(CompatibleMaterial.END_ROD).getMaterial())
            return;

        Farm farm = plugin.getFarmManager().removeFarm(event.getBlock().getLocation());

        if (farm == null) return;
        FarmTask.getCrops(farm, false);

        plugin.getDataManager().deleteFarm(farm);
        farm.forceMenuClose();

        event.setCancelled(true);

        ItemStack item = plugin.makeFarmItem(farm.getLevel());

        Block block = event.getBlock();

        block.setType(Material.AIR);
        block.getLocation().getWorld().dropItemNaturally(block.getLocation().add(.5, .5, .5), item);

        for (ItemStack itemStack : farm.getItems()) {
            farm.getLocation().getWorld().dropItemNaturally(farm.getLocation().add(.5, .5, .5), itemStack);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockFromToEventMonitor(BlockFromToEvent event) {
        // prevent water/lava/egg griefs
        if (plugin.getFarmManager().getFarm(event.getToBlock()) != null) {
            event.setCancelled(true);
        }
    }
}