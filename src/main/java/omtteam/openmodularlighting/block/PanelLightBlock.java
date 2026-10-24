package omtteam.openmodularlighting.block;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import javax.annotation.Nullable;

/**
 * A light that is a thin panel (3 px) on the face behind it: fluorescent lights and the grow light. No collision
 * (1.7.10 {@code getCollisionBoundingBoxFromPool} returned null), only an outline.
 */
public abstract class PanelLightBlock extends FloodlightBlock {
    /** 1.7.10: metal, metal sound, hardness 2.5. */
    protected PanelLightBlock() {
        super(Material.IRON);
        setLightOpacity(0);
    }

    /** The outline in this state (see {@link PanelShapes}); {@code state} is the actual state. */
    protected abstract AxisAlignedBB panelShape(IBlockState state);

    @Override
    @SuppressWarnings("deprecation")
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        return panelShape(state.getActualState(world, pos));
    }

    @Nullable
    @Override
    @SuppressWarnings("deprecation")
    public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        return NULL_AABB;
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    @SuppressWarnings("deprecation")
    public BlockFaceShape getBlockFaceShape(IBlockAccess world, IBlockState state, BlockPos pos, EnumFacing face) {
        return BlockFaceShape.UNDEFINED;
    }

    @Override
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.CUTOUT;
    }
}
