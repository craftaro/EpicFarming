package com.songoda.epicfarming.farming.levels.modules;

import com.songoda.core.compatibility.CompatibleMaterial;
import com.songoda.core.gui.GuiUtils;
import com.songoda.core.utils.BlockUtils;
import com.songoda.epicfarming.EpicFarming;
import com.songoda.epicfarming.boost.BoostData;
import com.songoda.epicfarming.farming.Crop;
import com.songoda.epicfarming.farming.Farm;
import com.songoda.epicfarming.farming.FarmType;
import com.songoda.epicfarming.utils.CropType;
import com.songoda.epicfarming.utils.Methods;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Wool;

import java.util.*;

public class ModuleAutoCollect extends Module {

    private static Map<Entity, Integer> lastTicksLived = new HashMap<>();
    private static final Map<Entity, Integer> ticksLived = new HashMap<>();
    private static final Random random = new Random();

    public ModuleAutoCollect(EpicFarming plugin) {
        super(plugin);
    }

    @Override
    public String getName() {
        return "AutoCollect";
    }

    @Override
    public int runEveryXTicks() {
        return 1;
    }

    @Override
    public void runFinal(Farm farm, Collection<LivingEntity> entitiesAroundFarm, List<Block> crops) {
        if (farm.getFarmType() != FarmType.LIVESTOCK)
            collectCrops(farm, crops);

        if (farm.getFarmType() != FarmType.CROPS)
            collectLivestock(farm, entitiesAroundFarm);
    }

    private void collectCrops(Farm farm, List<Block> crops) {
        for (Block block : crops) {

            if (BlockUtils.isCropFullyGrown(block) && isEnabled(farm) && doCropDrop(farm, CompatibleMaterial.getMaterial(block).getBlockMaterial())) {

                if (farm.getLevel().isAutoReplant()) {
                    BlockUtils.resetGrowthStage(block);
                    continue;
                }
                block.setType(Material.AIR);
            }
        }
    }

    private void collectLivestock(Farm farm, Collection<LivingEntity> entitiesAroundFarm) {
        for (Entity entity : entitiesAroundFarm) {
            if (!ticksLived.containsKey(entity)) ticksLived.put(entity, 0);

            int lived = ticksLived.get(entity);

            ticksLived.put(entity, lived + 20);

            int min = (int) Math.floor(getMin(entity) / farm.getLevel().getSpeedMultiplier());
            int max = (int) Math.floor(getMax(entity) / farm.getLevel().getSpeedMultiplier());

            int rand = random.nextInt((int) Math.floor(100 / farm.getLevel().getSpeedMultiplier()));

            if (lived < min) continue;

            if (rand != 5 && lived < max) continue;

            if (entity instanceof Chicken) {
                if (!((Ageable) entity).isAdult()) continue;
                entity.getLocation().getWorld().playSound(entity.getLocation(), Sound.ENTITY_CHICKEN_EGG, 1, 2);
                if (!isEnabled(farm)) {
                    ticksLived.remove(entity);
                    entity.getLocation().getWorld().dropItemNaturally(entity.getLocation(), new ItemStack(Material.EGG));
                } else {
                    doLivestockDrop(farm, new ItemStack(Material.EGG, 1));
                }
                Methods.animate(farm.getLocation(), Material.EGG);
            } else if (entity instanceof Sheep) {
                if (!((Ageable) entity).isAdult()) continue;
                ((Sheep) entity).setSheared(true);

                Wool woolColor = new Wool(((Sheep) entity).getColor());
                ItemStack wool = woolColor.toItemStack((int) Math.round(1 + (Math.random() * 3)));
                if (!isEnabled(farm)) {
                    entity.getLocation().getWorld().dropItemNaturally(entity.getLocation(), wool);
                } else {
                    doLivestockDrop(farm, wool);
                }
                Methods.animate(farm.getLocation(), wool.getType());
            }
            ticksLived.put(entity, 0);
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

    @Override
    public ItemStack getGUIButton(Farm farm) {
        return GuiUtils.createButtonItem(CompatibleMaterial.BUCKET, plugin.getLocale().getMessage("interface.button.autocollect")
                        .processPlaceholder("status", isEnabled(farm)
                                ? plugin.getLocale().getMessage("general.interface.on").getMessage()
                                : plugin.getLocale().getMessage("general.interface.off").getMessage()).getMessage(),
                plugin.getLocale().getMessage("interface.button.collectiontype").processPlaceholder("status", getCollectionType(farm).translate()).getMessage(),
                plugin.getLocale().getMessage("interface.button.functiontoggle").getMessage());
    }

    @Override
    public void runButtonPress(Player player, Farm farm, ClickType type) {
        if (type == ClickType.LEFT)
            toggleEnabled(farm);
        else if (type == ClickType.RIGHT)
            toggleCollectionType(farm);
    }

    @Override
    public String getDescription() {
        return plugin.getLocale().getMessage("interface.button.autocollect")
                .processPlaceholder("status",
                        plugin.getLocale().getMessage("general.interface.unlocked")
                                .getMessage()).getMessage();
    }

    public boolean isEnabled(Farm farm) {
        Object obj = getData(farm, "enabled");
        return obj == null || (boolean) obj;
    }

    private void toggleEnabled(Farm farm) {
        saveData(farm, "enabled", !isEnabled(farm));
    }

    public CollectionType getCollectionType(Farm farm) {
        Object obj = getData(farm, "collectiontype");
        return obj == null ? CollectionType.ALL : CollectionType.valueOf((String) obj);
    }

    private void toggleCollectionType(Farm farm) {
        saveData(farm, "collectiontype", getCollectionType(farm) == CollectionType.ALL
                ? CollectionType.NO_SEEDS.toString() : CollectionType.ALL.toString());
    }

    private boolean useBoneMeal(Farm farm) {
        for (ItemStack item : farm.getItems()) {
            if (item.getType() != CompatibleMaterial.BONE_MEAL.getMaterial()) continue;
            farm.removeMaterial(CompatibleMaterial.BONE_MEAL.getMaterial(), 1);
            return true;
        }
        return false;
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

    private boolean doCropDrop(Farm farm, Material material) {
        CropType cropTypeData = CropType.getCropType(material);

        if (material == null || farm == null || cropTypeData == null) return false;

        BoostData boostData = plugin.getBoostManager().getBoost(farm.getPlacedBy());

        ItemStack stack = new ItemStack(cropTypeData.getYieldMaterial(), (useBoneMeal(farm) ? random.nextInt(2) + 2 : 1) * (boostData == null ? 1 : boostData.getMultiplier()));
        ItemStack seedStack = new ItemStack(cropTypeData.getSeedMaterial(), random.nextInt(3) + 1 + (useBoneMeal(farm) ? 1 : 0));

        if (!farm.willFit(stack) || !farm.willFit(seedStack)) return false;
        Methods.animate(farm.getLocation(), cropTypeData.getYieldMaterial());
        farm.addItem(stack);
        if (getCollectionType(farm) != CollectionType.NO_SEEDS)
            farm.addItem(seedStack);
        return true;
    }

    private boolean doLivestockDrop(Farm farm, ItemStack stack) {
        BoostData boostData = plugin.getBoostManager().getBoost(farm.getPlacedBy());

        stack.setAmount(stack.getAmount() * (boostData == null ? 1 : boostData.getMultiplier()));

        if (!farm.willFit(stack)) return false;
        farm.addItem(stack);
        return true;
    }

    public static Map<Entity, Integer> getTicksLived() {
        return Collections.unmodifiableMap(ticksLived);
    }

    public enum CollectionType {
        ALL, NO_SEEDS;

        public String translate() {
            return EpicFarming.getInstance().getLocale().getMessage("general.interface." + name().replace("_", "").toLowerCase()).getMessage();
        }
    }

}
