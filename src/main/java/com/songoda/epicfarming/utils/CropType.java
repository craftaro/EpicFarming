package com.songoda.epicfarming.utils;

import com.songoda.core.compatibility.CompatibleMaterial;
import org.bukkit.Material;

public enum CropType {

    WHEAT("Wheat", CompatibleMaterial.WHEAT, CompatibleMaterial.WHEAT, CompatibleMaterial.WHEAT_SEEDS),

    CARROT("Carrot", CompatibleMaterial.CARROTS, CompatibleMaterial.CARROT, CompatibleMaterial.CARROT),

    POTATO("Potato", CompatibleMaterial.POTATOES, CompatibleMaterial.POTATO, CompatibleMaterial.POTATO),

    BEETROOT("Beetroot", CompatibleMaterial.BEETROOTS, CompatibleMaterial.BEETROOT, CompatibleMaterial.BEETROOT_SEEDS),

    WATER_MELON_STEM("Watermelon", CompatibleMaterial.MELON_STEM, CompatibleMaterial.MELON, CompatibleMaterial.MELON_SEEDS),

    PUMPKIN_STEM("Pumpkin", CompatibleMaterial.PUMPKIN_STEM, CompatibleMaterial.PUMPKIN, CompatibleMaterial.PUMPKIN_SEEDS),

    NETHER_WARTS("Nether Wart", CompatibleMaterial.NETHER_WART, CompatibleMaterial.NETHER_WART, CompatibleMaterial.NETHER_WART);

    private final String name;
    private final Material yieldMaterial, blockMaterial, seedMaterial;

    CropType(String name, CompatibleMaterial blockMaterial, CompatibleMaterial yieldMaterial, CompatibleMaterial seedMaterial) {
        this.name = name;
        this.blockMaterial = blockMaterial.getBlockMaterial();
        this.seedMaterial = seedMaterial.getMaterial();
        this.yieldMaterial = yieldMaterial.getMaterial();
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

    /**
     * Checks if a crop is growable
     *
     * @param material The material to check
     * @return True if the material is of a growable crop, otherwise false
     */
    public static boolean isGrowableCrop(Material material) {
        return CropType.isCrop(material);
    }

}