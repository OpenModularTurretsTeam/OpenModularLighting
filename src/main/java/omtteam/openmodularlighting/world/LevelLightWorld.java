package omtteam.openmodularlighting.world;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LightEngine;
import omtteam.omlib.compat.block.BlockCompat;
import omtteam.openmodularlighting.core.light.Cell;
import omtteam.openmodularlighting.core.light.LightWorld;
import omtteam.openmodularlighting.core.math.Pos;
import omtteam.openmodularlighting.registry.ModBlocks;

/**
 * The core {@link LightWorld} on top of a {@link Level}. Never loads a chunk.
 */
public record LevelLightWorld(Level level) implements LightWorld {
    @Override
    public Cell cellAt(Pos pos) {
        BlockPos blockPos = Positions.toBlockPos(pos);
        if (!level.hasChunkAt(blockPos)) return Cell.UNLOADED;
        BlockState state = level.getBlockState(blockPos);
        if (state.isAir()) return Cell.AIR;
        if (state.is(ModBlocks.PHANTOM_LIGHT)) return Cell.PHANTOM_LIGHT;
        if (state.is(ModBlocks.UV_LIGHT)) return Cell.UV_LIGHT;
        // solid blocks, and blocks that let no light through even though they are see-through (tinted glass)
        if (BlockCompat.isSolidRender(state, level, blockPos) || BlockCompat.lightBlock(state, level, blockPos) >= LightEngine.MAX_LEVEL) {
            return Cell.OPAQUE;
        }
        return Cell.TRANSPARENT;
    }
}
