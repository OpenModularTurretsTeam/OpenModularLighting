package omtteam.openmodularlighting.blockentity;

import net.minecraft.block.IGrowable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import omtteam.openmodularlighting.config.OMLConfig;
import omtteam.openmodularlighting.core.beam.BeamShape;
import omtteam.openmodularlighting.core.beam.GrowMode;
import omtteam.openmodularlighting.core.formula.GrowChance;
import omtteam.openmodularlighting.core.state.LightController;
import omtteam.openmodularlighting.energy.ElectricPower;
import omtteam.openmodularlighting.energy.LightPower;

/**
 * Grow light (1.7.10 {@code TileEntityGrowLight}): lights the block in front (normal lighting mode) and gives the crop
 * two blocks in front random growth ticks while it runs; {@code energyUsageGrowLight} per tick, energy only from the
 * back.
 */
public class GrowLightBlockEntity extends PanelLightBlockEntity {
    private static final String MODE = "mode";

    private final ElectricPower power = new ElectricPower(
            () -> OMLConfig.get().general.energyUsageGrowLight, this::markDirty);
    private GrowMode mode = GrowMode.LIGHTING;
    /** Game time of the next growth tick; not saved (1.7.10). */
    private long nextGrowTick;

    @Override
    protected LightPower power() {
        return power;
    }

    public GrowMode getMode() {
        return mode;
    }

    public void setMode(GrowMode mode) {
        if (this.mode == mode) return;
        this.mode = mode;
        shapeChanged();
    }

    public void cycleMode() {
        setMode(mode.next());
    }

    @Override
    public String modeKey() {
        return mode == GrowMode.LIGHTING
                ? "gui.openmodularlighting.grow_light_lighting" : "gui.openmodularlighting.grow_light_black_light";
    }

    @Override
    protected BeamShape currentShape() {
        return mode.shape();
    }

    @Override
    protected int[] shapeKey() {
        return new int[]{mode.ordinal()};
    }

    /**
     * While running: when the next growth tick is due, bonemeal the crop two blocks in front if the block between is
     * empty (air; the phantom light is an air block, as in 1.7.10).
     */
    @Override
    protected void afterTick(World world, BlockPos pos, IBlockState state, LightController.TickResult result) {
        if (!result.running() || world.getTotalWorldTime() <= nextGrowTick) return;
        EnumFacing facing = facing();
        BlockPos front = pos.offset(facing);
        BlockPos target = pos.offset(facing, 2);
        IBlockState frontState = world.getBlockState(front);
        IBlockState targetState = world.getBlockState(target);
        // the phantom light is an air block itself
        if (frontState.getBlock().isAir(frontState, world, front) && targetState.getBlock() instanceof IGrowable) {
            IGrowable crop = (IGrowable) targetState.getBlock();
            if (crop.canGrow(world, target, targetState, false)) {
                crop.grow(world, world.rand, target, targetState);
            }
        }
        nextGrowTick = GrowChance.nextGrowTick(world.getTotalWorldTime(), OMLConfig.get().general.chanceGrowLight,
                world.rand);
    }

    @Override
    public boolean hasCustomSettings() {
        return super.hasCustomSettings() || mode != GrowMode.LIGHTING;
    }

    @Override
    protected void writeSettings(NBTTagCompound tag) {
        super.writeSettings(tag);
        tag.setInteger(MODE, mode.ordinal());
    }

    @Override
    protected void readSettings(NBTTagCompound tag) {
        mode = GrowMode.byOrdinal(tag.getInteger(MODE));
        super.readSettings(tag);
    }
}
