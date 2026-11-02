package omtteam.openmodularlighting.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import omtteam.openmodularlighting.block.LightBlock;
import omtteam.omlib.compat.nbt.NbtCompat;
import omtteam.openmodularlighting.core.light.BeamCaster;
import omtteam.openmodularlighting.core.light.Cell;
import omtteam.openmodularlighting.core.light.ShapeDiff;
import omtteam.openmodularlighting.core.math.Pos;
import omtteam.openmodularlighting.world.LevelLightWorld;
import omtteam.openmodularlighting.world.LightBlocks;
import omtteam.openmodularlighting.world.Positions;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The beam of one light (CLAUDE.md §6.3): the light block positions it placed or joined, cached with the shape key,
 * position and facing they were cast for, so that switching after a reload needs no re-cast.
 */
final class LightBeam {
    private static final String SHAPE = "shape";
    private static final String SHAPE_KEY = "shape_key";
    private static final String SHAPE_ORIGIN = "shape_origin";
    private static final String SHAPE_FACING = "shape_facing";

    private final FloodlightBlockEntity light;
    private List<BlockPos> shape = List.of();
    private Set<BlockPos> shapeSet = Set.of();
    /** What the cached shape was cast for; null if there is no cached shape. */
    private int @Nullable [] shapeKey;
    /**
     * Position and facing the cached shape was cast from; null if unknown (saved before they were recorded), which is
     * taken as matching. A copy of the block entity data elsewhere ({@code /clone}, structures) or a facing changed
     * without a new block entity (debug stick) then re-casts.
     */
    private @Nullable BlockPos castOrigin;
    private @Nullable Direction castFacing;
    /** The cells of the six neighbours after the last cast (not saved: null until the first cast after loading). */
    private Cell @Nullable [] neighbours;

    LightBeam(FloodlightBlockEntity light) {
        this.light = light;
    }

    /** Whether the cached shape is missing or was cast for a different key (mode, config), position or facing. */
    boolean needsRecast() {
        return !Arrays.equals(shapeKey, light.shapeKey())
                || castOrigin != null && !castOrigin.equals(light.getBlockPos())
                || castFacing != null && castFacing != light.facing();
    }

    /**
     * Whether a neighbour of the light is not what it was at the last cast (a block in front placed or removed): the
     * block in front may hold no light block that would notice it (blocked beam, thinning). Neighbour updates that
     * change nothing for the beam (a redstone signal) do not count.
     */
    boolean neighboursChanged(Level level) {
        return !Arrays.equals(neighbours, neighbourCells(level));
    }

    private Cell[] neighbourCells(Level level) {
        LevelLightWorld world = new LevelLightWorld(level);
        Direction[] directions = Direction.values();
        Cell[] cells = new Cell[directions.length];
        for (int i = 0; i < directions.length; i++) {
            cells[i] = world.cellAt(Positions.toPos(light.getBlockPos().relative(directions[i])));
        }
        return cells;
    }

    /** Casts the beam again and updates the light blocks that changed (diff against the cached shape). */
    void recast(Level level) {
        BlockPos origin = light.getBlockPos();
        LightBlock block = light.lightBlock();
        List<Pos> cast = BeamCaster.cast(light.currentShape(), Positions.toPos(origin),
                Positions.toFacing(light.facing()), light.lightType(), new LevelLightWorld(level));
        List<Pos> before = shape.stream().map(Positions::toPos).toList();
        for (Pos removed : ShapeDiff.between(before, cast).removed()) {
            LightBlocks.leave(level, Positions.toBlockPos(removed), block, origin);
        }
        // join every position (not only the added ones): repairs light blocks lost while unloaded, and is a no-op
        // for positions that already have this source
        List<BlockPos> joined = new ArrayList<>();
        for (Pos pos : cast) {
            BlockPos blockPos = Positions.toBlockPos(pos);
            if (LightBlocks.join(level, blockPos, block, origin, light.isLightOn())) {
                joined.add(blockPos);
            }
        }
        setShape(joined);
        shapeKey = light.shapeKey();
        castOrigin = origin;
        castFacing = light.facing();
        neighbours = neighbourCells(level);
    }

    void switchLights(Level level, boolean on) {
        for (BlockPos pos : shape) {
            LightBlocks.setActive(level, pos, light.lightBlock(), light.getBlockPos(), on);
        }
    }

    /** Leaves all light blocks (removing those this light was the last source of). */
    void removeLights(Level level) {
        for (BlockPos pos : shape) {
            LightBlocks.leave(level, pos, light.lightBlock(), light.getBlockPos());
        }
        setShape(List.of());
        shapeKey = null;
        castOrigin = null;
        castFacing = null;
        neighbours = null;
    }

    List<BlockPos> positions() {
        return shape;
    }

    boolean covers(BlockPos pos) {
        return shapeSet.contains(pos);
    }

    void write(CompoundTag tag) {
        if (shapeKey != null) {
            tag.putLongArray(SHAPE, shape.stream().mapToLong(BlockPos::asLong).toArray());
            tag.putIntArray(SHAPE_KEY, shapeKey);
            if (castOrigin != null && castFacing != null) {
                tag.putLong(SHAPE_ORIGIN, castOrigin.asLong());
                tag.putInt(SHAPE_FACING, castFacing.get3DDataValue());
            }
        }
    }

    void read(CompoundTag tag) {
        if (tag.contains(SHAPE_KEY)) {
            setShape(Arrays.stream(NbtCompat.getLongArray(tag, SHAPE)).mapToObj(BlockPos::of).toList());
            shapeKey = NbtCompat.getIntArray(tag, SHAPE_KEY);
            boolean recorded = tag.contains(SHAPE_ORIGIN) && tag.contains(SHAPE_FACING);
            castOrigin = recorded ? BlockPos.of(NbtCompat.getLong(tag, SHAPE_ORIGIN, 0L)) : null;
            castFacing = recorded ? Direction.from3DDataValue(NbtCompat.getInt(tag, SHAPE_FACING, 0)) : null;
        } else {
            setShape(List.of());
            shapeKey = null;
            castOrigin = null;
            castFacing = null;
        }
    }

    private void setShape(List<BlockPos> positions) {
        shape = List.copyOf(positions);
        shapeSet = new HashSet<>(positions);
    }
}
