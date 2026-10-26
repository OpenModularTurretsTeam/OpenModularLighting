package omtteam.openmodularlighting.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import omtteam.omlib.compat.BlockCodec;

/**
 * A light that is a thin panel (3 px) on the face behind it: fluorescent lights and the grow light. No collision
 * (1.7.10 {@code getCollisionBoundingBoxFromPool} returned null), only an outline.
 */
public abstract class PanelLightBlock extends FloodlightBlock {
    protected PanelLightBlock(BlockCodec<?> codec, Properties properties) {
        super(codec, properties);
    }

    /** 1.7.10: rock, metal sound, hardness 2.5. */
    public static Properties createPanelProperties() {
        return Properties.of().mapColor(MapColor.METAL).sound(SoundType.METAL).strength(2.5F)
                .requiresCorrectToolForDrops().noCollission().noOcclusion();
    }

    /** The outline in this state (see {@link PanelShapes}). */
    protected abstract VoxelShape panelShape(BlockState state);

    @Override
    public @NotNull VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return panelShape(state);
    }
}
