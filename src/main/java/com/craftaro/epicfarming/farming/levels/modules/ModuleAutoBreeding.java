package com.craftaro.epicfarming.farming.levels.modules;

import com.craftaro.core.compatibility.CompatibleParticleHandler;
import com.craftaro.core.gui.GuiUtils;
import com.craftaro.core.hooks.EntityStackerManager;
import com.craftaro.third_party.com.cryptomorin.xseries.XMaterial;
import com.craftaro.epicfarming.EpicFarming;
import com.craftaro.epicfarming.farming.Farm;
import com.craftaro.epicfarming.farming.FarmType;
import com.craftaro.epicfarming.utils.EntityInfo;
import com.craftaro.epicfarming.utils.Methods;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ModuleAutoBreeding extends Module {
    private static final String BREED_COOLDOWN_METADATA = "breedCooldown";
    private static final int BREED_COOLDOWN_TICKS = 5 * 20 * 60;

    private final int autoBreedCap;

    public ModuleAutoBreeding(EpicFarming plugin, int autoBreedCap) {
        super(plugin);
        this.autoBreedCap = autoBreedCap;
    }

    @Override
    public String getName() {
        return "AutoBreeding";
    }

    @Override
    public int runEveryXTicks() {
        return 5;
    }

    @Override
    public void runFinal(Farm farm, Collection<LivingEntity> entitiesAroundFarm, List<Block> crops) {
        if (!isEnabled(farm) || farm.getFarmType() == FarmType.CROPS) {
            return;
        }

        List<LivingEntity> entities = new ArrayList<>(entitiesAroundFarm);
        entities.removeIf(e -> !(e instanceof Ageable) || !((Ageable) e).isAdult() || e.hasMetadata(BREED_COOLDOWN_METADATA) || e.isDead());
        int actualAmount = entities.stream().filter(e -> e instanceof Ageable && ((Ageable) e).isAdult() && !e.hasMetadata(BREED_COOLDOWN_METADATA) && !e.isDead()).map(EntityStackerManager::getSize).reduce(Integer::sum).orElse(0);

        if (actualAmount < this.autoBreedCap) {
            return;
        }

        Collections.shuffle(entities);

        Map<EntityType, Long> counts = entities.stream()
                .collect(Collectors.groupingBy(Entity::getType, Collectors.summingLong(entity -> {
                    int stackSize = EntityStackerManager.getSize(entity);
                    return stackSize > 0 ? stackSize : 1;
                })));

        boolean mate1 = false;
        for (Map.Entry<EntityType, Long> entry : counts.entrySet()) {
            if (entry.getValue() < 2) {
                continue;
            }

            EntityType entityType = entry.getKey();
            ItemStack breedingItem = getBreedingItem(farm, entityType);
            if (breedingItem == null || breedingItem.getAmount() < 2) {
                continue;
            }

            for (LivingEntity entity : entities) {
                if (entity.getType() != entityType) {
                    continue;
                }

                int stackSize = EntityStackerManager.getSize(entity);
                if (stackSize == 0) {
                    stackSize = 1;
                }

                if (mate1) {
                    farm.removeMaterial(breedingItem.getType(), 2);

                    if (stackSize > 1) {
                        handleStackedBreed(entity);
                        if (stackSize > 2) {
                            handleStackedBreed(entity);
                        }
                    } else {
                        handleBreedNatural(entity);
                        spawnParticlesAndAnimation(entity.getLocation(), farm.getLocation());
                    }

                    spawnParticlesAndAnimation(entity.getLocation(), farm.getLocation());
                    return;
                }

                if (stackSize > 1) {
                    handleStackedBreed(entity);
                    spawnParticlesAndAnimation(entity.getLocation(), farm.getLocation());
                } else {
                    handleBreedNatural(entity);
                    spawnParticlesAndAnimation(entity.getLocation(), farm.getLocation());
                }
                mate1 = true;
                break;
            }
        }
    }

    @Override
    public ItemStack getGUIButton(Farm farm) {
        return GuiUtils.createButtonItem(XMaterial.EGG, this.plugin.getLocale().getMessage("interface.button.autobreeding")
                        .processPlaceholder("status", isEnabled(farm)
                                ? this.plugin.getLocale().getMessage("general.interface.on").getMessage()
                                : this.plugin.getLocale().getMessage("general.interface.off").getMessage())
                        .getMessage(),
                this.plugin.getLocale().getMessage("interface.button.functiontoggle").getMessage());
    }

    @Override
    public void runButtonPress(Player player, Farm farm, ClickType type) {
        toggleEnabled(farm);
    }

    @Override
    public String getDescription() {
        return this.plugin.getLocale()
                .getMessage("interface.button.autobreeding")
                .processPlaceholder("status", this.autoBreedCap)
                .getMessage();
    }

    private void handleStackedBreed(LivingEntity entity) {
        Bukkit.getScheduler().runTask(this.plugin, () -> {
            LivingEntity spawned = (LivingEntity) entity.getWorld().spawnEntity(entity.getLocation(), entity.getType());
            Ageable ageable = (Ageable) spawned;
            ageable.setBaby();
            handleBreed(entity);
        });
    }
    private void handleBreedNatural(Entity entity) {
        Bukkit.getScheduler().runTask(this.plugin, () -> {
            LivingEntity spawned = (LivingEntity) entity.getWorld().spawnEntity(entity.getLocation(), entity.getType());
            Ageable ageable = (Ageable) spawned;
            ageable.setBaby();
            handleBreed(entity);
        });
    }
    private void handleBreed(Entity entity) {
        entity.setMetadata(BREED_COOLDOWN_METADATA, new FixedMetadataValue(this.plugin, true));
        Bukkit.getScheduler().scheduleSyncDelayedTask(this.plugin, () ->
                entity.removeMetadata(BREED_COOLDOWN_METADATA, this.plugin), BREED_COOLDOWN_TICKS);
    }

    private boolean isEnabled(Farm farm) {
        Object obj = getData(farm, "enabled");
        return obj == null || (boolean) obj;
    }

    private void toggleEnabled(Farm farm) {
        saveData(farm, "enabled", !isEnabled(farm));
    }

    private ItemStack getBreedingItem(Farm farm, EntityType entityType) {
        EntityInfo info = EntityInfo.of(entityType);
        if (info == null) {
            return null;
        }

        for (ItemStack item : farm.getItems().toArray(new ItemStack[0])) {
            if (item.getType() == info.getMaterial()) {
                return item;
            }
        }
        return null;
    }

    private void spawnParticlesAndAnimation(Location entityLocation, Location farmLocation) {
        CompatibleParticleHandler.spawnParticles(CompatibleParticleHandler.ParticleType.HEART, entityLocation, 5, .5, .5, .5);
        Bukkit.getScheduler().runTask(this.plugin, () -> Methods.animate(farmLocation, XMaterial.EGG));
    }
}