package omtteam.openmodularlighting.world;

import net.minecraft.block.BlockAir;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import omtteam.openmodularlighting.block.LightBlock;
import omtteam.openmodularlighting.core.light.LightSources;
import omtteam.openmodularlighting.tileentity.LightTileEntity;

import javax.annotation.Nullable;

/**
 * Server-side operations of lights on their light blocks (see CLAUDE.md §6.3). None of them loads a chunk: positions
 * in unloaded chunks are skipped and fixed later by the light block's random tick.
 */
public final class LightBlocks {
    /** Send the change to clients. */
    public static final int UPDATE_CLIENTS = 2;
    /** Notify neighbours (flag 1) and send the change to clients. */
    public static final int UPDATE_ALL = 3;
    /** No observer updates. */
    private static final int NO_OBSERVERS = 16;
    /**
     * Block update flags for light blocks: tell clients, but no neighbour or observer updates, so switching one light
     * block never triggers re-casts at the next one.
     */
    public static final int FLAGS = UPDATE_CLIENTS | NO_OBSERVERS;

    private LightBlocks() {
    }

    /**
     * Whether the block is real air (1.20.1+ {@code isAir}). Not {@code Block.isAir}: in 1.12 every block with
     * {@code Material.AIR} counts as air there, including the phantom light and other mods' blocks, which a beam
     * must not replace.
     */
    public static boolean isAir(IBlockState state) {
        return state.getBlock() instanceof BlockAir;
    }

    /**
     * Adds {@code source} to the light block at {@code pos}, placing one if the block is air (or one the light block
     * may replace, see {@link LightBlock#canReplace}; replacing a phantom light makes its sources re-cast).
     *
     * @return whether {@code pos} now has a light block of this type with this source
     */
    public static boolean join(World world, BlockPos pos, LightBlock block, BlockPos source, boolean on) {
        if (!world.isBlockLoaded(pos)) return false;
        IBlockState state = world.getBlockState(pos);
        if (state.getBlock() != block) {
            if (!(isAir(state) || block.canReplace(state))
                    || !world.setBlockState(pos, block.getDefaultState().withProperty(LightBlock.LIT, on), FLAGS)) {
                return false;
            }
        }
        LightTileEntity light = lightAt(world, pos, block);
        if (light == null) return false;
        apply(world, pos, light.addSource(Positions.toPos(source), on));
        return true;
    }

    /** Removes {@code source} from the light block at {@code pos}; the block is removed if it was the last source. */
    public static void leave(World world, BlockPos pos, LightBlock block, BlockPos source) {
        LightTileEntity light = lightAt(world, pos, block);
        if (light == null) return;
        apply(world, pos, light.removeSource(Positions.toPos(source)));
    }

    /** Switches {@code source} on or off at the light block at {@code pos}. */
    public static void setActive(World world, BlockPos pos, LightBlock block, BlockPos source, boolean on) {
        LightTileEntity light = lightAt(world, pos, block);
        if (light == null) return;
        apply(world, pos, light.setSourceActive(Positions.toPos(source), on));
    }

    /** Carries out an {@link LightSources.Effect} on the light block at {@code pos}. */
    public static void apply(World world, BlockPos pos, LightSources.Effect effect) {
        IBlockState state = world.getBlockState(pos);
        if (!(state.getBlock() instanceof LightBlock)) return;
        switch (effect) {
            case TURN_ON:
                setLit(world, pos, state, true);
                break;
            case TURN_OFF:
                setLit(world, pos, state, false);
                break;
            case REMOVE:
                world.setBlockState(pos, Blocks.AIR.getDefaultState(), ((LightBlock) state.getBlock()).removalFlags());
                break;
            default:
                break;
        }
    }

    private static void setLit(World world, BlockPos pos, IBlockState state, boolean lit) {
        if (state.getValue(LightBlock.LIT) != lit) {
            world.setBlockState(pos, state.withProperty(LightBlock.LIT, lit), FLAGS);
        }
    }

    @Nullable
    private static LightTileEntity lightAt(World world, BlockPos pos, LightBlock block) {
        if (!world.isBlockLoaded(pos) || world.getBlockState(pos).getBlock() != block) return null;
        TileEntity tile = world.getTileEntity(pos);
        return tile instanceof LightTileEntity ? (LightTileEntity) tile : null;
    }
}
