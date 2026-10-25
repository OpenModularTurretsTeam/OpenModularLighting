package omtteam.openmodularlighting.tileentity;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import omtteam.openmodularlighting.compat.CompatTileEntity;
import omtteam.openmodularlighting.core.light.LightSources;
import omtteam.openmodularlighting.core.light.LightSources.SourceStatus;
import omtteam.openmodularlighting.core.math.Pos;
import omtteam.openmodularlighting.world.PosArrays;
import omtteam.openmodularlighting.world.Positions;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Block entity of a light block (phantom or UV light): the lights that reach it and which of them are on
 * ({@link LightSources}). The phantom light's does not tick; the block refreshes it on random ticks.
 * <p>
 * Nothing is synced to clients: the on/off state is the {@code LIT} blockstate.
 */
public abstract class LightTileEntity extends CompatTileEntity {
    private static final String SOURCES = "sources";
    private static final String ACTIVE_SOURCES = "active_sources";

    private LightSources sources = new LightSources();

    /** The sources of this block. Call {@link #markDirty()} after changing them. */
    public LightSources sources() {
        return sources;
    }

    /** Flags every source in a loaded chunk for a re-cast (this block was replaced or a neighbour changed). */
    public void requestRecastOfSources() {
        if (world == null) return;
        for (Pos source : sources.sources()) {
            TileEntity tile = sourceAt(world, Positions.toBlockPos(source));
            if (tile instanceof LightSourceTileEntity) {
                ((LightSourceTileEntity) tile).requestRecast();
            }
        }
    }

    /**
     * Random-tick cleanup: drops sources that no longer exist or no longer cover this block, and updates the on/off
     * state of the others. Sources in unloaded chunks are kept as they are.
     */
    public LightSources.Effect refresh() {
        if (world == null) return LightSources.Effect.NONE;
        World level = world;
        LightSources.Effect effect = sources.refresh(source -> {
            BlockPos sourcePos = Positions.toBlockPos(source);
            if (!level.isBlockLoaded(sourcePos)) return SourceStatus.UNKNOWN;
            TileEntity tile = sourceAt(level, sourcePos);
            if (!(tile instanceof LightSourceTileEntity) || !((LightSourceTileEntity) tile).coversLightAt(pos)) {
                return SourceStatus.GONE;
            }
            return ((LightSourceTileEntity) tile).isLightOn() ? SourceStatus.ON : SourceStatus.OFF;
        });
        markDirty();
        return effect;
    }

    /** The block entity at {@code pos}, without loading its chunk. */
    @Nullable
    private static TileEntity sourceAt(World world, BlockPos pos) {
        return world.isBlockLoaded(pos) ? world.getTileEntity(pos) : null;
    }

    @Override
    protected void readData(NBTTagCompound tag) {
        LightSources loaded = new LightSources();
        Set<BlockPos> active = new HashSet<>(PosArrays.read(tag.getIntArray(ACTIVE_SOURCES)));
        for (BlockPos source : PosArrays.read(tag.getIntArray(SOURCES))) {
            loaded.add(Positions.toPos(source), active.contains(source));
        }
        sources = loaded;
    }

    @Override
    protected void writeData(NBTTagCompound tag) {
        tag.setIntArray(SOURCES, PosArrays.write(toBlockPositions(sources.sources())));
        tag.setIntArray(ACTIVE_SOURCES, PosArrays.write(toBlockPositions(sources.activeSources())));
    }

    @Override
    protected void writeClientData(NBTTagCompound tag) {
    }

    @Nullable
    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        return null;
    }

    private static List<BlockPos> toBlockPositions(Set<Pos> positions) {
        List<BlockPos> result = new ArrayList<>(positions.size());
        for (Pos pos : positions) {
            result.add(Positions.toBlockPos(pos));
        }
        return result;
    }
}
