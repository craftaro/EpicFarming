package com.songoda.epicfarming.farming;

import com.songoda.core.compatibility.CompatibleMaterial;
import com.songoda.core.compatibility.CompatibleSound;
import com.songoda.core.compatibility.ServerVersion;
import com.songoda.core.hooks.EconomyManager;
import com.songoda.epicfarming.EpicFarming;
import com.songoda.epicfarming.gui.OverviewGui;
import com.songoda.epicfarming.settings.Setting;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class Farm {

    private static final Random random = new Random();
    private final List<Block> cachedCrops = new ArrayList<>();
    private Location location;
    private Level level;
    private List<ItemStack> items = new ArrayList<>();
    private OverviewGui opened = null;
    private UUID placedBy;
    private UUID viewing = null;
    private long lastCached = 0;

    public Farm(Location location, Level level, UUID placedBy) {
        this.location = location;
        this.level = level;
        this.placedBy = placedBy;
    }

    public void view(Player player, boolean force) {
        if (!player.hasPermission("epicfarming.view") && !force)
            return;

        if (opened != null && !force) return;

        opened = new OverviewGui(this, player);

        EpicFarming.getInstance().getGuiManager().showGUI(player, opened);
    }

    public void upgrade(UpgradeType type, Player player) {
        EpicFarming instance = EpicFarming.getInstance();
        if (instance.getLevelManager().getLevels().containsKey(this.level.getLevel() + 1)) {
            Level level = instance.getLevelManager().getLevel(this.level.getLevel() + 1);
            int cost;
            if (type == UpgradeType.EXPERIENCE) {
                cost = level.getCostExperiance();
            } else {
                cost = level.getCostEconomy();
            }

            if (type == UpgradeType.ECONOMY) {
                if (EconomyManager.isEnabled()) {
                    if (EconomyManager.hasBalance(player, cost)) {
                        EconomyManager.withdrawBalance(player, cost);
                        upgradeFinal(level, player);
                    } else {
                        instance.getLocale().getMessage("event.upgrade.cannotafford").sendPrefixedMessage(player);
                    }
                } else {
                    player.sendMessage("Vault is not installed.");
                }
            } else if (type == UpgradeType.EXPERIENCE) {
                if (player.getLevel() >= cost || player.getGameMode() == GameMode.CREATIVE) {
                    if (player.getGameMode() != GameMode.CREATIVE) {
                        player.setLevel(player.getLevel() - cost);
                    }
                    upgradeFinal(level, player);
                } else {
                    instance.getLocale().getMessage("event.upgrade.cannotafford").sendPrefixedMessage(player);
                }
            }
        }
    }

    private void upgradeFinal(Level level, Player player) {
        EpicFarming instance = EpicFarming.getInstance();
        this.level = level;
        if (instance.getLevelManager().getHighestLevel() != level) {
            instance.getLocale().getMessage("event.upgrade.success")
                    .processPlaceholder("level", level.getLevel()).sendPrefixedMessage(player);
        } else {
            instance.getLocale().getMessage("event.upgrade.successmaxed")
                    .processPlaceholder("level", level.getLevel()).sendPrefixedMessage(player);
        }
        Location loc = location.clone().add(.5, .5, .5);
        tillLand(location);
        if (!ServerVersion.isServerVersionAtLeast(ServerVersion.V1_12)) return;

        player.getWorld().spawnParticle(org.bukkit.Particle.valueOf(instance.getConfig().getString("Main.Upgrade Particle Type")), loc, 200, .5, .5, .5);

        if (instance.getLevelManager().getHighestLevel() != level) {
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.6F, 15.0F);
        } else {
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 2F, 25.0F);

            if (!ServerVersion.isServerVersionAtLeast(ServerVersion.V1_13)) return;

            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 2F, 25.0F);
            Bukkit.getScheduler().scheduleSyncDelayedTask(instance, () -> player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1.2F, 35.0F), 5L);
            Bukkit.getScheduler().scheduleSyncDelayedTask(instance, () -> player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1.8F, 35.0F), 10L);
        }
    }

    public boolean tillLand(Location location) {
        if (Setting.DISABLE_AUTO_TIL_LAND.getBoolean()) return true;
        Block block = location.getBlock();
        int radius = level.getRadius();
        int bx = block.getX();
        int by = block.getY();
        int bz = block.getZ();
        for (int fx = -radius; fx <= radius; fx++) {
            for (int fy = -2; fy <= 1; fy++) {
                for (int fz = -radius; fz <= radius; fz++) {
                    Block b2 = block.getWorld().getBlockAt(bx + fx, by + fy, bz + fz);

                    // ToDo: enum for all flowers.
                    if (b2.getType() == CompatibleMaterial.TALL_GRASS.getMaterial()
                            || b2.getType() == CompatibleMaterial.GRASS.getMaterial()
                            || b2.getType().name().contains("TULIP")
                            || b2.getType().name().contains("ORCHID")
                            || b2.getType() == CompatibleMaterial.AZURE_BLUET.getMaterial()
                            || b2.getType() == CompatibleMaterial.ALLIUM.getMaterial()
                            || b2.getType() == CompatibleMaterial.POPPY.getMaterial()
                            || b2.getType() == CompatibleMaterial.DANDELION.getMaterial()) {
                        Bukkit.getScheduler().runTaskLater(EpicFarming.getInstance(), () -> {
                            b2.getRelative(BlockFace.DOWN).setType(CompatibleMaterial.FARMLAND.getMaterial());
                            b2.breakNaturally();
                            b2.getWorld().playSound(b2.getLocation(), CompatibleSound.BLOCK_GRASS_BREAK.getSound(), 10, 15);
                        }, random.nextInt(30) + 1);
                    }
                    if ((b2.getType() == CompatibleMaterial.GRASS_BLOCK.getMaterial()
                            || b2.getType() == Material.DIRT) && b2.getRelative(BlockFace.UP).getType() == Material.AIR) {
                        Bukkit.getScheduler().runTaskLater(EpicFarming.getInstance(), () -> {
                            b2.setType(CompatibleMaterial.FARMLAND.getMaterial());
                            b2.getWorld().playSound(b2.getLocation(), CompatibleSound.BLOCK_GRASS_BREAK.getSound(), 10, 15);
                        }, random.nextInt(30) + 1);
                    }

                }
            }
        }
        return false;
    }

    public List<ItemStack> getItems() {
        return Collections.unmodifiableList(items);
    }

    public void addItem(ItemStack toAdd) {
        for (ItemStack item : getItems()) {
            if (item.getType() != toAdd.getType()
                    || item.getAmount() + toAdd.getAmount() > item.getMaxStackSize()) continue;
            item.setAmount(item.getAmount() + toAdd.getAmount());
            if (opened != null)
                opened.updateInventory();
            return;
        }
        items.add(toAdd);
        if (opened != null)
            opened.updateInventory();
    }


    public void removeMaterial(Material material, int amount) {
        for (ItemStack item : getItems()) {
            if (material == item.getType()) {
                item.setAmount(item.getAmount() - amount);

                if (item.getAmount() <= 0)
                    this.items.remove(item);
                if (opened != null)
                    opened.updateInventory();
                return;
            }
        }
    }

    public boolean willFit(ItemStack item) {
        if (items.size() < 27 * level.getPages()) return true;

        for (ItemStack stack : items) {
            if (stack.isSimilar(item) && stack.getAmount() < stack.getMaxStackSize()) {
                return true;
            }
        }
        return false;
    }

    public void setItems(List<ItemStack> items) {
        this.items.clear();
        this.items.addAll(items);
        if (opened != null)
            opened.updateInventory();
    }

    public UUID getViewing() {
        return viewing;
    }

    public void setViewing(UUID viewing) {
        this.viewing = viewing;
    }

    public void addCachedCrop(Block block) {
        cachedCrops.add(block);
    }

    public void removeCachedCrop(Block block) {
        cachedCrops.remove(block);
    }

    public List<Block> getCachedCrops() {
        return new ArrayList<>(cachedCrops);
    }

    public void clearCache() {
        cachedCrops.clear();
    }

    public long getLastCached() {
        return lastCached;
    }

    public void setLastCached(long lastCached) {
        this.lastCached = lastCached;
    }

    public boolean isInLoadedChunk() {
        return location != null && location.getWorld() != null && location.getWorld().isChunkLoaded(((int) location.getX()) >> 4, ((int) location.getZ()) >> 4);
    }

    public Location getLocation() {
        return location.clone();
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public UUID getPlacedBy() {
        return placedBy;
    }

    public Level getLevel() {
        return level;
    }

    public void close() {
        this.opened = null;
    }
}