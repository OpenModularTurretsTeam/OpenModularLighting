package omtteam.openmodularlighting.world;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import omtteam.openmodularlighting.core.light.Cell;
import omtteam.openmodularlighting.core.light.LightWorld;
import omtteam.openmodularlighting.core.math.Pos;
import omtteam.openmodularlighting.registry.ModBlocks;

/**
 * The core {@link LightWorld} on top of a {@link World}. Never loads a chunk.
 */
public final class LevelLightWorld implements LightWorld {
    private static final int MAX_LIGHT = 15;

    private final World world;

    public LevelLightWorld(World world) {
        this.world = world;
    }

    @Override
    public Cell cellAt(Pos pos) {
        BlockPos blockPos = Positions.toBlockPos(pos);
        // outside the build height counts as unloaded: nothing can be placed there
        if (world.isOutsideBuildHeight(blockPos) || !world.isBlockLoaded(blockPos)) return Cell.UNLOADED;
        IBlockState state = world.getBlockState(blockPos);
        Block block = state.getBlock();
        if (block == ModBlocks.PHANTOM_LIGHT) return Cell.PHANTOM_LIGHT;
        if (block == ModBlocks.UV_LIGHT) return Cell.UV_LIGHT;
        if (LightBlocks.isAir(state)) return Cell.AIR;
        // solid blocks, and blocks that let no light through even though they are see-through (lightproof glass);
        // leaves are opaque cubes in 1.12 with fast graphics (always on a dedicated server), but see-through as on
        // 1.20.1+
        boolean opaque = state.isOpaqueCube() && state.getMaterial() != Material.LEAVES;
        if (opaque || state.getLightOpacity(world, blockPos) >= MAX_LIGHT) {
            return Cell.OPAQUE;
        }
        return Cell.TRANSPARENT;
    }
}
