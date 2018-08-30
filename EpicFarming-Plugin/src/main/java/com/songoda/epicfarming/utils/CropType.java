package com.songoda.epicfarming.utils;

import org.bukkit.Material;

public enum  CropType {

    WHEAT("Wheat", Material.WHEAT, Material.WHEAT, Material.WHEAT_SEEDS),

    CARROT("Carrot", Material.CARROTS, Material.CARROT, Material.CARROT),

    POTATO("Potato", Material.POTATOES, Material.POTATO, Material.POTATO),

    BEETROOT("Beetroot", Material.BEETROOTS, Material.BEETROOT, Material.BEETROOT_SEEDS),

    WATER_MELON_STEM("Watermelon", Material.MELON_STEM, Material.MELON, Material.MELON_SEEDS),

    PUMPKIN_STEM("Pumpkin", Material.PUMPKIN_STEM, Material.PUMPKIN, Material.PUMPKIN_SEEDS),

    NETHER_WARTS("Nether Wart", Material.NETHER_WART_BLOCK, Material.NETHER_WART, Material.NETHER_WART);

    private final String name;
    private final Material yieldMaterial, blockMaterial, seedMaterial;

    CropType(String name, Material blockMaterial, Material yieldMaterial, Material seedMaterial) {
        this.name = name;
        this.blockMaterial = blockMaterial;
        this.seedMaterial = seedMaterial;
        this.yieldMaterial = yieldMaterial;
    }


    /**
     * Get the friendly name of the crop
     *
     * @return the name of the crop
     */
    public String getName() {
        return name;
    }

    /**
     * Get the blockMaterial that represents this crop type
     *
     * @return the represented blockMaterial
     */
    public Material getBlockMaterial() {
        return blockMaterial;
    }

    /**
     * Get the yield Material that represents this crop type
     *
     * @return the represented yieldMaterial
     */
    public Material getYieldMaterial() {
        return yieldMaterial;
    }

    /**
     * Get the blockMaterial that represents the seed item for this crop type
     *
     * @return the represented seed blockMaterial
     */
    public Material getSeedMaterial() {
        return seedMaterial;
    }

    /**
     * Check whether a specific blockMaterial is an enumerated crop type or not
     *
     * @param material the blockMaterial to check
     * @return true if it is a crop, false otherwise
     */
    public static boolean isCrop(Material material) {
        for (CropType type : values())
            if (type.getBlockMaterial() == material) return true;
        return false;
    }

    /**
     * Check whether a specific blockMaterial is an enumerated crop type seed or not
     *
     * @param material the blockMaterial to check
     * @return true if it is a seed, false otherwise
     */
    public static boolean isCropSeed(Material material) {
        for (CropType type : values())
            if (type.getSeedMaterial() == material) return true;
        return false;
    }

    /**
     * Get the crop type based on the specified blockMaterial
     *
     * @param material the crop blockMaterial
     * @return the respective CropType. null if none found
     */
    public static CropType getCropType(Material material) {
        for (CropType type : values())
            if (type.getBlockMaterial() == material) return type;
        return null;
    }

}