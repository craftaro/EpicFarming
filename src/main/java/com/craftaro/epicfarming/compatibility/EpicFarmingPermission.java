package com.craftaro.epicfarming.compatibility;

import com.craftaro.skyblock.core.third_party.com.cryptomorin.xseries.XMaterial;
import com.craftaro.skyblock.permission.BasicPermission;
import com.craftaro.skyblock.permission.PermissionType;

public class EpicFarmingPermission extends BasicPermission {
    public EpicFarmingPermission() {
        super("EpicFarming", XMaterial.END_ROD, PermissionType.GENERIC);
    }
}
