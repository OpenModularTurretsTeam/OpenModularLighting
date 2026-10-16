package omtteam.openmodularlighting.world;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import omtteam.openmodularlighting.block.LightBlock;
import omtteam.openmodularlighting.blockentity.LightBlockEntity;
import omtteam.openmodularlighting.core.light.LightSources;

/**
 * Server-side operations of lights on their light blocks (see CLAUDE.md §6.3). None of them loads a chunk: positions
 * in unloaded chunks are skipped and fixed later by the light block's random tick.
 */
public final class LightBlocks {
    /**
     * Block update flags for light blocks: tell clients, but no neighbour or shape updates, so switching one light
     * block never triggers re-casts at the next one.
     */
    public static final int FLAGS = Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE;

    private LightBlocks() {
    }

    /**
     * Adds {@code source} to the light block at {@code pos}, placing one if the block is air (or one the light block
     * may replace, see {@link LightBlock#canReplace}; replacing a phantom light makes its sources re-cast).
     *
     * @return whether {@code pos} now has a light block of this type with this source
     */
    public static boolean join(Level level, BlockPos pos, LightBlock block, BlockPos source, boolean on) {
        if (!level.hasChunkAt(pos)) return false;
        BlockState state = level.getBlockState(pos);
        if (!state.is(block)) {
            if (!(state.isAir() || block.canReplace(state))
                    || !level.setBlock(pos, block.defaultBlockState().setValue(LightBlock.LIT, on), FLAGS)) {
                return false;
            }
        }
        LightBlockEntity light = lightAt(level, pos, block);
        if (light == null) return false;
        apply(level, pos, light.sources().add(Positions.toPos(source), on));
        light.setChanged();
        return true;
    }

    /** Removes {@code source} from the light block at {@code pos}; the block is removed if it was the last source. */
    public static void leave(Level level, BlockPos pos, LightBlock block, BlockPos source) {
        LightBlockEntity light = lightAt(level, pos, block);
        if (light == null) return;
        LightSources.Effect effect = light.sources().remove(Positions.toPos(source));
        light.setChanged();
        apply(level, pos, effect);
    }

    /** Switches {@code source} on or off at the light block at {@code pos}. */
    public static void setActive(Level level, BlockPos pos, LightBlock block, BlockPos source, boolean on) {
        LightBlockEntity light = lightAt(level, pos, block);
        if (light == null) return;
        LightSources.Effect effect = light.sources().setActive(Positions.toPos(source), on);
        light.setChanged();
        apply(level, pos, effect);
    }

    /** Carries out an {@link LightSources.Effect} on the light block at {@code pos}. */
    public static void apply(Level level, BlockPos pos, LightSources.Effect effect) {
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof LightBlock)) return;
        switch (effect) {
            case NONE -> {
            }
            case TURN_ON -> setLit(level, pos, state, true);
            case TURN_OFF -> setLit(level, pos, state, false);
            case REMOVE -> level.setBlock(pos, Blocks.AIR.defaultBlockState(), ((LightBlock) state.getBlock()).removalFlags());
        }
    }

    private static void setLit(Level level, BlockPos pos, BlockState state, boolean lit) {
        if (state.getValue(LightBlock.LIT) != lit) {
            level.setBlock(pos, state.setValue(LightBlock.LIT, lit), FLAGS);
        }
    }

    private static LightBlockEntity lightAt(Level level, BlockPos pos, LightBlock block) {
        if (!level.hasChunkAt(pos) || !level.getBlockState(pos).is(block)) return null;
        return level.getBlockEntity(pos) instanceof LightBlockEntity light ? light : null;
    }
}
