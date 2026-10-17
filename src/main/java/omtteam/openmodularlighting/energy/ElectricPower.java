package omtteam.openmodularlighting.energy;

import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import team.reborn.energy.api.base.SimpleEnergyStorage;

import java.util.function.IntSupplier;

/**
 * Power of the electric lights (1.7.10 {@code TileEntityFLElectric}): a 50 000 E buffer charged from a battery in the
 * slot. Up to the whole buffer can be inserted per tick (1.7.10 RF storage); nothing can be extracted.
 */
public final class ElectricPower implements LightPower {
    public static final long CAPACITY = 50_000;
    private static final String ENERGY = "energy";

    private final SimpleEnergyStorage storage;
    private final IntSupplier cost;
    private final Runnable onChange;

    /**
     * @param cost     energy per tick (read every tick, so config changes apply at once)
     * @param onChange called when energy was inserted from outside (marks the block entity dirty)
     */
    public ElectricPower(IntSupplier cost, Runnable onChange) {
        this.cost = cost;
        this.onChange = onChange;
        this.storage = new SimpleEnergyStorage(CAPACITY, CAPACITY, 0) {
            @Override
            protected void onFinalCommit() {
                onChange.run();
            }
        };
    }

    public SimpleEnergyStorage storage() {
        return storage;
    }

    /** Energy per tick at the moment. */
    public int cost() {
        return cost.getAsInt();
    }

    /** Takes up to {@code max} out of the buffer for another light of the same cluster (not for cables). */
    public long take(long max) {
        long taken = Math.max(0, Math.min(max, storage.amount));
        if (taken > 0) {
            storage.amount -= taken;
            onChange.run();
        }
        return taken;
    }

    /** Adds energy received from another light of the same cluster; returns what did not fit. */
    public long give(long amount) {
        long accepted = Math.max(0, Math.min(amount, storage.capacity - storage.amount));
        if (accepted > 0) {
            storage.amount += accepted;
            onChange.run();
        }
        return amount - accepted;
    }

    @Override
    public boolean canRun() {
        return storage.amount >= cost.getAsInt();
    }

    @Override
    public void run() {
        storage.amount -= cost.getAsInt();
    }

    @Override
    public boolean isValidItem(ItemStack stack) {
        return Batteries.isBattery(stack);
    }

    @Override
    public void prepare(SimpleContainer inventory, InventoryStorage inventoryStorage) {
        if (!inventory.getItem(0).isEmpty() && storage.amount < storage.capacity) {
            Batteries.discharge(inventoryStorage, 0, storage);
        }
    }

    @Override
    public boolean hasStored() {
        return storage.amount > 0;
    }

    @Override
    public void read(CompoundTag tag) {
        storage.amount = Math.max(0, Math.min(storage.capacity, tag.getLong(ENERGY)));
    }

    @Override
    public void write(CompoundTag tag) {
        tag.putLong(ENERGY, storage.amount);
    }
}
