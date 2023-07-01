package com.songoda.epicfarming.listeners;

import com.craftaro.core.third_party.com.cryptomorin.xseries.XMaterial;
import com.songoda.epicfarming.EpicFarming;
import com.songoda.epicfarming.farming.Farm;
import com.songoda.epicfarming.farming.FarmType;
import com.songoda.epicfarming.settings.Settings;
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

public class BlockListeners implements Listener {
    private final EpicFarming plugin;

    public BlockListeners(EpicFarming plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockFade(BlockFadeEvent e) {
        Farm farm = this.plugin.getFarmManager().checkForFarm(e.getBlock().getLocation());
        if (farm != null && farm.getFarmType() != FarmType.LIVESTOCK) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onGrow(BlockGrowEvent e) {
        Farm farm = this.plugin.getFarmManager().checkForFarm(e.getBlock().getLocation());
        if (farm != null && farm.getFarmType() != FarmType.LIVESTOCK) {
            e.setCancelled(true);
        }
    }

    private int maxFarms(Player player) {
        int limit = -1;
        for (PermissionAttachmentInfo permissionAttachmentInfo : player.getEffectivePermissions()) {
            if (!permissionAttachmentInfo.getPermission().toLowerCase().startsWith("epicfarming.limit")) {
                continue;
            }
            int num = Integer.parseInt(permissionAttachmentInfo.getPermission().split("\\.")[2]);
            if (num > limit) {
                limit = num;
            }
        }
        return limit;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent e) {
        Material farmBlock = Settings.FARM_BLOCK_MATERIAL.getMaterial(XMaterial.END_ROD).parseMaterial();

        if (e.getPlayer().getItemInHand().getType() != farmBlock || (this.plugin.getLevelFromItem(e.getItemInHand()) == 0 && !Settings.NON_COMMAND_FARMS.getBoolean())) {
            return;
        }

        if (e.getBlockAgainst().getType() == farmBlock) {
            e.setCancelled(true);
        }

        int amt = 0;
        for (Farm farm : this.plugin.getFarmManager().getFarms().values()) {
            if (farm.getPlacedBy() == null || !farm.getPlacedBy().equals(e.getPlayer().getUniqueId())) {
                continue;
            }
            amt++;
        }
        int limit = maxFarms(e.getPlayer());

        if (limit != -1 && amt >= limit) {
            e.setCancelled(true);
            this.plugin.getLocale().getMessage("event.limit.hit")
                    .processPlaceholder("limit", limit).sendPrefixedMessage(e.getPlayer());
            return;
        }

        Location location = e.getBlock().getLocation();
        if (e.getBlockPlaced().getType() == Material.MELON_SEEDS || e.getBlockPlaced().getType() == Material.PUMPKIN_SEEDS) {
            if (this.plugin.getFarmManager().checkForFarm(location) != null) {
                this.plugin.getLocale().getMessage("event.warning.noauto").sendPrefixedMessage(e.getPlayer());
            }
        }
        int level = this.plugin.getLevelFromItem(e.getItemInHand());
        Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
            if (location.getBlock().getType() != farmBlock) {
                return;
            }

            Farm farm = new Farm(location, this.plugin.getLevelManager().getLevel(level == 0 ? 1 : level), e.getPlayer().getUniqueId(), FarmType.BOTH);
            this.plugin.getFarmManager().addFarm(location, farm);
            this.plugin.getDataManager().save(farm);

            farm.tillLand();
        }, 1);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.getBlock().getType() != Settings.FARM_BLOCK_MATERIAL.getMaterial(XMaterial.END_ROD).parseMaterial()) {
            return;
        }

        Farm farm = this.plugin.getFarmManager().removeFarm(event.getBlock().getLocation());
        if (farm == null) {
            return;
        }

        this.plugin.getDataManager().delete(farm);
        farm.forceMenuClose();

        this.plugin.getFarmTask().getCrops(farm, false);

        event.setCancelled(true);

        ItemStack item = this.plugin.makeFarmItem(farm.getLevel());

        Block block = event.getBlock();

        block.setType(Material.AIR);
        block.getLocation().getWorld().dropItemNaturally(block.getLocation().add(.5, .5, .5), item);

        for (ItemStack itemStack : farm.getItems().toArray(new ItemStack[0])) {
            farm.getLocation().getWorld().dropItemNaturally(farm.getLocation().add(.5, .5, .5), itemStack);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent event) {
        if (event.getBlock().getType() != Settings.FARM_BLOCK_MATERIAL.getMaterial(XMaterial.END_ROD).parseMaterial()) {
            return;
        }

        Farm farm = this.plugin.getFarmManager().removeFarm(event.getBlock().getLocation());

        if (farm == null) {
            return;
        }
        this.plugin.getFarmTask().getCrops(farm, false);

        this.plugin.getDataManager().delete(farm);
        farm.forceMenuClose();

        event.setCancelled(true);

        ItemStack item = this.plugin.makeFarmItem(farm.getLevel());

        Block block = event.getBlock();

        block.setType(Material.AIR);
        block.getLocation().getWorld().dropItemNaturally(block.getLocation().add(.5, .5, .5), item);

        for (ItemStack itemStack : farm.getItems().toArray(new ItemStack[0])) {
            farm.getLocation().getWorld().dropItemNaturally(farm.getLocation().add(.5, .5, .5), itemStack);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockFromToEventMonitor(BlockFromToEvent event) {
        // prevent water/lava/egg griefs
        if (this.plugin.getFarmManager().getFarm(event.getToBlock()) != null) {
            event.setCancelled(true);
        }
    }
}
