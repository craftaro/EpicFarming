package com.songoda.epicfarming.tasks;

import com.songoda.core.hooks.EntityStackerManager;
import com.songoda.epicfarming.EpicFarming;
import com.songoda.epicfarming.boost.BoostData;
import com.songoda.epicfarming.farming.Farm;
import com.songoda.epicfarming.settings.Settings;
import com.songoda.epicfarming.utils.EntityInfo;
import com.songoda.epicfarming.utils.Methods;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Wool;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.stream.Collectors;

public class EntityTask extends BukkitRunnable {

    private static final Random random = new Random();
    private static Map<Entity, Integer> lastTicksLived = new HashMap<>();
    private static final Map<Entity, Integer> ticksLived = new HashMap<>();
    private static EntityTask instance;
    private static EpicFarming plugin;
    private final int autoBreedCap = Settings.AUTO_BREEDING_CAP.getInt();
    
    public static EntityTask startTask(EpicFarming pl) {
        if (instance != null && !instance.isCancelled()) {
            instance.cancel();
        }
        instance = new EntityTask();
        instance.runTaskTimer(plugin = pl, 0, Settings.ENTITY_TICK_SPEED.getInt());
        return instance;
    }

    @Override
    public void run() {
        for (Farm farm : plugin.getFarmManager().getFarms().values()) {
            if (farm.getLocation() == null) continue;

            Location location = farm.getLocation();
            location.add(.5, .5, .5);

            double radius = farm.getLevel().getRadius() + .5;
            Collection<LivingEntity> amt = location.getWorld().getNearbyEntities(location, radius, radius, radius)
                    .stream().filter(e -> !(e instanceof Player) && e instanceof LivingEntity && !(e instanceof ArmorStand))
                    .map(entity -> (LivingEntity) entity).collect(Collectors.toCollection(ArrayList::new));

            if (farm.getLevel().isAutoBreeding()) doAutoBreeding(farm, amt);

            for (Entity entity : amt) {
                if (!ticksLived.containsKey(entity)) ticksLived.put(entity, 0);

                int lived = ticksLived.get(entity);

                ticksLived.put(entity, lived + 100);

                int min = (int) Math.floor(getMin(entity) / farm.getLevel().getSpeedMultiplier());
                int max = (int) Math.floor(getMax(entity) / farm.getLevel().getSpeedMultiplier());

                int rand = random.nextInt((int) Math.floor(100 / farm.getLevel().getSpeedMultiplier()));

                if (lived < min) continue;

                if (rand != 5 && lived < max) continue;

                if (entity instanceof Chicken) {
                    if (!((Ageable) entity).isAdult()) continue;
                    entity.getLocation().getWorld().playSound(entity.getLocation(), Sound.ENTITY_CHICKEN_EGG, 1, 2);
                    if (!farm.getLevel().isAutoHarvest()) {
                        ticksLived.remove(entity);
                        entity.getLocation().getWorld().dropItemNaturally(entity.getLocation(), new ItemStack(Material.EGG));
                    } else {
                        doDrop(farm, new ItemStack(Material.EGG, 1));
                    }
                    Methods.animate(farm.getLocation(), Material.EGG);
                } else if (entity instanceof Sheep) {
                    if (!((Ageable) entity).isAdult()) continue;
                    ((Sheep) entity).setSheared(true);

                    Wool woolColor = new Wool(((Sheep) entity).getColor());
                    ItemStack wool = woolColor.toItemStack((int) Math.round(1 + (Math.random() * 3)));
                    if (!farm.getLevel().isAutoHarvest()) {
                        entity.getLocation().getWorld().dropItemNaturally(entity.getLocation(), wool);
                    } else {
                        doDrop(farm, wool);
                    }
                    Methods.animate(farm.getLocation(), wool.getType());
                }
                ticksLived.put(entity, 0);
            }
        }

        for (Map.Entry<Entity, Integer> entry : lastTicksLived.entrySet()) {
            int last = entry.getValue();
            if (!ticksLived.containsKey(entry.getKey())) continue;
            int current = ticksLived.get(entry.getKey());

            if (last == current) {
                ticksLived.remove(entry.getKey());
            }
        }
        lastTicksLived = new HashMap<>(ticksLived);

    }

    private int getMin(Entity entity) {
        switch (entity.getType()) {
            case SHEEP:
                return 0;
            case CHICKEN:
                return 6000;
            default:
                return 0;
        }
    }

    private int getMax(Entity entity) {
        switch (entity.getType()) {
            case SHEEP:
                return 6000;
            case CHICKEN:
                return 12000;
            default:
                return 0;
        }
    }

    private void doAutoBreeding(Farm farm, Collection<LivingEntity> entities1) {
        List<LivingEntity> entities = new ArrayList<>(entities1);
        Collections.shuffle(entities);
        entities.removeIf(e -> !(e instanceof Ageable) || !((Ageable) e).isAdult());

        Map<EntityType, Long> counts =
                entities.stream().collect(Collectors.groupingBy(Entity::getType, Collectors.counting()));

        for (LivingEntity entity : entities) {
            counts.put(entity.getType(),
                    counts.get(entity.getType()) - 1 + EntityStackerManager.getSize(entity));
        }

        boolean mate1 = false;

        for (Map.Entry<EntityType, Long> entry : counts.entrySet()) {
            for (LivingEntity entity : entities) {
                if (entry.getKey() != entity.getType()) continue;
                if (mate1) {
                    int count = EntityStackerManager.getSize(entity);
                    if (count > 1)
                        handleStackedBreed(entity);
                    else
                        handleBreed(entity);
                    return;
                }

                if (entry.getValue() >= 2 && entry.getValue() < autoBreedCap) {

                    EntityType entityType = entry.getKey();

                    for (ItemStack item : farm.getItems()) {

                        try {
                            if (item.getType() != EntityInfo.valueOf(entityType.name()).getMaterial() || item.getAmount() < 2)
                                continue;
                        } catch (IllegalArgumentException e) {
                            continue;
                        }

                        farm.removeMaterial(item.getType(), 2);

                        Location location = entity.getLocation();
                        Entity newSpawn = location.getWorld().spawnEntity(location, entityType);
                        ((Ageable) newSpawn).setBaby();

                        int count = EntityStackerManager.getSize(entity);
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


    private boolean doDrop(Farm farm, ItemStack stack) {
        BoostData boostData = plugin.getBoostManager().getBoost(farm.getPlacedBy());

        stack.setAmount(stack.getAmount() * (boostData == null ? 1 : boostData.getMultiplier()));

        if (!farm.willFit(stack)) return false;
        farm.addItem(stack);
        return true;
    }

    public Map<Entity, Integer> getTicksLived() {
        return Collections.unmodifiableMap(ticksLived);
    }
}