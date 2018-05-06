package com.songoda.epicfarming.utils;

import com.songoda.arconix.plugin.Arconix;
import com.songoda.epicfarming.EpicFarming;
import com.songoda.epicfarming.Lang;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Created by songoda on 2/24/2017.
 */
public class Methods {

    public static ItemStack getGlass() {
        try {
            EpicFarming plugin = EpicFarming.pl();
            return Arconix.pl().getApi().getGUI().getGlass(plugin.getConfig().getBoolean("settings.Rainbow-Glass"), plugin.getConfig().getInt("Interfaces.Glass Type 1"));
        } catch (Exception ex) {
            Debugger.runReport(ex);
        }
        return null;
    }

    public static ItemStack getBackgroundGlass(boolean type) {
        try {
            EpicFarming plugin = EpicFarming.pl();
            if (type)
                return Arconix.pl().getApi().getGUI().getGlass(false, plugin.getConfig().getInt("Interfaces.Glass Type 2"));
            else
                return Arconix.pl().getApi().getGUI().getGlass(false, plugin.getConfig().getInt("Interfaces.Glass Type 3"));
        } catch (Exception ex) {
            Debugger.runReport(ex);
        }
        return null;
    }

    public static int getLevelFromItem(ItemStack item) {
        try {
            if (!item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) return 0;
            if (item.getItemMeta().getDisplayName().contains(":")) {
                return NumberUtils.toInt(item.getItemMeta().getDisplayName().replace("\u00A7", "").split(":")[0], 0);
            }
        } catch (Exception ex) {
            Debugger.runReport(ex);
        }
        return 0;
    }

    public static String formatName(int level, boolean full) {
        try {
            String name = Lang.NAME_FORMAT.getConfigValue(level);

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

    public static ItemStack makeFarmItem(int level) {
        ItemStack item = new ItemStack(Material.END_ROD, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(Arconix.pl().getApi().format().formatText(Methods.formatName(level, true)));
        item.setItemMeta(meta);
        return item;
    }
}
