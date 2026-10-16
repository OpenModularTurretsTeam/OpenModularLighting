package omtteam.openmodularlighting.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import omtteam.openmodularlighting.registry.ModBlockEntities;

/**
 * Block entity of the phantom light: its floodlight sources (see {@link LightBlockEntity}).
 */
public class PhantomLightBlockEntity extends LightBlockEntity {
    public PhantomLightBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.PHANTOM_LIGHT, pos, state);
    }
}
