package com.songoda.epicfarming.hooks;

import com.songoda.epicfarming.EpicFarming;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public abstract class Hook {

    protected final String pluginName;

    protected Hook(String pluginName) {
        this.pluginName = pluginName;
        if (isEnabled())
            EpicFarming.getInstance().hooks.hooksFile.getConfig().addDefault("hooks." + pluginName, true);
    }

    protected boolean isEnabled() {
        return (Bukkit.getPluginManager().isPluginEnabled(pluginName)
                && EpicFarming.getInstance().hooks.hooksFile.getConfig().getBoolean("hooks." + pluginName,true));
    }

    protected boolean hasBypass(Player p) {
        return p.hasPermission(EpicFarming.getInstance().getDescription().getName() + ".bypass");
    }

    public abstract boolean canBuild(Player p, Location location);

    public boolean isInClaim(String id, Location location) {
        return false;
    }

    public String getClaimId(String name) {
        return null;
    }




}
