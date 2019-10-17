package com.songoda.epicfarming.farming.levels.modules;

import com.songoda.core.compatibility.CompatibleMaterial;
import com.songoda.core.compatibility.CompatibleParticleHandler;
import com.songoda.core.gui.GuiUtils;
import com.songoda.core.hooks.EntityStackerManager;
import com.songoda.epicfarming.EpicFarming;
import com.songoda.epicfarming.farming.Farm;
import com.songoda.epicfarming.farming.FarmType;
import com.songoda.epicfarming.utils.EntityInfo;
import com.songoda.epicfarming.utils.Methods;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.*;
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
    public void runFinal(Farm farm, Collection<LivingEntity> entitiesAroundFarm) {
        if (!isEnabled(farm) || farm.getFarmType() == FarmType.CROPS) return;

        List<LivingEntity> entities = new ArrayList<>(entitiesAroundFarm);
        Collections.shuffle(entities);
        if (entities.size() >= autoBreedCap)
            return;

        entities.removeIf(e -> !(e instanceof Ageable) || !((Ageable) e).isAdult() || e.hasMetadata("breedCooldown") || e.isDead());

        Map<EntityType, Long> counts =
                entities.stream().collect(Collectors.groupingBy(Entity::getType, Collectors.counting()));

        for (LivingEntity entity : entities) {
            int stackSize = EntityStackerManager.getSize(entity);
            if (stackSize == 0) stackSize = 1;
            counts.put(entity.getType(),
                    counts.get(entity.getType()) - 1 + stackSize);
        }


        for (Map.Entry<EntityType, Long> entry : counts.entrySet()) {
            boolean mate1 = false;

            for (LivingEntity entity : entities) {
                if (entry.getKey() != entity.getType()) continue;
                int count = EntityStackerManager.getSize(entity) == 0 ? 1 : EntityStackerManager.getSize(entity);
                if (mate1) {
                    if (count > 1)
                        handleStackedBreed(entity);
                    else
                        handleBreed(entity);
                    Methods.animate(farm.getLocation(), Material.EGG);
                    return;
                }

                if (entry.getValue() >= 2) {
                    EntityType entityType = entry.getKey();

                    for (ItemStack item : new ArrayList<>(farm.getItems())) {

                        try {
                            if (item.getType() != EntityInfo.valueOf(entityType.name()).getMaterial() || item.getAmount() < 2)
                                continue;
                        } catch (IllegalArgumentException e) {
                            continue;
                        }

                        farm.removeMaterial(item.getType(), 2);

                        Location location = entity.getLocation();
                        CompatibleParticleHandler.spawnParticles(CompatibleParticleHandler.ParticleType.HEART,
                                location, 5, .5, .5, .5);
                        Entity newSpawn = location.getWorld().spawnEntity(location, entityType);
                        ((Ageable) newSpawn).setBaby();

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
                    }
                }
            }
        }
    }

    @Override
    public ItemStack getGUIButton(Farm farm) {
        return GuiUtils.createButtonItem(CompatibleMaterial.EGG, plugin.getLocale().getMessage("interface.button.autobreeding")
                        .processPlaceholder("status", isEnabled(farm)
                                ? plugin.getLocale().getMessage("general.interface.on").getMessage()
                                : plugin.getLocale().getMessage("general.interface.off").getMessage()).getMessage(),
                plugin.getLocale().getMessage("interface.button.functiontoggle").getMessage());
    }

    @Override
    public void runButtonPress(Player player, Farm farm, ClickType type) {
        toggleEnabled(farm);
    }

    @Override
    public String getDescription() {
        return plugin.getLocale().getMessage("interface.button.autobreeding")
                .processPlaceholder("status", autoBreedCap).getMessage();
    }

    private void handleStackedBreed(LivingEntity entity) {
        EntityStackerManager.removeOne(entity);
        LivingEntity spawned = (LivingEntity) entity.getWorld().spawnEntity(entity.getLocation(), entity.getType());
        handleBreed(spawned);
    }

    private void handleBreed(Entity entity) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () ->
                entity.removeMetadata("breedCooldown", plugin), 5 * 20 * 60);
        entity.setMetadata("breedCooldown", new FixedMetadataValue(plugin, true));
    }

    private boolean isEnabled(Farm farm) {
        Object obj = getData(farm, "enabled");
        return obj == null || (boolean) obj;
    }

    private void toggleEnabled(Farm farm) {
        saveData(farm, "enabled", !isEnabled(farm));
    }
}
