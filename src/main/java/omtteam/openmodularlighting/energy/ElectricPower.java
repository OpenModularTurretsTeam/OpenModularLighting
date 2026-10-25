package omtteam.openmodularlighting.energy;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import omtteam.openmodularlighting.core.energy.EnergyStore;

import java.util.function.IntSupplier;

/**
 * Power of the electric lights (1.7.10 {@code TileEntityFLElectric}): a 50 000 E buffer charged from a battery in the
 * slot. Up to the whole buffer can be inserted per tick (1.7.10 RF storage); nothing can be extracted.
 */
public final class ElectricPower implements LightPower {
    public static final long CAPACITY = 50_000;
    private static final String ENERGY = "energy";

    private final EnergyStore store;
    private final IntSupplier cost;
    /**
     * True while the light pays its own cost. That change skips {@code onChange}, because
     * {@code FloodlightBlockEntity.serverTick} marks the block entity dirty once on every tick the light runs.
     */
    private boolean running;

    /**
     * @param cost     energy per tick (read every tick, so config changes apply at once)
     * @param onChange called when energy came in or went to another light (marks the block entity dirty); not for the
     *                 light's own cost per tick, see {@link #running}
     */
    public ElectricPower(IntSupplier cost, Runnable onChange) {
        this.cost = cost;
        this.store = new EnergyStore(CAPACITY, CAPACITY, 0, () -> {
            if (!running) onChange.run();
        });
    }

    public EnergyStore store() {
        return store;
    }

    /** Energy per tick at the moment. */
    public int cost() {
        return cost.getAsInt();
    }

    @Override
    public boolean canRun() {
        return store.getAmount() >= cost.getAsInt();
    }

    @Override
    public void run() {
        running = true;
        store.consume(cost.getAsInt());
        running = false;
    }

    @Override
    public boolean isValidItem(ItemStack stack) {
        return Batteries.isBattery(stack);
    }

    @Override
    public void prepare(SimpleContainer inventory) {
        if (!inventory.getItem(0).isEmpty() && store.getFreeSpace() > 0) {
            Batteries.discharge(inventory, 0, store);
        }
    }

    @Override
    public boolean hasStored() {
        return store.getAmount() > 0;
    }

    @Override
    public void read(CompoundTag tag) {
        store.setAmountUnchecked(Math.min(store.getCapacity(), tag.getLong(ENERGY)));
    }

    @Override
    public void write(CompoundTag tag) {
        tag.putLong(ENERGY, store.getAmount());
    }
}
