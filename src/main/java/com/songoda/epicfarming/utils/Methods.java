package com.songoda.epicfarming.utils;

import com.craftaro.core.third_party.com.cryptomorin.xseries.XMaterial;
import com.songoda.epicfarming.EpicFarming;
import com.songoda.epicfarming.settings.Settings;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Methods {
    private static final Map<String, Location> SERIALIZE_CACHE = new HashMap<>();

    public static String formatName(int level) {
        return EpicFarming.getPlugin(EpicFarming.class)
                .getLocale()
                .getMessage("general.nametag.farm")
                .processPlaceholder("level", level)
                .getMessage();
    }

    public static void animate(Location location, XMaterial material) {
        animate(location, material.parseItem());
    }

    public static void animate(Location location, ItemStack item) {
        if (!Settings.ANIMATE.getBoolean()) {
            return;
        }

        Block block = location.getBlock();
        if (block.getRelative(0, 1, 0).getType() != Material.AIR) {
            return;
        }

        final EpicFarming epicFarmingPlugin = EpicFarming.getPlugin(EpicFarming.class);
        Item droppedItem = block.getWorld().dropItem(block.getLocation().add(0.5, 1, 0.5), item);

        // Support for EpicHoppers suction.
        droppedItem.setMetadata("grabbed", new FixedMetadataValue(epicFarmingPlugin, "true"));

        droppedItem.setMetadata("betterdrops_ignore", new FixedMetadataValue(epicFarmingPlugin, true));
        droppedItem.setPickupDelay(3600);

        droppedItem.setVelocity(new Vector(0, .3, 0));

        Bukkit.getScheduler().scheduleSyncDelayedTask(epicFarmingPlugin, droppedItem::remove, 10);
    }

    /**
     * Serializes the location specified.
     *
     * @param location The location that is to be saved.
     * @return The serialized data.
     */
    public static String serializeLocation(Location location) {
        if (location == null || location.getWorld() == null) {
            return "";
        }
        String w = location.getWorld().getName();
        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();
        String str = w + ":" + x + ":" + y + ":" + z;
        str = str.replace(".0", "").replace(".", "/");
        return str;
    }

    /**
     * Deserializes a location from the string.
     *
     * @param str The string to parse.
     * @return The location that was serialized in the string.
     */
    public static Location deserializeLocation(String str) {
        if (str == null || str.equals("")) {
            return null;
        }
        if (SERIALIZE_CACHE.containsKey(str)) {
            return SERIALIZE_CACHE.get(str).clone();
        }
        List<String> args = Arrays.asList(str.split("\\s*:\\s*"));

        World world = Bukkit.getWorld(args.get(0));
        double x = Double.parseDouble(args.get(1)), y = Double.parseDouble(args.get(2)), z = Double.parseDouble(args.get(3));
        Location location = new Location(world, x, y, z, 0, 0);
        SERIALIZE_CACHE.put(str, location.clone());
        return location;
    }
}
