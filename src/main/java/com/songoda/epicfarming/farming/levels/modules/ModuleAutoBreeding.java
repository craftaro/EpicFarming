package com.songoda.epicfarming.farming.levels.modules;

import com.craftaro.core.compatibility.CompatibleParticleHandler;
import com.craftaro.core.gui.GuiUtils;
import com.craftaro.core.hooks.EntityStackerManager;
import com.craftaro.core.third_party.com.cryptomorin.xseries.XMaterial;
import com.songoda.epicfarming.EpicFarming;
import com.songoda.epicfarming.farming.Farm;
import com.songoda.epicfarming.farming.FarmType;
import com.songoda.epicfarming.utils.EntityInfo;
import com.songoda.epicfarming.utils.Methods;
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
        Collections.shuffle(entities);
        if (entities.size() >= this.autoBreedCap) {
            return;
        }

        entities.removeIf(e -> !(e instanceof Ageable) || !((Ageable) e).isAdult() || e.hasMetadata("breedCooldown") || e.isDead());

        Map<EntityType, Long> counts =
                entities.stream().collect(Collectors.groupingBy(Entity::getType, Collectors.counting()));

        for (LivingEntity entity : entities) {
            int stackSize = EntityStackerManager.getSize(entity);
            if (stackSize == 0) {
                stackSize = 1;
            }
            counts.put(entity.getType(), counts.get(entity.getType()) - 1 + stackSize);
        }

        boolean mate1 = false;
        for (Map.Entry<EntityType, Long> entry : counts.entrySet()) {
            for (LivingEntity entity : entities) {
                if (entry.getKey() != entity.getType()) {
                    continue;
                }
                int count = EntityStackerManager.getSize(entity) == 0 ? 1 : EntityStackerManager.getSize(entity);
                if (mate1) {
                    if (count > 1) {
                        handleStackedBreed(entity);
                    } else {
                        handleBreed(entity);
                    }
                    Bukkit.getScheduler().runTask(this.plugin, () -> Methods.animate(farm.getLocation(), XMaterial.EGG));
                    return;
                }

                if (entry.getValue() >= 2) {
                    EntityType entityType = entry.getKey();

                    for (ItemStack item : farm.getItems().toArray(new ItemStack[0])) {
                        EntityInfo info = EntityInfo.of(entityType);
                        try {
                            if (info == null || item.getType() != info.getMaterial() || item.getAmount() < 2) {
                                continue;
                            }
                        } catch (IllegalArgumentException e) {
                            continue;
                        }

                        farm.removeMaterial(item.getType(), 2);

                        Location location = entity.getLocation();
                        CompatibleParticleHandler.spawnParticles(CompatibleParticleHandler.ParticleType.HEART,
                                location, 5, .5, .5, .5);
                        Bukkit.getScheduler().runTask(this.plugin, () -> {
                            Entity newSpawn = location.getWorld().spawnEntity(location, entityType);
                            ((Ageable) newSpawn).setBaby();
                        });

                        if (count > 1) {
                            handleStackedBreed(entity);
                            if (count - 1 > 1) {
                                handleStackedBreed(entity);
                            } else {
                                handleBreed(entity);
                            }
                            return;
                        }
                        handleBreed(entity);
                        mate1 = true;
                        break;
                    }
                }
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
        EntityStackerManager.removeOne(entity);
        Bukkit.getScheduler().runTask(this.plugin, () -> {
            LivingEntity spawned = (LivingEntity) entity.getWorld().spawnEntity(entity.getLocation(), entity.getType());
            handleBreed(spawned);
        });
    }

    private void handleBreed(Entity entity) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(this.plugin, () ->
                entity.removeMetadata("breedCooldown", this.plugin), 5 * 20 * 60);
        entity.setMetadata("breedCooldown", new FixedMetadataValue(this.plugin, true));
    }

    private boolean isEnabled(Farm farm) {
        Object obj = getData(farm, "enabled");
        return obj == null || (boolean) obj;
    }

    private void toggleEnabled(Farm farm) {
        saveData(farm, "enabled", !isEnabled(farm));
    }
}
