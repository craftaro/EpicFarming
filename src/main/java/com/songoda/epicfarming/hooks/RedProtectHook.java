package com.songoda.epicfarming.hooks;

import br.net.fabiozumbi12.RedProtect.Bukkit.API.RedProtectAPI;
import br.net.fabiozumbi12.RedProtect.Bukkit.RedProtect;
import com.songoda.epicfarming.EpicFarming;
import com.songoda.epicfarming.utils.Debugger;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * Created by songoda on 3/17/2017.
 */
public class RedProtectHook extends Hook {

    private EpicFarming plugin = EpicFarming.pl();

    public RedProtectHook() {
        super("RedProtect");
        if (isEnabled())
            plugin.hooks.RedProtectHook = this;
    }

    @Override
    public boolean canBuild(Player p, Location location) {
        try {
            RedProtectAPI rpAPI = RedProtect.get().getAPI();
            return hasBypass(p) || (rpAPI.getRegion(location) != null && rpAPI.getRegion(location).canBuild(p));
        } catch (Exception e) {
            Debugger.runReport(e);
        }
        return true;
    }

}
