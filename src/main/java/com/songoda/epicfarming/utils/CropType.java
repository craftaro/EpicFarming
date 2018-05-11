package com.songoda.epicfarming.utils;

import com.songoda.epicfarming.EpicFarming;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

/**
 * A list of all crop types available in Minecraft that can be
 * modified by CropsReborn
 */
public class CropType {
    private static List<CropTypeData> crops = new ArrayList<>();

    private CropType() {
        crops.add(new CropTypeData("Wheat", Material.CROPS, Material.WHEAT, Material.SEEDS));
        crops.add(new CropTypeData("Carrot", Material.CARROT, Material.CARROT_ITEM, Material.CARROT_ITEM));
        crops.add(new CropTypeData("Potato", Material.POTATO, Material.CARROT_ITEM, Material.POTATO_ITEM));
        crops.add(new CropTypeData("Watermelon", Material.MELON_STEM, Material.MELON, Material.MELON_SEEDS));
        crops.add(new CropTypeData("Pumpkin", Material.PUMPKIN_STEM, Material.PUMPKIN, Material.PUMPKIN_SEEDS));
        crops.add(new CropTypeData("Nether Wart", Material.NETHER_WART_BLOCK, Material.NETHER_WARTS, Material.NETHER_WARTS));

        if (!EpicFarming.pl().v1_8 && !EpicFarming.pl().v1_7) {
            crops.add(new CropTypeData("Beetroot", Material.BEETROOT_BLOCK, Material.BEETROOT, Material.BEETROOT_SEEDS));
        }
    }

    private void handleAdd() {
        if (crops.size() < 1) {
            crops.add(new CropTypeData("Wheat", Material.CROPS, Material.WHEAT, Material.SEEDS));
            crops.add(new CropTypeData("Carrot", Material.CARROT, Material.CARROT_ITEM, Material.CARROT_ITEM));
            crops.add(new CropTypeData("Potato", Material.POTATO, Material.CARROT_ITEM, Material.POTATO_ITEM));
            crops.add(new CropTypeData("Watermelon", Material.MELON_STEM, Material.MELON, Material.MELON_SEEDS));
            crops.add(new CropTypeData("Pumpkin", Material.PUMPKIN_STEM, Material.PUMPKIN, Material.PUMPKIN_SEEDS));
            crops.add(new CropTypeData("Nether Wart", Material.NETHER_WART_BLOCK, Material.NETHER_WARTS, Material.NETHER_WARTS));

            if (!EpicFarming.pl().v1_8 && !EpicFarming.pl().v1_7) {
                crops.add(new CropTypeData("Beetroot", Material.BEETROOT_BLOCK, Material.BEETROOT, Material.BEETROOT_SEEDS));
            }
        }
    }

    public static boolean isCrop(Material material) {
        for (CropTypeData type : values())
            if (type.getBlockMaterial() == material) return true;
        return false;
    }

    public static boolean isCropSeed(Material material) {
        for (CropTypeData type : values())
            if (type.getSeedMaterial() == material) return true;
        return false;
    }

    public static CropTypeData getCropType(Material material) {
        for (CropTypeData type : values())
            if (type.getBlockMaterial() == material) return type;
        return null;
    }

    public static List<CropTypeData> values() {
        if (crops.size() < 1) {
            new CropType().handleAdd();
        }
        return crops;
    }




    /*
    WHEAT("Wheat", Material.CROPS, Material.WHEAT, Material.SEEDS),

    CARROT("Carrot", Material.CARROT, Material.CARROT_ITEM, Material.CARROT_ITEM),

    POTATO("Potato", Material.POTATO, Material.CARROT_ITEM, Material.POTATO_ITEM),

    BEETROOT("Beetroot", Material.BEETROOT_BLOCK, Material.BEETROOT, Material.BEETROOT_SEEDS),

    WATER_MELON_STEM("Watermelon", Material.MELON_STEM, Material.MELON, Material.MELON_SEEDS),

    PUMPKIN_STEM("Pumpkin", Material.PUMPKIN_STEM, Material.PUMPKIN, Material.PUMPKIN_SEEDS),

    NETHER_WARTS("Nether Wart", Material.NETHER_WART_BLOCK, Material.NETHER_WARTS, Material.NETHER_WARTS);

    private final String name;
    private final Material yieldMaterial, blockMaterial, seedMaterial;

    CropType(String name, Material blockMaterial, Material yieldMaterial, Material seedMaterial) {
        this.name = name;
        this.blockMaterial = blockMaterial;
        this.seedMaterial = seedMaterial;
        this.yieldMaterial = yieldMaterial;
    }

    public String getName() {
        return name;
    }

    public Material getBlockMaterial() {
        return blockMaterial;
    }

    public Material getYieldMaterial() {
        return yieldMaterial;
    }

    public Material getSeedMaterial() {
        return seedMaterial;
    }
    */

    public class CropTypeData {
        private final String name;
        private final Material blockMaterial;
        private final Material seedMaterial;
        private final Material yieldMaterial;

        public CropTypeData(String _name, Material _blockMaterial, Material _seedMaterial, Material _yieldMaterial) {
            name = _name;
            blockMaterial = _blockMaterial;
            seedMaterial = _seedMaterial;
            yieldMaterial = _yieldMaterial;
        }

        public String getName() {
            return name;
        }

        public Material getBlockMaterial() {
            return blockMaterial;
        }

        public Material getSeedMaterial() {
            return seedMaterial;
        }

        public Material getYieldMaterial() {
            return yieldMaterial;
        }
    }
}