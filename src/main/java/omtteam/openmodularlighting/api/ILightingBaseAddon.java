package omtteam.openmodularlighting.api;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import omtteam.openmodularlighting.tileentity.LightingBase;
import omtteam.openmodularlighting.util.LightAddonUtil;

/**
 * Created by Keridos on 17/05/17.
 * This Class
 */
public interface ILightingBaseAddon {
    /**
     * This should give the correct bounding box for the block based on its BlockState.
     * It is used for example for the block building preview.
     *
     * @param blockState the blockstate of the block
     * @param world      the World of the block
     * @param pos        the BlockPos of the block in the world
     * @return the correct bounding box for the block, based on its blockstate and pos.
     */
    AxisAlignedBB getBoundingBoxFromState(IBlockState blockState, World world, BlockPos pos);

    /**
     * This should give the correct bounding box for the block based on its Facing.
     * It is used for example for the block building preview.
     *
     * @param facing the facing of the block
     * @param world  the World of the block
     * @param pos    the BlockPos of the block in the world
     * @return the correct bounding box for the block, based on its blockstate and pos.
     */
    AxisAlignedBB getBoundingBoxFromFacing(EnumFacing facing, World world, BlockPos pos);

    /**
     * This should give back the base that this addon block belongs to.
     *
     * @param world the World of the block
     * @param pos   the BlockPos of the block in the world
     * @return the corresponding base.
     */
    default LightingBase getBase(World world, BlockPos pos) {
        return LightAddonUtil.getBase(world, pos);
    }
}
