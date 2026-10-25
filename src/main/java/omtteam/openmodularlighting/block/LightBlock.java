package omtteam.openmodularlighting.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import omtteam.openmodularlighting.tileentity.LightTileEntity;
import omtteam.openmodularlighting.world.LightBlocks;

import javax.annotation.Nullable;
import java.util.Random;

/**
 * A block placed by a light along its beam (phantom light, UV light). It stays while one of its sources exists and is
 * switched on and off with {@link #LIT} (see CLAUDE.md §6.3); {@link LightBlocks} places, switches and removes it.
 * No shape, no drops, replaceable, destroyed by pistons, instantly broken; metadata is {@code LIT}.
 */
public abstract class LightBlock extends Block {
    public static final PropertyBool LIT = PropertyBool.create("lit");

    protected LightBlock(Material material) {
        super(material);
        setHardness(0.0F);
        setLightOpacity(0);
        setTickRandomly(true);
        setDefaultState(blockState.getBaseState().withProperty(LIT, false));
    }

    /** Light level while lit. */
    protected abstract int litLightLevel();

    /** Whether this light block may be placed in place of {@code state} (besides air). */
    public boolean canReplace(IBlockState state) {
        return false;
    }

    /** Block update flags used when this block removes itself (no sources left). */
    public int removalFlags() {
        return LightBlocks.FLAGS;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, LIT);
    }

    @Override
    @SuppressWarnings("deprecation")
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(LIT, meta != 0);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(LIT) ? 1 : 0;
    }

    @Override
    @SuppressWarnings("deprecation")
    public int getLightValue(IBlockState state) {
        return state.getValue(LIT) ? litLightLevel() : 0;
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Override
    public abstract TileEntity createTileEntity(World world, IBlockState state);

    /** Drops sources that are gone, removes the block if none are left and fixes a stale {@code LIT}. */
    @Override
    public void randomTick(World world, BlockPos pos, IBlockState state, Random random) {
        if (world.isRemote) return;
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof LightTileEntity) {
            LightBlocks.apply(world, pos, ((LightTileEntity) tile).refresh());
        }
    }

    /** Random ticks only ({@link #randomTick}); no scheduled ticks. */
    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random random) {
    }

    /** A block next to the beam changed: the beam may now reach further or be blocked earlier. */
    @Override
    @SuppressWarnings("deprecation")
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block neighbourBlock,
                                BlockPos neighbourPos) {
        if (world.isRemote) return;
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof LightTileEntity) {
            ((LightTileEntity) tile).requestRecastOfSources();
        }
    }

    /** Replaced (by a player, fluid, piston, …): the sources have to re-cast around the new block. */
    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        if (!world.isRemote) {
            TileEntity tile = world.getTileEntity(pos);
            if (tile instanceof LightTileEntity) {
                ((LightTileEntity) tile).requestRecastOfSources();
            }
        }
        super.breakBlock(world, pos, state);
    }

    @Nullable
    @Override
    @SuppressWarnings("deprecation")
    public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        return NULL_AABB;
    }

    /** No outline: the player looks and clicks through it. */
    @Override
    public boolean canCollideCheck(IBlockState state, boolean hitIfLiquid) {
        return false;
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
    public boolean isReplaceable(IBlockAccess world, BlockPos pos) {
        return true;
    }

    @Override
    @SuppressWarnings("deprecation")
    public EnumPushReaction getPushReaction(IBlockState state) {
        return EnumPushReaction.DESTROY;
    }

    @Override
    @SuppressWarnings("deprecation")
    public BlockFaceShape getBlockFaceShape(IBlockAccess world, IBlockState state, BlockPos pos, EnumFacing face) {
        return BlockFaceShape.UNDEFINED;
    }

    @Override
    public Item getItemDropped(IBlockState state, Random random, int fortune) {
        return Items.AIR;
    }

    @Override
    public int quantityDropped(Random random) {
        return 0;
    }
}
