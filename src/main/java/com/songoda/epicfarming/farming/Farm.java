package com.songoda.epicfarming.farming;

import com.craftaro.core.compatibility.CompatibleMaterial;
import com.craftaro.core.compatibility.CompatibleParticleHandler;
import com.craftaro.core.hooks.EconomyManager;
import com.craftaro.core.hooks.ProtectionManager;
import com.craftaro.core.third_party.com.cryptomorin.xseries.XBlock;
import com.craftaro.core.third_party.com.cryptomorin.xseries.XMaterial;
import com.craftaro.core.third_party.com.cryptomorin.xseries.XSound;
import com.songoda.epicfarming.EpicFarming;
import com.songoda.epicfarming.farming.levels.Level;
import com.songoda.epicfarming.gui.OverviewGui;
import com.songoda.epicfarming.settings.Settings;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class Farm {
    // This is the unique identifier for this farm.
    // It is reset on every plugin load.
    private final UUID uniqueId = UUID.randomUUID();

    // Id for database usage.
    private int id;

    private boolean needsToBeSaved = false;

    private static final Random random = new Random();
    private final List<Block> cachedCrops = new ArrayList<>();
    private final List<ItemStack> items = new ArrayList<>();
    private Location location;
    private Level level;
    private OverviewGui opened = null;
    private final UUID placedBy;
    private UUID viewing = null;
    private long lastCached = 0;

    private FarmType farmType = FarmType.BOTH;

    private final Map<String, Object> moduleCache = new HashMap<>();

    public Farm(Location location, Level level, UUID placedBy) {
        this.location = location;
        this.level = level;
        this.placedBy = placedBy;
    }

    public void view(Player player, boolean force) {
        if (!player.hasPermission("epicfarming.view") && !force) {
            return;
        }

        if (this.opened != null && !force) {
            return;
        }

        if (Settings.USE_PROTECTION_PLUGINS.getBoolean() && !ProtectionManager.canInteract(player, this.location)) {
            player.sendMessage(EpicFarming.getInstance().getLocale().getMessage("event.general.protected").getPrefixedMessage());
            return;
        }

        this.opened = new OverviewGui(this, player);

        EpicFarming.getInstance().getGuiManager().showGUI(player, this.opened);
    }

    public void forceMenuClose() {
        if (this.opened == null) {
            return;
        }

        this.opened.close();
    }

    public void upgrade(UpgradeType type, Player player) {
        EpicFarming instance = EpicFarming.getInstance();
        if (instance.getLevelManager().getLevels().containsKey(this.level.getLevel() + 1)) {
            Level level = instance.getLevelManager().getLevel(this.level.getLevel() + 1);
            int cost;
            if (type == UpgradeType.EXPERIENCE) {
                cost = level.getCostExperience();
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
        instance.getDataManager().updateFarm(this);
        if (instance.getLevelManager().getHighestLevel() != level) {
            instance.getLocale().getMessage("event.upgrade.success")
                    .processPlaceholder("level", level.getLevel()).sendPrefixedMessage(player);
        } else {
            instance.getLocale().getMessage("event.upgrade.successmaxed")
                    .processPlaceholder("level", level.getLevel()).sendPrefixedMessage(player);
        }
        tillLand();
        Location loc = this.location.clone().add(.5, .5, .5);
        CompatibleParticleHandler.spawnParticles(Settings.PARTICLE_TYPE.getString(), loc, 200, .5, .5, .5);

        if (instance.getLevelManager().getHighestLevel() != level) {
            XSound.ENTITY_PLAYER_LEVELUP.play(player, .6f, 15);
        } else {
            XSound.ENTITY_PLAYER_LEVELUP.play(player, 2, 25);

            XSound.BLOCK_NOTE_BLOCK_CHIME.play(player, 2, 25);
            Bukkit.getScheduler().scheduleSyncDelayedTask(instance, () -> XSound.BLOCK_NOTE_BLOCK_CHIME.play(player, 1.2f, 35), 5);
            Bukkit.getScheduler().scheduleSyncDelayedTask(instance, () -> XSound.BLOCK_NOTE_BLOCK_CHIME.play(player, 1.8f, 35), 10);
        }
    }

    public boolean tillLand() {
        if (Settings.DISABLE_AUTO_TIL_LAND.getBoolean()) {
            return true;
        }
        Block block = this.location.getBlock();
        int radius = this.level.getRadius();
        int bx = block.getX();
        int by = block.getY();
        int bz = block.getZ();
        for (int fx = -radius; fx <= radius; fx++) {
            for (int fy = -2; fy <= 1; fy++) {
                for (int fz = -radius; fz <= radius; fz++) {
                    Block b2 = block.getWorld().getBlockAt(bx + fx, by + fy, bz + fz);

                    // ToDo: enum for all flowers.
                    if (Settings.BREAKABLE_BLOCKS.getStringList().contains(CompatibleMaterial.getMaterial(b2.getType()).get().name())) {
                        Bukkit.getScheduler().runTaskLater(EpicFarming.getInstance(), () -> {
                            XBlock.setType(b2.getRelative(BlockFace.DOWN), XMaterial.FARMLAND);
                            b2.breakNaturally();
                            XSound.BLOCK_GRASS_BREAK.play(b2.getLocation(), 10, 15);
                        }, random.nextInt(30) + 1);
                    }
                    if ((b2.getType() == XMaterial.GRASS_BLOCK.parseMaterial()
                            || b2.getType() == Material.DIRT) && b2.getRelative(BlockFace.UP).getType() == Material.AIR) {
                        Bukkit.getScheduler().runTaskLater(EpicFarming.getInstance(), () -> {
                            XBlock.setType(b2, XMaterial.FARMLAND);
                            XSound.BLOCK_GRASS_BREAK.play(b2.getLocation(), 10, 15);
                        }, random.nextInt(30) + 1);
                    }
                }
            }
        }
        return false;
    }

    public UUID getUniqueId() {
        return this.uniqueId;
    }

    public List<ItemStack> getItems() {
        return Collections.unmodifiableList(this.items);
    }

    // Should be used in sync.
    public void addItem(ItemStack toAdd) {
        this.needsToBeSaved = true;
        synchronized (this.items) {
            for (ItemStack item : new ArrayList<>(this.items)) {
                if (item.getType() != toAdd.getType()
                        || item.getAmount() + toAdd.getAmount() > item.getMaxStackSize()) {
                    continue;
                }
                item.setAmount(item.getAmount() + toAdd.getAmount());
                if (this.opened != null) {
                    this.opened.updateInventory();
                }
                return;
            }
            this.items.add(toAdd);
            if (this.opened != null) {
                this.opened.updateInventory();
            }
        }
    }

    public void removeMaterial(Material material, int amount) {
        this.needsToBeSaved = true;
        synchronized (this.items) {
            for (ItemStack item : getItems().toArray(new ItemStack[0])) {
                if (material == item.getType()) {
                    item.setAmount(item.getAmount() - amount);

                    if (item.getAmount() <= 0) {
                        this.items.remove(item);
                    }
                    if (this.opened != null) {
                        this.opened.updateInventory();
                    }
                    return;
                }
            }
        }
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean willFit(ItemStack item) {
        synchronized (this.items) {
            if (this.items.size() < 27 * this.level.getPages()) {
                return true;
            }

            for (ItemStack stack : this.items) {
                if (stack.isSimilar(item) && stack.getAmount() < stack.getMaxStackSize()) {
                    return true;
                }
            }
        }

        return false;
    }

    public void setItems(List<ItemStack> items) {
        this.needsToBeSaved = true;
        synchronized (this.items) {
            this.items.clear();
            this.items.addAll(items);

            if (this.opened != null) {
                this.opened.updateInventory();
            }
        }
    }

    public UUID getViewing() {
        return this.viewing;
    }

    public void setViewing(UUID viewing) {
        this.viewing = viewing;
    }

    public void addCachedCrop(Block block) {
        this.cachedCrops.add(block);
    }

    public void removeCachedCrop(Block block) {
        this.cachedCrops.remove(block);
    }

    public List<Block> getCachedCrops() {
        return new ArrayList<>(this.cachedCrops);
    }

    public void clearCache() {
        this.cachedCrops.clear();
    }

    public long getLastCached() {
        return this.lastCached;
    }

    public void setLastCached(long lastCached) {
        this.lastCached = lastCached;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isInLoadedChunk() {
        return this.location != null && this.location.getWorld() != null && this.location.getWorld().isChunkLoaded(((int) this.location.getX()) >> 4, ((int) this.location.getZ()) >> 4);
    }

    public Location getLocation() {
        return this.location.clone();
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public UUID getPlacedBy() {
        return this.placedBy;
    }

    public Level getLevel() {
        return this.level;
    }

    public void close() {
        this.opened = null;
    }

    public Object getDataFromModuleCache(String key) {
        return this.moduleCache.getOrDefault(key, null);
    }

    public void addDataToModuleCache(String key, Object data) {
        this.moduleCache.put(key, data);
    }

    public boolean isDataCachedInModuleCache(String key) {
        return this.moduleCache.containsKey(key);
    }

    public void removeDataFromModuleCache(String key) {
        this.moduleCache.remove(key);
    }

    public void clearModuleCache() {
        this.moduleCache.clear();
    }

    public FarmType getFarmType() {
        return this.farmType;
    }

    public void toggleFarmType() {
        switch (this.farmType) {
            case CROPS:
                this.farmType = FarmType.LIVESTOCK;
                break;
            case LIVESTOCK:
                this.farmType = FarmType.BOTH;
                break;
            case BOTH:
                this.farmType = FarmType.CROPS;
                break;
        }
        EpicFarming.getInstance().getDataManager().updateFarm(this);
    }

    public void setFarmType(FarmType farmType) {
        this.farmType = farmType;
        EpicFarming.getInstance().getDataManager().updateFarm(this);
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean needsToBeSaved() {
        return this.needsToBeSaved;
    }

    public void setNeedsToBeSaved(boolean needsToBeSaved) {
        this.needsToBeSaved = needsToBeSaved;
    }
}
