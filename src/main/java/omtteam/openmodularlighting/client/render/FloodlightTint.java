package omtteam.openmodularlighting.client.render;

import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import omtteam.omlib.util.world.WorldUtil;
import omtteam.openmodularlighting.client.camo.CamoModels;
import omtteam.openmodularlighting.registry.ModBlocks;
import omtteam.openmodularlighting.tileentity.FloodlightTileEntity;

import javax.annotation.Nullable;

/**
 * Tints dyed floodlights (1.7.10 {@code RotatableBlockRenderer}): the dye's map colour, brightened per block so that
 * the dark textures do not get too dark.
 */
public final class FloodlightTint {
    private static final int NO_TINT = 0xFFFFFF;

    private FloodlightTint() {
    }

    /**
     * The block colour of a light ({@code IBlockColor}, registered for all light sources). On a disguised light,
     * tinted quads other than the lamp belong to the disguise and get its colour.
     */
    public static int color(IBlockState state, @Nullable IBlockAccess world, @Nullable BlockPos pos, int tintIndex) {
        if (world == null || pos == null) return NO_TINT;
        TileEntity tile = WorldUtil.getTileEntity(world, pos);
        if (!(tile instanceof FloodlightTileEntity)) return NO_TINT;
        FloodlightTileEntity light = (FloodlightTileEntity) tile;
        IBlockState camo = light.getCamo();
        if (camo != null && tintIndex != CamoModels.LAMP_TINT_INDEX) {
            return Minecraft.getMinecraft().getBlockColors().colorMultiplier(camo, world, pos, tintIndex);
        }
        return tint(light.getColor(), brightness(state.getBlock()));
    }

    /** 1.7.10 multipliers: {@code 1 + log10(255 / average texture brightness)}. */
    private static float brightness(Block block) {
        if (block == ModBlocks.CARBON_FLOODLIGHT) return 1.41531F;
        if (block == ModBlocks.ELECTRIC_FLOODLIGHT) return 1.2512F;
        if (block == ModBlocks.UV_FLOODLIGHT) return 1.273F;
        return 1.21342F; // fluorescent, grow and cube lights
    }

    /**
     * The map colour of dye {@code color} (an {@link EnumDyeColor} metadata) times {@code brightness}, each channel
     * capped at 255. {@link FloodlightTileEntity#NO_COLOR} is no tint.
     */
    static int tint(int color, float brightness) {
        if (color < 0 || color > 15) return NO_TINT;
        int rgb = MapColor.getBlockColor(EnumDyeColor.byMetadata(color)).colorValue;
        int r = Math.min(255, Math.round(((rgb >> 16) & 0xFF) * brightness));
        int g = Math.min(255, Math.round(((rgb >> 8) & 0xFF) * brightness));
        int b = Math.min(255, Math.round((rgb & 0xFF) * brightness));
        return r << 16 | g << 8 | b;
    }
}
