package com.songoda.epicfarming;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

public enum Lang {

    PREFIX("prefix", "&8[&6EpicFarming&8]"),
    NEXT("Next", "&9Next"),
    BACK("Back", "&9Back"),

    XPTITLE("Xp-upgrade-title", "&aUpgrade with XP"),
    XPLORE("Xp-upgrade-lore", "&7Cost: &a{COST} Levels"),

    ECOTITLE("Eco-upgrade-title", "&aUpgrade with ECO"),
    ECOLORE("Eco-upgrade-lore", "&7Cost: &a${COST}"),

    LEVEL("Level", "&6Farm Level &7{LEVEL}"),
    NEXT_LEVEL("Next-Level", "&6Next Level &7{LEVEL}"),

    NEXT_RADIUS("Next-Radius", "&7Radius: &6{RADIUS}"),
    NEXT_SPEED("Next-Speed", "&7Speed: &6{SPEED}x"),
    NEXT_AUTO_HARVEST("Next-Auto-Harvest", "&7Auto Harvest: &6{AUTOHARVEST}"),
    NEXT_AUTO_REPLANT("Next-Auto-Replant", "&7Auto Replant: &6{AUTOREPLANT}"),

    CANT_AFFORD("Cant-afford", "&cYou cannot afford this upgrade."),

    UPGRADE_MESSAGE("Upgrade-message", "&7You successfully upgraded this farm to &6level {LEVEL}&7!"),

    MAXED("Maxed", "&6This farm is maxed out."),

    YOU_MAXED("You-Maxed", "&7You maxed out this farm at &6{NEW}x&7."),

    NAME_FORMAT("Name-format", "&eLevel {LEVEL} &fFarm"),

    NO_PERMS("No-perms", "&cYou do not have permission to do that.");

    private String path;
    private String def;
    private static FileConfiguration LANG;

    Lang(String path, String start) {
        this.path = path;
        this.def = start;
    }

    public static void setFile(final FileConfiguration config) {
        LANG = config;
    }

    public String getDefault() {
        return this.def;
    }

    public String getPath() {
        return this.path;
    }

    public String getConfigValue() {
        return getConfigValue(null, null, null);
    }

    public String getConfigValue(String arg) {
        return getConfigValue(arg, null, null);
    }

    public String getConfigValue(int arg) {
        return getConfigValue(Integer.toString(arg));
    }

    public String getConfigValue(double arg) {
        return getConfigValue(Double.toString(arg));
    }

    public String getConfigValue(boolean arg) {
        return getConfigValue(Boolean.toString(arg));
    }

        public String getConfigValue(String arg, String arg2, String arg3) {
        String value = ChatColor.translateAlternateColorCodes('&', LANG.getString(this.path, this.def));

            if (arg != null) {
                value = value.replace("{COST}", arg);
                value = value.replace("{GOAL}", arg);
                value = value.replace("{NEW}", arg);
                value = value.replace("{MAX}", arg);
                value = value.replace("{AMT}", arg);
                value = value.replace("{LEVEL}", arg);
                value = value.replace("{NEWM}", arg);

                value = value.replace("{RADIUS}", arg);
                value = value.replace("{SPEED}", arg);
                value = value.replace("{AUTOHARVEST}", arg);
                value = value.replace("{AUTOREPLANT}", arg);

                if (arg2 != null) {
                    value = value.replace("{TYPE}", arg2);
                }
                value = value.replace("{TYPE}", arg);
                if (arg3 != null)
                    value = value.replace("{TIME}", arg3);
            }

            if (arg2 != null) {
                value = value.replace("{COST}", arg2).replace("{GOAL}", arg2).replace("{NEW}", arg2).replace("{MAX}", arg2).replace("{AMT}", arg2)
                        .replace("{LEVEL}", arg2).replace("{TYPE}", arg2).replace("{NEWM}", arg2);
            }

        return value;
    }
}
