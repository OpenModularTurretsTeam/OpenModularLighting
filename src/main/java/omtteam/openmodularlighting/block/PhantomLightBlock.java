package omtteam.openmodularlighting.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import omtteam.openmodularlighting.blockentity.PhantomLightBlockEntity;
import org.jetbrains.annotations.NotNull;
//? if >=1.21 {
import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.Block;
//?}

/**
 * The invisible light block of floodlights, fluorescent and grow lights: light level 15 while {@link #LIT}, 0
 * otherwise. Behaves like air for everything else (no shape, replaceable, destroyed by pistons, does not block
 * spawning while dark); see CLAUDE.md §6.3.
 */
public class PhantomLightBlock extends LightBlock {
    //? if >=1.21 {
    private static final MapCodec<PhantomLightBlock> CODEC = simpleCodec(PhantomLightBlock::new);
    //?}

    public PhantomLightBlock(Properties properties) {
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
                .lightLevel(state -> state.getValue(LIT) ? 15 : 0);
    }

    //? if >=1.21 {
    @Override
    protected @NotNull MapCodec<? extends Block> codec() {
        return CODEC;
    }
    //?}

    @Override
    public @NotNull VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    public @NotNull RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PhantomLightBlockEntity(pos, state);
    }
}
