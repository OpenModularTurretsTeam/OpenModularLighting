package omtteam.openmodularlighting.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import omtteam.omlib.api.IHasItemBlock;
import omtteam.omlib.util.MathUtil;
import omtteam.openmodularlighting.OpenModularLighting;
import omtteam.openmodularlighting.init.ModBlocks;
import omtteam.openmodularlighting.items.blocks.ItemBlockFloodlight;
import omtteam.openmodularlighting.reference.OMLNames;
import omtteam.openmodularlighting.reference.Reference;
import omtteam.openmodularlighting.tileentity.LightingBase;
import omtteam.openmodularlighting.tileentity.lights.Floodlight;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

import static omtteam.omlib.util.WorldUtil.getTouchingTileEntities;

/**
 * Created by Keridos on 19/07/16.
 * This Class
 */
@SuppressWarnings("deprecation")
public class BlockFloodlight extends BlockLightingBaseAddon implements IHasItemBlock {
    public static final PropertyInteger TIER = PropertyInteger.create("tier", 0, 4);

    public static final PropertyBool ACTIVE = PropertyBool.create("active");

    public BlockFloodlight() {
        super(Material.GLASS);
        this.setCreativeTab(OpenModularLighting.creativeTab);
        this.setResistance(5.0F);
        this.setHardness(5.0F);
        this.setSoundType(SoundType.STONE);
        this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.DOWN));
        this.setUnlocalizedName(OMLNames.Blocks.floodlight);
        this.setRegistryName(Reference.MOD_ID, OMLNames.Blocks.floodlight);
    }

    @Override
    public ItemBlock getItemBlock(Block block) {
        return new ItemBlockFloodlight(block);
    }

    @Override
    @SuppressWarnings("unchecked")
    @Nonnull
    public IBlockState getStateFromMeta(int meta) {
        return meta < 8 ? this.getDefaultState().withProperty(TIER, meta).withProperty(ACTIVE, false) :
                this.getDefaultState().withProperty(TIER, meta - 8).withProperty(ACTIVE, true);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(TIER) + (state.getValue(ACTIVE) ? 8 : 0);
    }

    @Override
    @Nonnull
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, TIER, ACTIVE, FACING);
    }

    @Override
    @Nonnull
    @ParametersAreNonnullByDefault
    public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        Floodlight te = ((Floodlight) worldIn.getTileEntity(pos));
        if (te != null) {
            return state.withProperty(FACING, te.getOrientation());
        } else return state.withProperty(FACING, EnumFacing.NORTH);
    }

    @Override
    @Nonnull
    @ParametersAreNonnullByDefault
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new Floodlight(state.getValue(TIER) + 1);
    }

    @Override
    @Nonnull
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        IBlockState blockState = this.getActualState(state, source, pos);
        EnumFacing facing = blockState.getValue(FACING);
        return getBoundingBoxFromFacing(facing);
    }

    @Override
    public AxisAlignedBB getBoundingBoxFromState(IBlockState blockState, World world, BlockPos pos) {
        EnumFacing facing = blockState.getValue(FACING);
        return getBoundingBoxFromFacing(facing).offset(pos);
    }

    public static AxisAlignedBB getBoundingBoxFromFacing(EnumFacing facing) {
        AxisAlignedBB alignedBB = MathUtil.rotateAABB(new AxisAlignedBB(-3 / 8F, -3 / 8F, -3 / 16F, 3 / 8F, 3 / 8F, 3 / 16F), facing.getOpposite());
        double[] offset = new double[3];
        offset[0] = 0.5D + facing.getFrontOffsetX() * 0.325D;
        offset[1] = 0.5D + facing.getFrontOffsetY() * 0.325D;
        offset[2] = 0.5D + facing.getFrontOffsetZ() * 0.325D;
        return alignedBB.offset(offset[0], offset[1], offset[2]);
    }

    @Override
    public boolean isFullBlock(IBlockState state) {
        return false;
    }

    @Override
    public boolean isNormalCube(IBlockState state, IBlockAccess world, BlockPos pos) {
        return false;
    }

    @Override
    @ParametersAreNonnullByDefault
    public boolean isBlockSolid(IBlockAccess worldIn, BlockPos pos, EnumFacing side) {
        return false;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    @ParametersAreNonnullByDefault
    public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
        for (TileEntity tileEntity : getTouchingTileEntities(worldIn, pos)) {
            if (tileEntity instanceof LightingBase) return true;
        }
        return false;
    }

    @Override
    @ParametersAreNonnullByDefault
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        if (!worldIn.isRemote) {
            dropItems(worldIn, pos);
            super.breakBlock(worldIn, pos, state);
        }
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        omtteam.openmodularlighting.tileentity.lights.Floodlight floodlight = (omtteam.openmodularlighting.tileentity.lights.Floodlight) worldIn.getTileEntity(pos);
        if (floodlight != null) {
            floodlight.setOwnerName(floodlight.getBase().getOwnerName());
            floodlight.setOwner(floodlight.getBase().getOwner());
            floodlight.setSide();
        }
    }

    @Override
    public int damageDropped(IBlockState state) {
        return state.getValue(TIER);
    }

    @Override
    @SideOnly(Side.CLIENT)
    @SuppressWarnings("unchecked")
    @ParametersAreNonnullByDefault
    public void clGetSubBlocks(Item item, CreativeTabs tab, List subItems) {
        for (int i = 0; i < 5; i++) {
            subItems.add(new ItemStack(ModBlocks.floodlight, 1, i));
        }
    }
}
