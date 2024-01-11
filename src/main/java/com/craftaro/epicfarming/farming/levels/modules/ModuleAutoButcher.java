package com.craftaro.epicfarming.farming.levels.modules;

import com.craftaro.core.gui.GuiUtils;
import com.craftaro.core.hooks.EntityStackerManager;
import com.craftaro.third_party.com.cryptomorin.xseries.XMaterial;
import com.craftaro.third_party.com.cryptomorin.xseries.XSound;
import com.craftaro.epicfarming.EpicFarming;
import com.craftaro.epicfarming.farming.Farm;
import com.craftaro.epicfarming.farming.FarmType;
import com.craftaro.epicfarming.utils.Methods;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class ModuleAutoButcher extends Module {
    private final int autoButcherDelay;

    public ModuleAutoButcher(EpicFarming plugin, int autoButcherDelay) {
        super(plugin);
        this.autoButcherDelay = autoButcherDelay;
    }

    @Override
    public String getName() {
        return "AutoButcher";
    }

    @Override
    public int runEveryXTicks() {
        return this.autoButcherDelay;
    }

    @Override
    public void runFinal(Farm farm, Collection<LivingEntity> entitiesAroundFarm, List<Block> crops) {
        if (!isEnabled(farm) || farm.getFarmType() == FarmType.CROPS) {
            return;
        }

        List<LivingEntity> entities = new ArrayList<>(entitiesAroundFarm);
        Collections.shuffle(entities);
        entities.removeIf(e -> e instanceof Ageable && !((Ageable) e).isAdult() || e.isDead());

        int count = 0;
        for (LivingEntity entity : entities) {
            int stackSize = EntityStackerManager.getSize(entity);
            if (stackSize == 0) {
                stackSize = 1;
            }
            count += stackSize;
        }

        if (count <= 2 || !farm.willFit(XMaterial.STONE.parseItem())) {
            return;
        }

        for (LivingEntity entity : entities) {
            entity.setMetadata("EFA-TAGGED", new FixedMetadataValue(this.plugin, farm.getLocation()));
            XSound.ENTITY_PLAYER_ATTACK_SWEEP.play(entity);
            Bukkit.getScheduler().runTask(this.plugin, () -> {
                entity.damage(99999999, entity);
                Methods.animate(farm.getLocation(), XMaterial.IRON_SWORD);
            });
            return;
        }
    }

    @Override
    public ItemStack getGUIButton(Farm farm) {
        return GuiUtils.createButtonItem(XMaterial.STONE_SWORD, this.plugin.getLocale().getMessage("interface.button.autobutcher")
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
        return this.plugin.getLocale().getMessage("interface.button.autobutcher")
                .processPlaceholder("status", this.autoButcherDelay).getMessage();
    }

    private boolean isEnabled(Farm farm) {
        Object obj = getData(farm, "enabled");
        return obj == null || (boolean) obj;
    }

    private void toggleEnabled(Farm farm) {
        saveData(farm, "enabled", !isEnabled(farm));
    }
}
