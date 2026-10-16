package omtteam.openmodularlighting.energy;

import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
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

    private final FuelTimer timer = new FuelTimer();
    private final Supplier<BeamMode> mode;

    /** @param mode the light's current mode, which decides how long a fuel item lasts */
    public FuelPower(Supplier<BeamMode> mode) {
        this.mode = mode;
    }

    /** Furnace burn time of an item in ticks, 0 if it is no fuel. */
    public static int burnTime(ItemStack stack) {
        if (stack.isEmpty()) return 0;
        Integer ticks = FuelRegistry.INSTANCE.get(stack.getItem());
        return ticks == null ? 0 : Math.max(0, ticks);
    }

    public int ticksRemaining() {
        return timer.ticksRemaining();
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
     * Burns the next item as soon as the timer is empty, even while the light is off (1.7.10). The mode at that time
     * decides how long it lasts. Leaves the remainder (lava bucket → bucket) like a furnace.
     */
    @Override
    public void prepare(SimpleContainer inventory, InventoryStorage inventoryStorage) {
        if (!timer.needsFuel()) return;
        ItemStack stack = inventory.getItem(0);
        int burnTime = burnTime(stack);
        if (burnTime <= 0 || !timer.refuel(CarbonFuel.ticks(burnTime, OMLConfig.get().general.carbonTime, mode.get()))) {
            return;
        }
        Item remainder = stack.getItem().getCraftingRemainingItem();
        stack.shrink(1);
        if (stack.isEmpty() && remainder != null) {
            inventory.setItem(0, new ItemStack(remainder));
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
        timer.setTicksRemaining(tag.getInt(TIME_REMAINING));
    }

    @Override
    public void write(CompoundTag tag) {
        tag.putInt(TIME_REMAINING, timer.ticksRemaining());
    }
}
