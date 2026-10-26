package omtteam.openmodularlighting.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;
import omtteam.openmodularlighting.blockentity.FloodlightBlockEntity;
import omtteam.openmodularlighting.blockentity.GrowLightBlockEntity;
import omtteam.openmodularlighting.registry.ModBlockEntities;

import java.util.Map;
import omtteam.omlib.compat.BlockCodec;

/**
 * Grow light (1.7.10 {@code BlockGrowLight}): a square panel that always hangs from the ceiling, shining down; a
 * stick or wrench switches between normal lighting and black light.
 */
public class GrowLightBlock extends PanelLightBlock {
    private static final BlockCodec<GrowLightBlock> CODEC = codecOf(GrowLightBlock::new);

    private static final Map<Direction, VoxelShape> SHAPES = PanelShapes.of(0, 0, 0, 16, 3, 16);

    public GrowLightBlock(Properties properties) {
        super(CODEC, properties);
        registerDefaultState(defaultBlockState().setValue(FACING, Direction.DOWN));
    }

    /** 1.7.10 {@code onBlockPlacedBy}: always facing down. */
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, Direction.DOWN);
    }

    @Override
    protected VoxelShape panelShape(BlockState state) {
        return SHAPES.get(state.getValue(FACING));
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new GrowLightBlockEntity(pos, state);
    }

    @Override
    protected BlockEntityType<? extends FloodlightBlockEntity> blockEntityType() {
        return ModBlockEntities.GROW_LIGHT;
    }

    @Override
    protected boolean hasConfiguration() {
        return true;
    }

    @Override
    protected void configure(Level level, BlockPos pos, BlockState state, Player player, FloodlightBlockEntity light) {
        if (light instanceof GrowLightBlockEntity grow) {
            grow.cycleMode();
            sendMode(player, grow.modeKey());
        }
    }
}
