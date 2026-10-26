package omtteam.openmodularlighting.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import omtteam.openmodularlighting.blockentity.CubeLightBlockEntity;
import omtteam.openmodularlighting.blockentity.FloodlightBlockEntity;
import omtteam.openmodularlighting.registry.ModBlockEntities;
import omtteam.omlib.compat.BlockCodec;

/**
 * Cube light: a full block that lights all 6 blocks around it. It looks the same from every side, so {@link #FACING}
 * is fixed (the light has no front).
 */
public class CubeLightBlock extends FloodlightBlock {
    private static final BlockCodec<CubeLightBlock> CODEC = codecOf(CubeLightBlock::new);

    public CubeLightBlock(Properties properties) {
        super(CODEC, properties);
        registerDefaultState(defaultBlockState().setValue(FACING, Direction.DOWN));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState();
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CubeLightBlockEntity(pos, state);
    }

    @Override
    protected BlockEntityType<? extends FloodlightBlockEntity> blockEntityType() {
        return ModBlockEntities.CUBE_LIGHT;
    }
}
