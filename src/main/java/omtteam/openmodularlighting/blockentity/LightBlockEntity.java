package omtteam.openmodularlighting.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import omtteam.openmodularlighting.compat.CompatBlockEntity;
import omtteam.openmodularlighting.core.light.LightSources;
import omtteam.openmodularlighting.core.light.LightSources.SourceStatus;
import omtteam.openmodularlighting.core.math.Pos;
import omtteam.openmodularlighting.world.Positions;

import java.util.HashSet;
import java.util.Set;

/**
 * Block entity of a light block (phantom or UV light): the lights that reach it and which of them are on
 * ({@link LightSources}). It does not tick; the block refreshes it on random ticks.
 * <p>
 * Nothing is synced to clients: the on/off state is the {@code LIT} blockstate.
 */
public abstract class LightBlockEntity extends CompatBlockEntity {
    private static final String SOURCES = "sources";
    private static final String ACTIVE_SOURCES = "active_sources";

    private LightSources sources = new LightSources();

    protected LightBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    /** The sources of this block, for reading; change them through the methods below, which save only real changes. */
    public LightSources sources() {
        return sources;
    }

    public LightSources.Effect addSource(Pos source, boolean on) {
        long before = sources.changeCount();
        return savedIfChanged(before, sources.add(source, on));
    }

    public LightSources.Effect removeSource(Pos source) {
        long before = sources.changeCount();
        return savedIfChanged(before, sources.remove(source));
    }

    public LightSources.Effect setSourceActive(Pos source, boolean on) {
        long before = sources.changeCount();
        return savedIfChanged(before, sources.setActive(source, on));
    }

    /**
     * Marks the block entity dirty only if the sources changed since {@code before}: re-casts join every position again
     * and random ticks refresh every light block, and most of these calls change nothing.
     */
    private LightSources.Effect savedIfChanged(long before, LightSources.Effect effect) {
        if (sources.changeCount() != before) {
            setChanged();
        }
        return effect;
    }

    /** Flags every source in a loaded chunk for a re-cast (this block was replaced or a neighbour changed). */
    public void requestRecastOfSources() {
        if (level == null) return;
        for (Pos source : sources.sources()) {
            if (sourceAt(level, Positions.toBlockPos(source)) instanceof LightSourceBlockEntity light) {
                light.requestRecast();
            }
        }
    }

    /**
     * Random-tick cleanup: drops sources that no longer exist or no longer cover this block, and updates the on/off
     * state of the others. Sources in unloaded chunks are kept as they are.
     */
    public LightSources.Effect refresh() {
        if (level == null) return LightSources.Effect.NONE;
        Level world = level;
        long before = sources.changeCount();
        LightSources.Effect effect = sources.refresh(source -> {
            BlockPos sourcePos = Positions.toBlockPos(source);
            if (!world.hasChunkAt(sourcePos)) return SourceStatus.UNKNOWN;
            if (!(sourceAt(world, sourcePos) instanceof LightSourceBlockEntity light)
                    || !light.coversLightAt(worldPosition)) {
                return SourceStatus.GONE;
            }
            return light.isLightOn() ? SourceStatus.ON : SourceStatus.OFF;
        });
        return savedIfChanged(before, effect);
    }

    /** The block entity at {@code pos}, without loading its chunk. */
    private static BlockEntity sourceAt(Level level, BlockPos pos) {
        return level.hasChunkAt(pos) ? level.getBlockEntity(pos) : null;
    }

    @Override
    protected void readData(CompoundTag tag, HolderLookup.Provider registries) {
        LightSources loaded = new LightSources();
        Set<Long> active = new HashSet<>();
        for (long pos : tag.getLongArray(ACTIVE_SOURCES)) {
            active.add(pos);
        }
        for (long pos : tag.getLongArray(SOURCES)) {
            loaded.add(Positions.toPos(BlockPos.of(pos)), active.contains(pos));
        }
        sources = loaded;
    }

    @Override
    protected void writeData(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putLongArray(SOURCES, sources.sources().stream().mapToLong(LightBlockEntity::asLong).toArray());
        tag.putLongArray(ACTIVE_SOURCES, sources.activeSources().stream().mapToLong(LightBlockEntity::asLong).toArray());
    }

    @Override
    protected void writeClientData(CompoundTag tag, HolderLookup.Provider registries) {
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return null;
    }

    private static long asLong(Pos pos) {
        return Positions.toBlockPos(pos).asLong();
    }
}
