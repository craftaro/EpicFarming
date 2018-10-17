package com.songoda.epicfarming.utils;

import com.songoda.arconix.plugin.Arconix;
import com.songoda.epicfarming.EpicFarmingPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

public class Methods {

    public static ItemStack getGlass() {
        try {
            EpicFarmingPlugin plugin = EpicFarmingPlugin.getInstance();
            return Arconix.pl().getApi().getGUI().getGlass(plugin.getConfig().getBoolean("settings.Rainbow-Glass"), plugin.getConfig().getInt("Interfaces.Glass Type 1"));
        } catch (Exception ex) {
            Debugger.runReport(ex);
        }
        return null;
    }

    public static ItemStack getBackgroundGlass(boolean type) {
        try {
            EpicFarmingPlugin plugin = EpicFarmingPlugin.getInstance();
            if (type)
                return Arconix.pl().getApi().getGUI().getGlass(false, plugin.getConfig().getInt("Interfaces.Glass Type 2"));
            else
                return Arconix.pl().getApi().getGUI().getGlass(false, plugin.getConfig().getInt("Interfaces.Glass Type 3"));
        } catch (Exception ex) {
            Debugger.runReport(ex);
        }
        return null;
    }

    public static String formatName(int level, boolean full) {
        try {
            String name = EpicFarmingPlugin.getInstance().getLocale().getMessage("general.nametag.farm", level);

            String info = "";
            if (full) {
                info += Arconix.pl().getApi().format().convertToInvisibleString(level + ":");
            }

            return info + Arconix.pl().getApi().format().formatText(name);
        } catch (Exception ex) {
            Debugger.runReport(ex);
        }
        return null;
    }


    public static void animate(Location location, Material mat) {
        try {
            if (!EpicFarmingPlugin.getInstance().getConfig().getBoolean("Main.Animate")) return;
            Block block = location.getBlock();
            if (block.getRelative(0, 1, 0).getType() != Material.AIR && EpicFarmingPlugin.getInstance().getConfig().getBoolean("Main.Do Dispenser Animation"))
                return;
            Item i = block.getWorld().dropItem(block.getLocation().add(0.5, 1, 0.5), new ItemStack(mat));

            // Support for EpicHoppers suction.
            i.setMetadata("grabbed", new FixedMetadataValue(EpicFarmingPlugin.getInstance(), "true"));

            i.setMetadata("betterdrops_ignore", new FixedMetadataValue(EpicFarmingPlugin.getInstance(), true));
            i.setPickupDelay(3600);

            i.setVelocity(new Vector(0, .3, 0));

            Bukkit.getScheduler().scheduleSyncDelayedTask(EpicFarmingPlugin.getInstance(), i::remove, 10);
        } catch (Exception ex) {
            Debugger.runReport(ex);
        }
    }
}