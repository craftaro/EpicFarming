package com.songoda.epicfarming;

public class References {

    private String prefix;

    public References() {
        prefix = EpicFarmingPlugin.getInstance().getLocale().getMessage("general.nametag.prefix") + " ";
    }

    public String getPrefix() {
        return this.prefix;
    }
}