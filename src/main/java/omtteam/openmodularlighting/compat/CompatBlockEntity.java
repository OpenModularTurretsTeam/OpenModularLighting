package omtteam.openmodularlighting.compat;

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

/**
 * Base class for all mod block entities (tile entities), the 1.12.2 counterpart of the main branch's class of the same
 * name.
 * <p>
 * Subclasses override {@link #readData} / {@link #writeData} instead of the vanilla NBT methods. Everything written by
 * {@link #writeData} is also sent to clients (initial chunk data and {@link #syncToClient()}); override
 * {@link #writeClientData} to send less.
 * <p>
 * Unlike vanilla's, a mod tile entity is recreated by Forge on every blockstate change unless
 * {@link #shouldRefresh} says otherwise: here it is only replaced when the block changes, so switching {@code LIT} keeps
 * the data.
 */
public abstract class CompatBlockEntity extends TileEntity {
    /**
     * Loads state from NBT. Also called on the client for synced data.
     */
    protected abstract void readData(NBTTagCompound tag);

    /**
     * Saves state to NBT.
     */
    protected abstract void writeData(NBTTagCompound tag);

    /**
     * Data sent to clients. Defaults to the full saved data.
     */
    protected void writeClientData(NBTTagCompound tag) {
        writeData(tag);
    }

    /**
     * Marks the block entity dirty and sends its client data to all tracking players.
     */
    public void syncToClient() {
        markDirty();
        if (world != null && !world.isRemote) {
            IBlockState state = world.getBlockState(pos);
            world.notifyBlockUpdate(pos, state, state, 2);
        }
    }

    /** Makes the client redraw the block, e.g. after data the model reads changed (no effect on the server). */
    public void remesh() {
        if (world != null && world.isRemote) {
            world.markBlockRangeForRenderUpdate(pos, pos);
        }
    }

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
        return oldState.getBlock() != newState.getBlock();
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        readData(tag);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        writeData(tag);
        return tag;
    }

    /** Position and id (so the client can create the block entity) plus {@link #writeClientData}. */
    @Override
    public NBTTagCompound getUpdateTag() {
        NBTTagCompound tag = super.writeToNBT(new NBTTagCompound());
        writeClientData(tag);
        return tag;
    }

    @Nullable
    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(pos, 0, getUpdateTag());
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity packet) {
        handleUpdateTag(packet.getNbtCompound());
    }
}
