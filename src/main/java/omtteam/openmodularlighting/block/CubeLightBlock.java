package omtteam.openmodularlighting.block;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import omtteam.openmodularlighting.blockentity.CubeLightBlockEntity;
import omtteam.openmodularlighting.blockentity.FloodlightBlockEntity;

/**
 * Cube light: a full block that lights all 6 blocks around it. It looks the same from every side, so {@link #FACING}
 * is fixed (the light has no front).
 */
public class CubeLightBlock extends FloodlightBlock {
    public CubeLightBlock() {
        super(Material.ROCK);
        setDefaultState(getDefaultState().withProperty(FACING, EnumFacing.DOWN));
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY,
                                            float hitZ, int meta, EntityLivingBase placer) {
        return getDefaultState();
    }

    /** No lamp overlay, so no cutout. */
    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.SOLID;
    }

    @Override
    protected FloodlightBlockEntity createLight() {
        return new CubeLightBlockEntity();
    }
}
