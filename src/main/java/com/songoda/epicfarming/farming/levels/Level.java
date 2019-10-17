package com.songoda.epicfarming.farming.levels;

import com.songoda.epicfarming.EpicFarming;
import com.songoda.epicfarming.farming.levels.modules.Module;

import java.util.ArrayList;
import java.util.List;

public class Level {

    private final ArrayList<Module> registeredModules;
    private List<String> description = new ArrayList<>();
    private int level, costExperiance, costEconomy, radius, pages;
    private double speedMultiplier;
    private boolean autoReplant;


    Level(int level, int costExperiance, int costEconomy, double speedMultiplier, int radius, boolean autoReplant, int pages, ArrayList<Module> registeredModules) {
        this.level = level;
        this.costExperiance = costExperiance;
        this.costEconomy = costEconomy;
        this.speedMultiplier = speedMultiplier;
        this.radius = radius;
        this.autoReplant = autoReplant;
        this.pages = pages;
        this.registeredModules = registeredModules;
        buildDescription();
    }

    public void buildDescription() {
        EpicFarming instance = EpicFarming.getInstance();

        description.add(instance.getLocale().getMessage("interface.button.radius")
                .processPlaceholder("radius", radius).getMessage());

        description.add(instance.getLocale().getMessage("interface.button.speed")
                .processPlaceholder("speed", speedMultiplier).getMessage());


        if (autoReplant)
            description.add(instance.getLocale().getMessage("interface.button.autoreplant")
                    .processPlaceholder("status",
                            instance.getLocale().getMessage("general.interface.unlocked")
                                    .getMessage()).getMessage());

        if (pages > 1)
            description.add(instance.getLocale().getMessage("interface.button.pages")
                    .processPlaceholder("amount", pages).getMessage());

        for (Module module : registeredModules) {
            description.add(module.getDescription());
        }
    }

    public List<String> getDescription() {
        return new ArrayList<>(description);
    }

    public int getLevel() {
        return level;
    }

    public int getRadius() {
        return radius;
    }

    public boolean isAutoReplant() {
        return autoReplant;
    }

    public int getPages() {
        return pages;
    }

    public double getSpeedMultiplier() {
        return speedMultiplier;
    }

    public int getCostExperiance() {
        return costExperiance;
    }

    public int getCostEconomy() {
        return costEconomy;
    }

    public ArrayList<Module> getRegisteredModules() {
        return new ArrayList<>(registeredModules);
    }

    public void addModule(Module module) {
        registeredModules.add(module);
        buildDescription();
    }

    public Module getModule(String name) {
        return registeredModules == null ? null :
                registeredModules.stream().filter(module -> module.getName().equals(name)).findFirst().orElse(null);
    }
}
