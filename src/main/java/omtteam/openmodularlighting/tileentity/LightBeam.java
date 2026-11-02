package omtteam.openmodularlighting.tileentity;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import omtteam.openmodularlighting.block.LightBlock;
import omtteam.openmodularlighting.core.light.BeamCaster;
import omtteam.openmodularlighting.core.light.Cell;
import omtteam.openmodularlighting.core.light.ShapeDiff;
import omtteam.openmodularlighting.core.math.Pos;
import omtteam.openmodularlighting.world.LevelLightWorld;
import omtteam.openmodularlighting.world.LightBlocks;
import omtteam.openmodularlighting.world.PosArrays;
import omtteam.openmodularlighting.world.Positions;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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

    private final FloodlightTileEntity light;
    private List<BlockPos> shape = Collections.emptyList();
    private Set<BlockPos> shapeSet = Collections.emptySet();
    /** What the cached shape was cast for; null if there is no cached shape. */
    @Nullable
    private int[] shapeKey;
    /**
     * Position and facing the cached shape was cast from; null if unknown (saved before they were recorded), which is
     * taken as matching. A copy of the tile entity data elsewhere ({@code /clone}, structures) or a facing changed
     * without a new tile entity then re-casts.
     */
    @Nullable
    private BlockPos castOrigin;
    @Nullable
    private EnumFacing castFacing;
    /** The cells of the six neighbours after the last cast (not saved: null until the first cast after loading). */
    @Nullable
    private Cell[] neighbours;

    LightBeam(FloodlightTileEntity light) {
        this.light = light;
    }

    /** Whether the cached shape is missing or was cast for a different key (mode, config), position or facing. */
    boolean needsRecast() {
        return !Arrays.equals(shapeKey, light.shapeKey())
                || castOrigin != null && !castOrigin.equals(light.getPos())
                || castFacing != null && castFacing != light.facing();
    }

    /**
     * Whether a neighbour of the light is not what it was at the last cast (a block in front placed or removed): the
     * block in front may hold no light block that would notice it (blocked beam, thinning). Neighbour updates that
     * change nothing for the beam (a redstone signal) do not count.
     */
    boolean neighboursChanged(World world) {
        return !Arrays.equals(neighbours, neighbourCells(world));
    }

    private Cell[] neighbourCells(World world) {
        LevelLightWorld lightWorld = new LevelLightWorld(world);
        EnumFacing[] directions = EnumFacing.values();
        Cell[] cells = new Cell[directions.length];
        for (int i = 0; i < directions.length; i++) {
            cells[i] = lightWorld.cellAt(Positions.toPos(light.getPos().offset(directions[i])));
        }
        return cells;
    }

    /** Casts the beam again and updates the light blocks that changed (diff against the cached shape). */
    void recast(World world) {
        BlockPos origin = light.getPos();
        LightBlock block = light.lightBlock();
        List<Pos> cast = BeamCaster.cast(light.currentShape(), Positions.toPos(origin),
                Positions.toFacing(light.facing()), light.lightType(), new LevelLightWorld(world));
        List<Pos> before = new ArrayList<>(shape.size());
        for (BlockPos pos : shape) {
            before.add(Positions.toPos(pos));
        }
        for (Pos removed : ShapeDiff.between(before, cast).removed()) {
            LightBlocks.leave(world, Positions.toBlockPos(removed), block, origin);
        }
        // join every position (not only the added ones): repairs light blocks lost while unloaded, and is a no-op
        // for positions that already have this source
        List<BlockPos> joined = new ArrayList<>();
        for (Pos pos : cast) {
            BlockPos blockPos = Positions.toBlockPos(pos);
            if (LightBlocks.join(world, blockPos, block, origin, light.isLightOn())) {
                joined.add(blockPos);
            }
        }
        setShape(joined);
        shapeKey = light.shapeKey();
        castOrigin = origin;
        castFacing = light.facing();
        neighbours = neighbourCells(world);
    }

    void switchLights(World world, boolean on) {
        for (BlockPos pos : shape) {
            LightBlocks.setActive(world, pos, light.lightBlock(), light.getPos(), on);
        }
    }

    /** Leaves all light blocks (removing those this light was the last source of). */
    void removeLights(World world) {
        for (BlockPos pos : shape) {
            LightBlocks.leave(world, pos, light.lightBlock(), light.getPos());
        }
        setShape(Collections.emptyList());
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

    void write(NBTTagCompound tag) {
        if (shapeKey != null) {
            tag.setIntArray(SHAPE, PosArrays.write(shape));
            tag.setIntArray(SHAPE_KEY, shapeKey);
            if (castOrigin != null && castFacing != null) {
                tag.setIntArray(SHAPE_ORIGIN, new int[]{castOrigin.getX(), castOrigin.getY(), castOrigin.getZ()});
                tag.setInteger(SHAPE_FACING, castFacing.getIndex());
            }
        }
    }

    void read(NBTTagCompound tag) {
        if (tag.hasKey(SHAPE_KEY)) {
            setShape(PosArrays.read(tag.getIntArray(SHAPE)));
            shapeKey = tag.getIntArray(SHAPE_KEY);
            int[] origin = tag.getIntArray(SHAPE_ORIGIN);
            boolean recorded = origin.length == 3 && tag.hasKey(SHAPE_FACING);
            castOrigin = recorded ? new BlockPos(origin[0], origin[1], origin[2]) : null;
            castFacing = recorded ? EnumFacing.byIndex(tag.getInteger(SHAPE_FACING)) : null;
        } else {
            setShape(Collections.emptyList());
            shapeKey = null;
            castOrigin = null;
            castFacing = null;
        }
    }

    private void setShape(List<BlockPos> positions) {
        shape = Collections.unmodifiableList(new ArrayList<>(positions));
        shapeSet = new HashSet<>(positions);
    }
}
