package omtteam.openmodularlighting.energy;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import omtteam.omlib.compat.nbt.NbtCompat;
import omtteam.openmodularlighting.config.OMLConfig;
import omtteam.openmodularlighting.core.beam.BeamMode;
import omtteam.openmodularlighting.core.formula.CarbonFuel;
import omtteam.openmodularlighting.core.state.FuelTimer;
import omtteam.omlib.platform.Platform;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

/**
 * Power of the carbon floodlight (1.7.10 {@code TileEntityCarbonFloodlight}): burns furnace fuel from the slot.
 */
public final class FuelPower implements LightPower {
    private static final String TIME_REMAINING = "time_remaining";

    private final FuelTimer timer;
    private final Supplier<BeamMode> mode;
    private final Supplier<Level> level;

    /**
     * @param mode  the light's current mode, which decides how fast the burn time is used up
     * @param level the light's level (fuel values), {@code null} while it is not placed
     */
    public FuelPower(Supplier<BeamMode> mode, Supplier<Level> level) {
        this.mode = mode;
        this.level = level;
        this.timer = new FuelTimer(() -> CarbonFuel.drain(mode.get()));
    }

    /** Furnace burn time of an item in ticks, 0 if it is no fuel (or there is no level yet). */
    public static int burnTime(@Nullable Level level, ItemStack stack) {
        return level == null ? 0 : Platform.fuel().burnTime(level, stack);
    }

    /** Stored burn time, in ticks of straight light. */
    public int ticksRemaining() {
        return timer.ticksRemaining();
    }

    /** How long the light can run on the stored burn time in its current mode (shown in the GUI). */
    public int runTicks() {
        return CarbonFuel.runTicks(timer.ticksRemaining(), mode.get());
    }

    @Override
    public boolean canRun() {
        return timer.canRun();
    }

    @Override
    public void run() {
        timer.run();
    }

    @Override
    public boolean isValidItem(ItemStack stack) {
        return burnTime(level.get(), stack) > 0;
    }

    /**
     * Burns the next item as soon as the timer is empty, even while the light is off (1.7.10). Leaves the remainder (lava bucket → bucket) like a furnace.
     */
    @Override
    public void prepare(SimpleContainer inventory) {
        if (!timer.needsFuel()) return;
        ItemStack stack = inventory.getItem(0);
        int burnTime = burnTime(level.get(), stack);
        if (burnTime <= 0 || !timer.refuel(CarbonFuel.ticks(burnTime, OMLConfig.get().general.carbonTime))) {
            return;
        }
        ItemStack remainder = Platform.fuel().remainder(stack);
        stack.shrink(1);
        if (stack.isEmpty() && !remainder.isEmpty()) {
            inventory.setItem(0, remainder);
        } else {
            inventory.setChanged();
        }
    }

    @Override
    public boolean hasStored() {
        return timer.ticksRemaining() > 0;
    }

    @Override
    public void read(CompoundTag tag) {
        timer.setTicksRemaining(NbtCompat.getInt(tag, TIME_REMAINING, 0));
    }

    @Override
    public void write(CompoundTag tag) {
        tag.putInt(TIME_REMAINING, timer.ticksRemaining());
    }
}
