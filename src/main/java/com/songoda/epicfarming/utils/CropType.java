package com.songoda.epicfarming.utils;

import com.songoda.core.compatibility.CompatibleMaterial;
import com.songoda.core.compatibility.ServerVersion;
import com.songoda.epicfarming.farming.Crop;
import org.bukkit.CropState;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.material.Crops;

public enum CropType {

    WHEAT("Wheat", CompatibleMaterial.WHEAT, CompatibleMaterial.WHEAT, CompatibleMaterial.WHEAT_SEEDS),

    CARROT("Carrot", CompatibleMaterial.CARROTS, CompatibleMaterial.CARROT, CompatibleMaterial.CARROT),

    POTATO("Potato", CompatibleMaterial.POTATOES, CompatibleMaterial.POTATO, CompatibleMaterial.POTATO),

    BEETROOT("Beetroot", CompatibleMaterial.BEETROOTS, CompatibleMaterial.BEETROOT, CompatibleMaterial.BEETROOT_SEEDS),

    WATER_MELON_STEM("Watermelon", CompatibleMaterial.MELON_STEM, CompatibleMaterial.MELON, CompatibleMaterial.MELON_SEEDS),

    PUMPKIN_STEM("Pumpkin", CompatibleMaterial.PUMPKIN_STEM, CompatibleMaterial.PUMPKIN, CompatibleMaterial.PUMPKIN_SEEDS),

    NETHER_WARTS("Nether Wart", CompatibleMaterial.NETHER_WART_BLOCK, CompatibleMaterial.NETHER_WART, CompatibleMaterial.NETHER_WART);

    private final String name;
    private final CompatibleMaterial yieldMaterial, blockMaterial, seedMaterial;

    CropType(String name, CompatibleMaterial blockMaterial, CompatibleMaterial yieldMaterial, CompatibleMaterial seedMaterial) {
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
        return blockMaterial.getMaterial();
    }

    /**
     * Get the yield Material that represents this crop type
     *
     * @return the represented yieldMaterial
     */
    public Material getYieldMaterial() {
        return yieldMaterial.getMaterial();
    }

    /**
     * Get the blockMaterial that represents the seed item for this crop type
     *
     * @return the represented seed blockMaterial
     */
    public Material getSeedMaterial() {
        return seedMaterial.getMaterial();
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

    /**
     * Checks if a crop is at its max growth stage
     *
     * @param block The crop block to check
     * @return True if the crop is at its max growth stage, otherwise false
     */
    public static boolean isMaxGrowthStage(Block block) {
        if (!isGrowableCrop(block.getType()))
            throw new IllegalArgumentException("Block given was not a valid crop");

        return block.getData() >= getMaxGrowthStage(block.getType());
    }

    /**
     * Gets the max growth stage for the given material
     *
     * @param material The material of the crop
     * @return The max growth stage of the given crop type
     */
    public static int getMaxGrowthStage(Material material) {
        if (!isGrowableCrop(material))
            throw new IllegalArgumentException("Block given was not a valid crop");

        if (material.equals(CompatibleMaterial.BEETROOTS.getMaterial()))
            return 3;

        return 7;
    }

    /**
     * Grows a crop by 1 growth stage
     *
     * @param crop The crop to grow
     */
    public static void grow(Crop crop) {
        if (ServerVersion.isServerVersionAtLeast(ServerVersion.V1_13)) {
            BlockState cropState = crop.getLocation().getBlock().getState();
            Crops cropData = (Crops) cropState.getData();

            Material material = crop.getLocation().getBlock().getType();

            switch (cropData.getState()) {
                case SEEDED:
                    if (material == Material.BEETROOTS)
                        cropData.setState(CropState.VERY_SMALL);
                    else
                        cropData.setState(CropState.GERMINATED);
                    break;
                case GERMINATED:
                    cropData.setState(CropState.VERY_SMALL);
                    break;
                case VERY_SMALL:
                    cropData.setState(CropState.SMALL);
                    break;
                case SMALL:
                    cropData.setState(CropState.MEDIUM);
                    break;
                case MEDIUM:
                    cropData.setState(CropState.TALL);
                    break;
                case TALL:
                    cropData.setState(CropState.VERY_TALL);
                    break;
                case VERY_TALL:
                    cropData.setState(CropState.RIPE);
                    break;
                case RIPE:
                    break;
            }
            cropState.setData(cropData);
            cropState.update();
            crop.setTicksLived(1);
            return;
        }

        Block block = crop.getLocation().getBlock();

        if (!isGrowableCrop(block.getType()))
            throw new IllegalArgumentException("Block given was not a valid crop");

        byte data = block.getData();

        if (isMaxGrowthStage(block))
            return;

        block.setData((byte) (data + 1));
    }

    /**
     * Sets a crop's growth back to stage 0
     *
     * @param block The crop block to set
     */
    public static void replant(Block block) {
        if (ServerVersion.isServerVersionAtLeast(ServerVersion.V1_13)) {
            BlockState cropState = block.getState();
            Crops cropData = (Crops) cropState.getData();
            cropData.setState(CropState.SEEDED);
            cropState.setData(cropData);
            cropState.update();
            return;
        }
        if (!isGrowableCrop(block.getType()))
            throw new IllegalArgumentException("Block given was not a valid crop");

        block.setData((byte) 0);
    }
}