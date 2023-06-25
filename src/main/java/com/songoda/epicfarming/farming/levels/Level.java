package com.songoda.epicfarming.farming.levels;

import com.songoda.epicfarming.EpicFarming;
import com.songoda.epicfarming.farming.levels.modules.Module;

import java.util.ArrayList;
import java.util.List;

public class Level {

    private final ArrayList<Module> registeredModules;
    private final List<String> description = new ArrayList<>();
    private final int level;
    private final int costExperience;
    private final int costEconomy;
    private final int radius;
    private final int pages;
    private final double speedMultiplier;
    private final boolean autoReplant;


    Level(int level, int costExperience, int costEconomy, double speedMultiplier, int radius, boolean autoReplant, int pages, ArrayList<Module> registeredModules) {
        this.level = level;
        this.costExperience = costExperience;
        this.costEconomy = costEconomy;
        this.speedMultiplier = speedMultiplier;
        this.radius = radius;
        this.autoReplant = autoReplant;
        this.pages = pages;
        this.registeredModules = registeredModules;

        buildDescription();
    }

    public void buildDescription() {
        EpicFarming instance = EpicFarming.getPlugin(EpicFarming.class);

        this.description.add(instance.getLocale().getMessage("interface.button.radius")
                .processPlaceholder("radius", this.radius).getMessage());

        this.description.add(instance.getLocale().getMessage("interface.button.speed")
                .processPlaceholder("speed", this.speedMultiplier).getMessage());


        if (this.autoReplant) {
            this.description.add(instance.getLocale().getMessage("interface.button.autoreplant")
                    .processPlaceholder("status",
                            instance.getLocale().getMessage("general.interface.unlocked")
                                    .getMessage()).getMessage());
        }

        if (this.pages > 1) {
            this.description.add(instance.getLocale().getMessage("interface.button.pages")
                    .processPlaceholder("amount", this.pages).getMessage());
        }

        for (Module module : this.registeredModules) {
            this.description.add(module.getDescription());
        }
    }

    public List<String> getDescription() {
        return new ArrayList<>(this.description);
    }

    public int getLevel() {
        return this.level;
    }

    public int getRadius() {
        return this.radius;
    }

    public boolean isAutoReplant() {
        return this.autoReplant;
    }

    public int getPages() {
        return this.pages;
    }

    public double getSpeedMultiplier() {
        return this.speedMultiplier;
    }

    public int getCostExperience() {
        return this.costExperience;
    }

    public int getCostEconomy() {
        return this.costEconomy;
    }

    public ArrayList<Module> getRegisteredModules() {
        return new ArrayList<>(this.registeredModules);
    }

    public void addModule(Module module) {
        this.registeredModules.add(module);
        buildDescription();
    }

    public Module getModule(String name) {
        return this.registeredModules == null ? null :
                this.registeredModules.stream().filter(module -> module.getName().equals(name)).findFirst().orElse(null);
    }
}
