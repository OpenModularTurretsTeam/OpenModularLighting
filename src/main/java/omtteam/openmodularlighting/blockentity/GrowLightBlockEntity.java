package omtteam.openmodularlighting.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;
import omtteam.openmodularlighting.compat.VersionCompat;
import omtteam.openmodularlighting.config.OMLConfig;
import omtteam.openmodularlighting.core.beam.BeamShape;
import omtteam.openmodularlighting.core.beam.GrowMode;
import omtteam.openmodularlighting.core.formula.GrowChance;
import omtteam.openmodularlighting.core.state.LightController;
import omtteam.openmodularlighting.energy.ElectricPower;
import omtteam.openmodularlighting.energy.LightPower;
import omtteam.openmodularlighting.registry.ModBlockEntities;
import omtteam.openmodularlighting.registry.ModBlocks;

/**
 * Grow light (1.7.10 {@code TileEntityGrowLight}): lights the block in front (normal lighting mode) and gives the crop
 * two blocks in front random growth ticks while it runs; {@code energyUsageGrowLight} per tick, energy only from the
 * back.
 */
public class GrowLightBlockEntity extends PanelLightBlockEntity {
    private static final String MODE = "mode";

    private final ElectricPower power = new ElectricPower(
            () -> OMLConfig.get().general.energyUsageGrowLight, this::setChanged);
    private GrowMode mode = GrowMode.LIGHTING;
    /** Game time of the next growth tick; not saved (1.7.10). */
    private long nextGrowTick;

    public GrowLightBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.GROW_LIGHT, pos, state);
    }

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
     * empty (air or a phantom light, which counted as air in 1.7.10).
     */
    @Override
    protected void afterTick(Level level, BlockPos pos, BlockState state, LightController.TickResult result) {
        if (!result.running() || level.getGameTime() <= nextGrowTick || !(level instanceof ServerLevel server)) return;
        Direction facing = facing();
        BlockPos front = pos.relative(facing);
        BlockPos target = pos.relative(facing, 2);
        BlockState frontState = level.getBlockState(front);
        BlockState targetState = level.getBlockState(target);
        if ((frontState.isAir() || frontState.is(ModBlocks.PHANTOM_LIGHT))
                && targetState.getBlock() instanceof BonemealableBlock crop
                && VersionCompat.isValidBonemealTarget(crop, level, target, targetState)) {
            crop.performBonemeal(server, level.random, target, targetState);
        }
        nextGrowTick = GrowChance.nextGrowTick(level.getGameTime(), OMLConfig.get().general.chanceGrowLight,
                () -> level.random.nextLong());
    }

    @Override
    public boolean hasCustomSettings() {
        return super.hasCustomSettings() || mode != GrowMode.LIGHTING;
    }

    @Override
    protected void writeSettings(CompoundTag tag) {
        super.writeSettings(tag);
        tag.putInt(MODE, mode.ordinal());
    }

    @Override
    protected void readSettings(CompoundTag tag) {
        mode = GrowMode.byOrdinal(tag.getInt(MODE));
        super.readSettings(tag);
    }
}
