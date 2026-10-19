package omtteam.openmodularlighting.client.render;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import omtteam.openmodularlighting.blockentity.FloodlightBlockEntity;
import omtteam.openmodularlighting.registry.ModBlocks;
import org.jetbrains.annotations.Nullable;

/**
 * Tints dyed floodlights (1.7.10 {@code RotatableBlockRenderer}): the dye's map colour, brightened per block so that
 * the dark textures do not get too dark.
 */
public final class FloodlightTint {
    private static final int NO_TINT = 0xFFFFFF;

    private FloodlightTint() {
    }

    /**
     * The block colour of a light ({@code BlockColor}; the loaders register it for {@link #tintedBlocks()}).
     */
    public static int color(BlockState state, @Nullable BlockAndTintGetter level, @Nullable BlockPos pos, int tintIndex) {
        if (level == null || pos == null || !(level.getBlockEntity(pos) instanceof FloodlightBlockEntity light)) {
            return NO_TINT;
        }
        return tint(light.getColor(), brightness(state.getBlock()));
    }

    /** The blocks {@link #color} applies to. */
    public static Block[] tintedBlocks() {
        return ModBlocks.LIGHT_SOURCES.toArray(Block[]::new);
    }

    /** 1.7.10 multipliers: {@code 1 + log10(255 / average texture brightness)}. */
    private static float brightness(Block block) {
        if (block == ModBlocks.CARBON_FLOODLIGHT) return 1.41531F;
        if (block == ModBlocks.ELECTRIC_FLOODLIGHT) return 1.2512F;
        if (block == ModBlocks.UV_FLOODLIGHT) return 1.273F;
        return 1.21342F; // fluorescent and grow lights
    }

    /**
     * The dye colour of {@code color} (a {@link DyeColor} id) times {@code brightness}, each channel capped at 255.
     * {@link FloodlightBlockEntity#NO_COLOR} is no tint.
     */
    static int tint(int color, float brightness) {
        if (color < 0 || color > 15) return NO_TINT;
        int rgb = DyeColor.byId(color).getMapColor().col;
        int r = Math.min(255, Math.round(((rgb >> 16) & 0xFF) * brightness));
        int g = Math.min(255, Math.round(((rgb >> 8) & 0xFF) * brightness));
        int b = Math.min(255, Math.round((rgb & 0xFF) * brightness));
        return r << 16 | g << 8 | b;
    }
}
