package omtteam.openmodularlighting.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import omtteam.omlib.compatibility.minecraft.CompatBlock;
import omtteam.openmodularlighting.reference.OMLNames;
import omtteam.openmodularlighting.reference.Reference;
import omtteam.openmodularlighting.tileentity.LightingBase;
import omtteam.openmodularlighting.tileentity.lights.PhantomLight;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Created by Keridos on 03/08/17.
 * This Class
 */
public class BlockPhantomLight extends CompatBlock {

    public BlockPhantomLight() {
        super(Material.AIR);
        this.setUnlocalizedName(OMLNames.Blocks.phantomLight);
        this.setRegistryName(Reference.MOD_ID, OMLNames.Blocks.phantomLight);
    }

    @Override
    @Nonnull
    @ParametersAreNonnullByDefault
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new PhantomLight();
    }

    @Override
    public boolean isFullBlock(IBlockState state) {
        return false;
    }

    @Override
    public int getLightValue(IBlockState state) {
        return 15;
    }

    @Override
    public boolean isOpaqueCube(IBlockState blockState) {
        return false;
    }


    @Override
    protected void clOnNeighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn) {
        if (!worldIn.isRemote) {
            LightingBase base = (LightingBase) worldIn.getTileEntity(pos);
            if (base != null && worldIn.isBlockIndirectlyGettingPowered(pos) > 0) {
                base.setRedstone(true);
            } else if (base != null && worldIn.isBlockIndirectlyGettingPowered(pos) == 0) {
                base.setRedstone(false);
            }
        }
    }


    @Override
    @ParametersAreNonnullByDefault
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        if (!worldIn.isRemote) {
            worldIn.removeTileEntity(pos);
        }
    }
}
