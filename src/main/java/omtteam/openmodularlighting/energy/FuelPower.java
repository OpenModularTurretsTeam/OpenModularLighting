package omtteam.openmodularlighting.energy;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraftforge.items.ItemStackHandler;
import omtteam.openmodularlighting.config.OMLConfig;
import omtteam.openmodularlighting.core.beam.BeamMode;
import omtteam.openmodularlighting.core.formula.CarbonFuel;
import omtteam.openmodularlighting.core.state.FuelTimer;

import java.util.function.Supplier;

/**
 * Power of the carbon floodlight (1.7.10 {@code TileEntityCarbonFloodlight}): burns furnace fuel from the slot.
 */
public final class FuelPower implements LightPower {
    private static final String TIME_REMAINING = "time_remaining";

    private final FuelTimer timer;
    private final Supplier<BeamMode> mode;

    /** @param mode the light's current mode, which decides how fast the burn time is used up */
    public FuelPower(Supplier<BeamMode> mode) {
        this.mode = mode;
        this.timer = new FuelTimer(() -> CarbonFuel.drain(mode.get()));
    }

    /** Furnace burn time of an item in ticks (vanilla and modded fuels), 0 if it is no fuel. */
    public static int burnTime(ItemStack stack) {
        return stack.isEmpty() ? 0 : Math.max(0, TileEntityFurnace.getItemBurnTime(stack));
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
        return burnTime(stack) > 0;
    }

    /**
     * Burns the next item as soon as the timer is empty, even while the light is off (1.7.10). Leaves the remainder (lava bucket → bucket) like a furnace.
     */
    @Override
    public void prepare(ItemStackHandler inventory) {
        if (!timer.needsFuel()) return;
        ItemStack stack = inventory.getStackInSlot(0);
        int burnTime = burnTime(stack);
        if (burnTime <= 0 || !timer.refuel(CarbonFuel.ticks(burnTime, OMLConfig.get().general.carbonTime))) {
            return;
        }
        ItemStack remainder = stack.getItem().getContainerItem(stack);
        ItemStack rest = stack.copy();
        rest.shrink(1);
        inventory.setStackInSlot(0, rest.isEmpty() ? remainder : rest);
    }

    @Override
    public boolean hasStored() {
        return timer.ticksRemaining() > 0;
    }

    @Override
    public void read(NBTTagCompound tag) {
        timer.setTicksRemaining(tag.getInteger(TIME_REMAINING));
    }

    @Override
    public void write(NBTTagCompound tag) {
        tag.setInteger(TIME_REMAINING, timer.ticksRemaining());
    }
}
