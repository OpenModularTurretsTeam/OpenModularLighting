package omtteam.openmodularlighting.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.VoxelShape;
import omtteam.openmodularlighting.blockentity.FloodlightBlockEntity;
import omtteam.openmodularlighting.blockentity.FluorescentLightBlockEntity;
import omtteam.openmodularlighting.registry.ModBlockEntities;
//? if >=1.21 {
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.block.BaseEntityBlock;
import org.jetbrains.annotations.NotNull;
//?}

import java.util.Map;

/**
 * Small and square fluorescent lights (1.7.10 {@code BlockSmallElectricFloodlight}, metadata 0 and 1). The small
 * light is a 6 px wide strip that a stick or wrench turns by 90° ({@link #ROTATED}, 1.7.10 {@code rotationState}).
 */
public class FluorescentLightBlock extends PanelLightBlock {
    public static final BooleanProperty ROTATED = BooleanProperty.create("rotated");

    //? if >=1.21 {
    private static final MapCodec<FluorescentLightBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            propertiesCodec(),
            Codec.BOOL.fieldOf("small").forGetter(block -> block.small)
    ).apply(instance, FluorescentLightBlock::new));
    //?}

    private static final Map<Direction, VoxelShape> SQUARE = PanelShapes.of(0, 0, 0, 16, 3, 16);
    private static final Map<Direction, VoxelShape> SMALL = PanelShapes.of(0, 0, 5, 16, 3, 11);
    private static final Map<Direction, VoxelShape> SMALL_ROTATED = PanelShapes.of(5, 0, 0, 11, 3, 16);

    private final boolean small;

    /** @param small the 6 px strip (rotatable) instead of the full square */
    public FluorescentLightBlock(Properties properties, boolean small) {
        super(properties);
        this.small = small;
        registerDefaultState(defaultBlockState().setValue(ROTATED, false));
    }

    //? if >=1.21 {
    @Override
    protected @NotNull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }
    //?}

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(ROTATED);
    }

    @Override
    protected VoxelShape panelShape(BlockState state) {
        Map<Direction, VoxelShape> shapes = !small ? SQUARE : state.getValue(ROTATED) ? SMALL_ROTATED : SMALL;
        return shapes.get(state.getValue(FACING));
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FluorescentLightBlockEntity(pos, state);
    }

    @Override
    protected BlockEntityType<? extends FloodlightBlockEntity> blockEntityType() {
        return ModBlockEntities.FLUORESCENT_LIGHT;
    }

    /** Only the small light can be turned (the square one looks the same either way). */
    @Override
    protected boolean hasConfiguration() {
        return small;
    }

    /** Turns the strip by 90°; without neighbour updates, which would make the beam re-cast. */
    @Override
    protected void configure(Level level, BlockPos pos, BlockState state, Player player, FloodlightBlockEntity light) {
        level.setBlock(pos, state.cycle(ROTATED), Block.UPDATE_CLIENTS);
    }
}
