package com.craftaro.epicfarming.farming;

import com.craftaro.epicfarming.EpicFarming;

public enum FarmType {
    CROPS, LIVESTOCK, BOTH;

    public String translate() {
        return EpicFarming.getPlugin(EpicFarming.class)
                .getLocale()
                .getMessage("general.interface." + name().toLowerCase())
                .getMessage();
    }

}
