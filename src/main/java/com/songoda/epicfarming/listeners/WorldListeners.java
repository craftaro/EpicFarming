package com.songoda.epicfarming.listeners;

import com.songoda.epicfarming.EpicFarming;
import com.songoda.epicfarming.farming.Farm;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;

import java.util.Map;
import java.util.function.Consumer;

/**
 * Created by noahvdaa on 5/21/2021.
 */
public class WorldListeners implements Listener {

	private final EpicFarming plugin;

	public WorldListeners(EpicFarming plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onWorldUnload(WorldUnloadEvent e){
		for (Farm f : plugin.getFarmManager().getFarms().values()) {
			if (e.getWorld().getName().equals(f.getLocation().getWorld().getName())) {
				plugin.getFarmManager().removeFarm(f.getLocation());
			}
		}
	}

	@EventHandler
	public void onWorldLoad(WorldLoadEvent e) {
		// Unload previous farms belonging to this world.
		for (Farm f : plugin.getFarmManager().getFarms().values()) {
			if (e.getWorld().getName().equals(f.getLocation().getWorld().getName())) {
				plugin.getFarmManager().removeFarm(f.getLocation());
			}
		}

		// Load farms back in.
		plugin.getDataManager().getFarms(new Consumer<Map<Integer, Farm>>() {
			@Override
			public void accept(Map<Integer, Farm> locationFarmMap) {
				for (Farm f : locationFarmMap.values()) {
					if (e.getWorld().getName().equals(f.getLocation().getWorld().getName())) {
						plugin.getFarmManager().addFarm(f.getLocation(), f);
					}
				}
			}
		});
	}

}