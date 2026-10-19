package omtteam.openmodularlighting.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import omtteam.openmodularlighting.blockentity.FloodlightBlockEntity;
import omtteam.openmodularlighting.blockentity.UvFloodlightBlockEntity;
import omtteam.openmodularlighting.camo.CamoBlock;
import omtteam.openmodularlighting.registry.ModBlockEntities;
//? if >=1.21 {
import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.BaseEntityBlock;
import org.jetbrains.annotations.NotNull;
//?}

/**
 * UV floodlight (1.7.10 {@code BlockUVLight}): a floodlight block with a straight beam of UV light. No mode.
 */
public class UvFloodlightBlock extends FloodlightBlock implements CamoBlock {
    //? if >=1.21 {
    private static final MapCodec<UvFloodlightBlock> CODEC = simpleCodec(UvFloodlightBlock::new);
    //?}

    public UvFloodlightBlock(Properties properties) {
        super(properties);
    }

    //? if >=1.21 {
    @Override
    protected @NotNull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }
    //?}

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new UvFloodlightBlockEntity(pos, state);
    }

    @Override
    protected BlockEntityType<? extends FloodlightBlockEntity> blockEntityType() {
        return ModBlockEntities.UV_FLOODLIGHT;
    }
}
