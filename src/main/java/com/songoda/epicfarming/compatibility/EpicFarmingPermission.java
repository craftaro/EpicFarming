package com.songoda.epicfarming.compatibility;

import com.songoda.skyblock.core.compatibility.CompatibleMaterial;
import com.songoda.skyblock.permission.BasicPermission;
import com.songoda.skyblock.permission.PermissionType;

public class EpicFarmingPermission extends BasicPermission {

    public EpicFarmingPermission() {
        super("EpicFarming", CompatibleMaterial.END_ROD, PermissionType.GENERIC);
    }

}
