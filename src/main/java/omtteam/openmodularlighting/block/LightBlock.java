package omtteam.openmodularlighting.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import omtteam.openmodularlighting.blockentity.LightBlockEntity;
import omtteam.openmodularlighting.compat.CompatBlockEntity;
import omtteam.openmodularlighting.world.LightBlocks;

/**
 * A block placed by a light along its beam (phantom light, UV light). It stays while one of its sources exists and is
 * switched on and off with {@link #LIT} (see CLAUDE.md §6.3); {@link LightBlocks} places, switches and removes it.
 */
public abstract class LightBlock extends Block implements EntityBlock {
    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    protected LightBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(LIT, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LIT);
    }

    /** Whether this light block may be placed in place of {@code state} (besides air). */
    public boolean canReplace(BlockState state) {
        return false;
    }

    /** Block update flags used when this block removes itself (no sources left). */
    public int removalFlags() {
        return LightBlocks.FLAGS;
    }

    /** Drops sources that are gone, removes the block if none are left and fixes a stale {@code LIT}. */
    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (level.getBlockEntity(pos) instanceof LightBlockEntity light) {
            LightBlocks.apply(level, pos, light.refresh());
        }
    }

    /** A block next to the beam changed: the beam may now reach further or be blocked earlier. */
    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighbourBlock, BlockPos neighbourPos,
                                boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighbourBlock, neighbourPos, movedByPiston);
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof LightBlockEntity light) {
            light.requestRecastOfSources();
        }
    }

    //? if <1.21.11 {
    /** Replaced: {@link LightBlockEntity#onBlockRemoved} flags the sources for a re-cast. */
    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        CompatBlockEntity.handleRemove(state, level, pos, newState);
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
    //?}
}
