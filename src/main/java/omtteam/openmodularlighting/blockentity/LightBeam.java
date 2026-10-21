package omtteam.openmodularlighting.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import omtteam.openmodularlighting.block.LightBlock;
import omtteam.openmodularlighting.core.light.BeamCaster;
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
 * The beam of one light (CLAUDE.md §6.3): the light block positions it placed or joined, cached with the shape key
 * they were cast for, so that switching after a reload needs no re-cast.
 */
final class LightBeam {
    private static final String SHAPE = "shape";
    private static final String SHAPE_KEY = "shape_key";

    private final FloodlightBlockEntity light;
    private List<BlockPos> shape = List.of();
    private Set<BlockPos> shapeSet = Set.of();
    /** What the cached shape was cast for; null if there is no cached shape. */
    private int @Nullable [] shapeKey;

    LightBeam(FloodlightBlockEntity light) {
        this.light = light;
    }

    /** Whether the cached shape is missing or was cast for a different key (mode, config). */
    boolean needsRecast() {
        return !Arrays.equals(shapeKey, light.shapeKey());
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
        }
    }

    void read(CompoundTag tag) {
        if (tag.contains(SHAPE_KEY)) {
            setShape(Arrays.stream(tag.getLongArray(SHAPE)).mapToObj(BlockPos::of).toList());
            shapeKey = tag.getIntArray(SHAPE_KEY);
        } else {
            setShape(List.of());
            shapeKey = null;
        }
    }

    private void setShape(List<BlockPos> positions) {
        shape = List.copyOf(positions);
        shapeSet = new HashSet<>(positions);
    }
}
