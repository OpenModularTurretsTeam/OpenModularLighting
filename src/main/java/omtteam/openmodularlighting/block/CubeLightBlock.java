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
//? if >=1.21 {
import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.BaseEntityBlock;
import org.jetbrains.annotations.NotNull;
//?}

/**
 * Cube light: a full block that lights all 6 blocks around it. It looks the same from every side, so {@link #FACING}
 * is fixed (the light has no front).
 */
public class CubeLightBlock extends FloodlightBlock {
    //? if >=1.21 {
    private static final MapCodec<CubeLightBlock> CODEC = simpleCodec(CubeLightBlock::new);
    //?}

    public CubeLightBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(FACING, Direction.DOWN));
    }

    //? if >=1.21 {
    @Override
    protected @NotNull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }
    //?}

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
