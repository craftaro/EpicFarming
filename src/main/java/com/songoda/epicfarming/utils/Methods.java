package com.songoda.epicfarming.utils;

import com.songoda.epicfarming.EpicFarming;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class Methods {

    private static Map<String, Location> serializeCache = new HashMap<>();

    public static ItemStack getGlass() {
        try {
            EpicFarming instance = EpicFarming.getInstance();
            return getGlass(instance.getConfig().getBoolean("Interfaces.Replace Glass Type 1 With Rainbow Glass"), instance.getConfig().getInt("Interfaces.Glass Type 1"));
        } catch (Exception e) {
            Debugger.runReport(e);
        }
        return null;
    }

    public static ItemStack getBackgroundGlass(boolean type) {
        try {
            EpicFarming instance = EpicFarming.getInstance();
            if (type)
                return getGlass(false, instance.getConfig().getInt("Interfaces.Glass Type 2"));
            else
                return getGlass(false, instance.getConfig().getInt("Interfaces.Glass Type 3"));
        } catch (Exception e) {
            Debugger.runReport(e);
        }
        return null;
    }

    private static ItemStack getGlass(Boolean rainbow, int type) {
        int randomNum = 1 + (int) (Math.random() * 6);
        ItemStack glass;
        if (rainbow) {
            glass = new ItemStack(Material.LEGACY_STAINED_GLASS_PANE, 1, (short) randomNum);
        } else {
            glass = new ItemStack(Material.LEGACY_STAINED_GLASS_PANE, 1, (short) type);
        }
        ItemMeta glassmeta = glass.getItemMeta();
        glassmeta.setDisplayName("§l");
        glass.setItemMeta(glassmeta);
        return glass;
    }

    public static String formatName(int level, boolean full) {
        try {
            String name = EpicFarming.getInstance().getLocale().getMessage("general.nametag.farm", level);

            String info = "";
            if (full) {
                info += Methods.convertToInvisibleString(level + ":");
            }

            return info + Methods.formatText(name);
        } catch (Exception ex) {
            Debugger.runReport(ex);
        }
        return null;
    }

    public static void animate(Location location, Material mat) {
        try {
            if (!EpicFarming.getInstance().getConfig().getBoolean("Main.Animate")) return;
            Block block = location.getBlock();
            if (block.getRelative(0, 1, 0).getType() != Material.AIR && EpicFarming.getInstance().getConfig().getBoolean("Main.Do Dispenser Animation"))
                return;
            Item i = block.getWorld().dropItem(block.getLocation().add(0.5, 1, 0.5), new ItemStack(mat));

            // Support for EpicHoppers suction.
            i.setMetadata("grabbed", new FixedMetadataValue(EpicFarming.getInstance(), "true"));

            i.setMetadata("betterdrops_ignore", new FixedMetadataValue(EpicFarming.getInstance(), true));
            i.setPickupDelay(3600);

            i.setVelocity(new Vector(0, .3, 0));

            Bukkit.getScheduler().scheduleSyncDelayedTask(EpicFarming.getInstance(), i::remove, 10);
        } catch (Exception ex) {
            Debugger.runReport(ex);
        }
    }

    public static String formatText(String text) {
        if (text == null || text.equals(""))
            return "";
        return formatText(text, false);
    }

    public static String formatText(String text, boolean cap) {
        if (text == null || text.equals(""))
            return "";
        if (cap)
            text = text.substring(0, 1).toUpperCase() + text.substring(1);
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    public static String convertToInvisibleString(String s) {
        if (s == null || s.equals(""))
            return "";
        StringBuilder hidden = new StringBuilder();
        for (char c : s.toCharArray()) hidden.append(ChatColor.COLOR_CHAR + "").append(c);
        return hidden.toString();
    }

    /**
     * Serializes the location of the block specified.
     *
     * @param b The block whose location is to be saved.
     * @return The serialized data.
     */
    public static String serializeLocation(Block b) {
        if (b == null)
            return "";
        return serializeLocation(b.getLocation());
    }

    /**
     * Serializes the location specified.
     *
     * @param location The location that is to be saved.
     * @return The serialized data.
     */
    public static String serializeLocation(Location location) {
        if (location == null)
            return "";
        String w = location.getWorld().getName();
        double x = location.getX();
        double y = location.getY();
        double z = location.getZ();
        String str = w + ":" + x + ":" + y + ":" + z;
        str = str.replace(".0", "").replace("/", "");
        return str;
    }

    /**
     * Deserializes a location from the string.
     *
     * @param str The string to parse.
     * @return The location that was serialized in the string.
     */
    public static Location unserializeLocation(String str) {
        if (str == null || str.equals(""))
            return null;
        if (serializeCache.containsKey(str)) {
            return serializeCache.get(str).clone();
        }
        String cacheKey = str;
        str = str.replace("y:", ":").replace("z:", ":").replace("w:", "").replace("x:", ":").replace("/", ".");
        List<String> args = Arrays.asList(str.split("\\s*:\\s*"));

        World world = Bukkit.getWorld(args.get(0));
        double x = Double.parseDouble(args.get(1)), y = Double.parseDouble(args.get(2)), z = Double.parseDouble(args.get(3));
        Location location = new Location(world, x, y, z, 0, 0);
        serializeCache.put(cacheKey, location.clone());
        return location;
    }

    /**
     * Makes the specified Unix Epoch time human readable as per the format settings in the Arconix config.
     *
     * @param time The time to convert.
     * @return A human readable string representing to specified time.
     */
    public static String makeReadable(Long time) {
        if (time == null)
            return "";
        return String.format("%d hour(s), %d min(s), %d sec(s)", TimeUnit.MILLISECONDS.toHours(time), TimeUnit.MILLISECONDS.toMinutes(time) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(time)), TimeUnit.MILLISECONDS.toSeconds(time) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(time)));
    }

    /**
     * Formats the specified double into the Economy format specified in the Arconix config.
     *
     * @param amt The double to format.
     * @return The economy formatted double.
     */
    public static String formatEconomy(double amt) {
        DecimalFormat formatter = new DecimalFormat("#,###.00");
        return formatter.format(amt);
    }

    public static boolean isInt(String number) {
        if (number == null || number.equals(""))
            return false;
        try {
            Integer.parseInt(number);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

}