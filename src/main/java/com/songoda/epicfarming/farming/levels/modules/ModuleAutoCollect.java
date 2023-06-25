package com.songoda.epicfarming.farming.levels.modules;

import com.craftaro.core.compatibility.CompatibleMaterial;
import com.craftaro.core.gui.GuiUtils;
import com.craftaro.core.third_party.com.cryptomorin.xseries.XBlock;
import com.craftaro.core.third_party.com.cryptomorin.xseries.XMaterial;
import com.craftaro.core.utils.BlockUtils;
import com.songoda.epicfarming.EpicFarming;
import com.songoda.epicfarming.boost.BoostData;
import com.songoda.epicfarming.farming.Farm;
import com.songoda.epicfarming.farming.FarmType;
import com.songoda.epicfarming.utils.Methods;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Wool;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

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
        if (farm.getFarmType() != FarmType.LIVESTOCK) {
            collectCrops(farm, crops);
        }

        if (farm.getFarmType() != FarmType.CROPS) {
            collectLivestock(farm, entitiesAroundFarm);
        }
    }

    private void collectCrops(Farm farm, List<Block> crops) {
        for (Block block : crops) {
            if (BlockUtils.isCropFullyGrown(block) && isEnabled(farm) && doCropDrop(farm, CompatibleMaterial.getMaterial(block.getType()).get())) {
                if (farm.getLevel().isAutoReplant()) {
                    Bukkit.getScheduler().runTask(this.plugin, () ->
                            BlockUtils.resetGrowthStage(block));
                    continue;
                }
                Bukkit.getScheduler().runTask(this.plugin, () -> block.setType(Material.AIR));
            }
        }
    }

    private void collectLivestock(Farm farm, Collection<LivingEntity> entitiesAroundFarm) {
        for (Entity entity : new ArrayList<>(entitiesAroundFarm)) {
            if (!ticksLived.containsKey(entity)) {
                ticksLived.put(entity, 0);
            }

            int lived = ticksLived.get(entity);

            ticksLived.put(entity, lived + 20);

            int min = (int) Math.floor(getMin(entity) / farm.getLevel().getSpeedMultiplier());
            int max = (int) Math.floor(getMax(entity) / farm.getLevel().getSpeedMultiplier());

            int rand = random.nextInt((int) Math.floor(100 / farm.getLevel().getSpeedMultiplier()));

            if (lived < min) {
                continue;
            }

            if (rand != 5 && lived < max) {
                continue;
            }

            if (entity instanceof Chicken) {
                if (!((Ageable) entity).isAdult()) {
                    continue;
                }
                entity.getLocation().getWorld().playSound(entity.getLocation(), Sound.ENTITY_CHICKEN_EGG, 1, 2);
                if (!isEnabled(farm)) {
                    ticksLived.remove(entity);
                    Bukkit.getScheduler().runTask(this.plugin, () ->
                            entity.getLocation().getWorld().dropItemNaturally(entity.getLocation(), new ItemStack(Material.EGG)));
                } else {
                    doLivestockDrop(farm, new ItemStack(Material.EGG, 1));
                }
                Bukkit.getScheduler().runTask(this.plugin, () ->
                        Methods.animate(farm.getLocation(), XMaterial.EGG));
            } else if (entity instanceof Sheep) {
                if (!((Ageable) entity).isAdult()) {
                    continue;
                }
                ((Sheep) entity).setSheared(true);

                Wool woolColor = new Wool(((Sheep) entity).getColor());
                ItemStack wool = woolColor.toItemStack((int) Math.round(1 + (Math.random() * 3)));
                if (!isEnabled(farm)) {
                    Bukkit.getScheduler().runTask(this.plugin, () ->
                            entity.getLocation().getWorld().dropItemNaturally(entity.getLocation(), wool));
                } else {
                    doLivestockDrop(farm, wool);
                }
                Bukkit.getScheduler().runTask(this.plugin, () ->
                        Methods.animate(farm.getLocation(), wool));
            }
            ticksLived.put(entity, 0);
        }

        for (Map.Entry<Entity, Integer> entry : lastTicksLived.entrySet()) {
            int last = entry.getValue();
            if (!ticksLived.containsKey(entry.getKey())) {
                continue;
            }
            int current = ticksLived.get(entry.getKey());

            if (last == current) {
                ticksLived.remove(entry.getKey());
            }
        }
        lastTicksLived = new HashMap<>(ticksLived);
    }

    @Override
    public ItemStack getGUIButton(Farm farm) {
        return GuiUtils.createButtonItem(XMaterial.BUCKET, this.plugin.getLocale().getMessage("interface.button.autocollect")
                        .processPlaceholder("status", isEnabled(farm)
                                ? this.plugin.getLocale().getMessage("general.interface.on").getMessage()
                                : this.plugin.getLocale().getMessage("general.interface.off").getMessage()).getMessage(),
                this.plugin.getLocale().getMessage("interface.button.collectiontype").processPlaceholder("status", getCollectionType(farm).translate()).getMessage(),
                this.plugin.getLocale().getMessage("interface.button.functiontoggle").getMessage());
    }

    @Override
    public void runButtonPress(Player player, Farm farm, ClickType type) {
        if (type == ClickType.LEFT) {
            toggleEnabled(farm);
        } else if (type == ClickType.RIGHT) {
            toggleCollectionType(farm);
        }
    }

    @Override
    public String getDescription() {
        return this.plugin.getLocale()
                .getMessage("interface.button.autocollect")
                .processPlaceholder("status",
                        this.plugin.getLocale().getMessage("general.interface.unlocked").getMessage())
                .getMessage();
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
        for (ItemStack item : farm.getItems().toArray(new ItemStack[0])) {
            if (item == null || item.getType() != XMaterial.BONE_MEAL.parseMaterial()) {
                continue;
            }
            farm.removeMaterial(XMaterial.BONE_MEAL.parseMaterial(), 1);
            return true;
        }

        return false;
    }

    private int getMin(Entity entity) {
        switch (entity.getType()) {
            case CHICKEN:
                return 6000;
            case SHEEP:
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

    private boolean doCropDrop(Farm farm, XMaterial material) {
        if (material == null || farm == null || !XBlock.isCrop(material) || !this.plugin.isEnabled()) {
            return false;
        }

        BoostData boostData = this.plugin.getBoostManager().getBoost(farm.getPlacedBy());

        XMaterial yield = CompatibleMaterial.getYieldForCrop(material);

        ItemStack stack = yield.parseItem();
        stack.setAmount((useBoneMeal(farm) ? random.nextInt(2) + 2 : 1) * (boostData == null ? 1 : boostData.getMultiplier()));
        ItemStack seedStack = CompatibleMaterial.getSeedForCrop(material).parseItem();
        seedStack.setAmount(random.nextInt(3) + 1 + (useBoneMeal(farm) ? 1 : 0));

        if (!farm.willFit(stack) || !farm.willFit(seedStack)) {
            return false;
        }
        Bukkit.getScheduler().runTask(this.plugin, () -> {
            Methods.animate(farm.getLocation(), yield);
            farm.addItem(stack);
        });

        if (getCollectionType(farm) != CollectionType.NO_SEEDS) {
            farm.addItem(seedStack);
        }

        return true;
    }

    private boolean doLivestockDrop(Farm farm, ItemStack stack) {
        BoostData boostData = this.plugin.getBoostManager().getBoost(farm.getPlacedBy());

        stack.setAmount(stack.getAmount() * (boostData == null ? 1 : boostData.getMultiplier()));

        if (!farm.willFit(stack)) {
            return false;
        }
        farm.addItem(stack);
        return true;
    }

    public static Map<Entity, Integer> getTicksLived() {
        return Collections.unmodifiableMap(ticksLived);
    }

    public enum CollectionType {
        ALL, NO_SEEDS;

        public String translate() {
            return EpicFarming.getPlugin(EpicFarming.class)
                    .getLocale()
                    .getMessage("general.interface." + name().replace("_", "").toLowerCase())
                    .getMessage();
        }
    }
}
