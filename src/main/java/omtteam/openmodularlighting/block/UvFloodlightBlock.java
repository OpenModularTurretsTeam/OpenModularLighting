package omtteam.openmodularlighting.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import omtteam.openmodularlighting.blockentity.FloodlightBlockEntity;
import omtteam.openmodularlighting.blockentity.UvFloodlightBlockEntity;
import omtteam.omlib.camo.CamoBlock;
import omtteam.openmodularlighting.registry.ModBlockEntities;
import omtteam.omlib.compat.BlockCodec;

/**
 * UV floodlight (1.7.10 {@code BlockUVLight}): a floodlight block with a straight beam of UV light. No mode.
 */
public class UvFloodlightBlock extends FloodlightBlock implements CamoBlock {
    private static final BlockCodec<UvFloodlightBlock> CODEC = codecOf(UvFloodlightBlock::new);

    public UvFloodlightBlock(Properties properties) {
        super(CODEC, properties);
    }

    /** A disguised floodlight still shows its lamp ({@link #LAMP_TINT_INDEX}). */
    @Override
    public int overlayTintIndex() {
        return LAMP_TINT_INDEX;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new UvFloodlightBlockEntity(pos, state);
    }

    @Override
    protected BlockEntityType<? extends FloodlightBlockEntity> blockEntityType() {
        return ModBlockEntities.UV_FLOODLIGHT;
    }
}
