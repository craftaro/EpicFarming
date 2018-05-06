package com.songoda.epicfarming.hooks;

import com.intellectualcrafters.plot.api.PlotAPI;
import com.songoda.epicfarming.EpicFarming;
import com.songoda.epicfarming.utils.Debugger;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * Created by songoda on 3/17/2017.
 */
public class PlotSquaredHook extends Hook {

    private PlotAPI plotAPI;

    public PlotSquaredHook() {
        super("PlotSquared");
        if (isEnabled()) {
            EpicFarming plugin = EpicFarming.pl();
            plugin.hooks.PlotSquaredHook = this;
            this.plotAPI = new PlotAPI();
        }
    }

    @Override
    public boolean canBuild(Player p, Location location) {
        try {
            return hasBypass(p)
                    || (plotAPI.getPlot(location) != null
                    && plotAPI.isInPlot(p)
                    && plotAPI.getPlot(p) == plotAPI.getPlot(location));
        } catch (Exception e) {
            Debugger.runReport(e);
        }
        return true;
    }

}
