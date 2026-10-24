package omtteam.openmodularlighting.block;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.world.World;
import omtteam.openmodularlighting.blockentity.PhantomLightBlockEntity;

/**
 * The invisible light block of floodlights, fluorescent and grow lights: light level 15 while {@link #LIT}, 0
 * otherwise. Behaves like air for everything else (see CLAUDE.md §6.3).
 * <p>
 * Its material is {@link Material#AIR}, as the 1.7.10 phantom light reported itself as air: falling blocks, trees,
 * pistons and explosions check that material directly. Random ticks still reach it, because 1.12.2 counts them per
 * block, not per material.
 */
public class PhantomLightBlock extends LightBlock {
    public PhantomLightBlock() {
        super(Material.AIR);
    }

    @Override
    protected int litLightLevel() {
        return 15;
    }

    @Override
    @SuppressWarnings("deprecation")
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.INVISIBLE;
    }

    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new PhantomLightBlockEntity();
    }
}
