package omtteam.openmodularlighting.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import omtteam.openmodularlighting.blockentity.UvLightBlockEntity;
import omtteam.openmodularlighting.config.OMLConfig;
import omtteam.openmodularlighting.registry.ModBlockEntities;
import omtteam.openmodularlighting.registry.ModBlocks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
//? if >=1.21 {
import com.mojang.serialization.MapCodec;
//?}

/**
 * The UV light block of the UV floodlight (1.7.10 {@code BlockUVLightBlock}): light level 4 while {@link #LIT}, and it
 * damages living entities inside it (see {@link UvLightBlockEntity}). Otherwise like a phantom light: no shape,
 * replaceable, removed with its last source. Visible only while lit and if {@code uvLightRendered} is on.
 */
public class UvLightBlock extends LightBlock {
    //? if >=1.21 {
    private static final MapCodec<UvLightBlock> CODEC = simpleCodec(UvLightBlock::new);
    //?}

    public UvLightBlock(Properties properties) {
        super(properties);
    }

    public static Properties createProperties() {
        return Properties.of()
                .mapColor(MapColor.NONE)
                .replaceable()
                .noCollission()
                .noOcclusion()
                .instabreak()
                .noLootTable()
                .pushReaction(PushReaction.DESTROY)
                .randomTicks()
                .lightLevel(state -> state.getValue(LIT) ? 4 : 0);
    }

    //? if >=1.21 {
    @Override
    protected @NotNull MapCodec<? extends Block> codec() {
        return CODEC;
    }
    //?}

    /** The UV beam takes over phantom lights (1.7.10 {@code UVSource}). */
    @Override
    public boolean canReplace(BlockState state) {
        return state.is(ModBlocks.PHANTOM_LIGHT);
    }

    /**
     * With neighbour updates: a floodlight beam crossing this block passed it without light, so the phantom lights
     * next to it re-cast their sources and fill the gap.
     */
    @Override
    public int removalFlags() {
        return Block.UPDATE_ALL;
    }

    @Override
    public @NotNull VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    /** {@code uvLightRendered} is the player's own setting: read from the local config, not the server's. */
    @Override
    public @NotNull RenderShape getRenderShape(BlockState state) {
        return state.getValue(LIT) && OMLConfig.local().general.uvLightRendered ? RenderShape.MODEL : RenderShape.INVISIBLE;
    }

    /** Hide faces between UV light blocks, like glass (1.7.10 {@code shouldSideBeRendered}). */
    @Override
    public boolean skipRendering(BlockState state, BlockState adjacent, Direction direction) {
        return adjacent.is(this) && adjacent.getValue(LIT) || super.skipRendering(state, adjacent, direction);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new UvLightBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide() || type != ModBlockEntities.UV_LIGHT) return null;
        return (tickLevel, pos, tickState, blockEntity) ->
                UvLightBlockEntity.serverTick(tickLevel, pos, tickState, (UvLightBlockEntity) blockEntity);
    }
}
