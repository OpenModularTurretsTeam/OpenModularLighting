package omtteam.openmodularlighting.gametest.platform.fabric;

import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.world.item.ItemStack;
import omtteam.openmodularlighting.gametest.TestBatteryItem;
import omtteam.openmodularlighting.gametest.TestItems;
import team.reborn.energy.api.EnergyStorage;

/**
 * Team Reborn Energy view of a {@link TestBatteryItem}: changes exchange the item in its context (slot) for one with
 * the new energy, so they take part in the transaction.
 */
final class FabricTestBatteryStorage implements EnergyStorage {
    private final ContainerItemContext context;

    FabricTestBatteryStorage(ContainerItemContext context) {
        this.context = context;
    }

    @Override
    public long insert(long maxAmount, TransactionContext transaction) {
        StoragePreconditions.notNegative(maxAmount);
        long inserted = Math.min(maxAmount, TestBatteryItem.CAPACITY - getAmount());
        return inserted > 0 && exchange(getAmount() + inserted, transaction) ? inserted : 0;
    }

    @Override
    public long extract(long maxAmount, TransactionContext transaction) {
        StoragePreconditions.notNegative(maxAmount);
        long extracted = Math.min(maxAmount, getAmount());
        return extracted > 0 && exchange(getAmount() - extracted, transaction) ? extracted : 0;
    }

    @Override
    public long getAmount() {
        return TestItems.TEST_BATTERY.getStoredEnergy(context.getItemVariant().toStack());
    }

    @Override
    public long getCapacity() {
        return TestBatteryItem.CAPACITY;
    }

    private boolean exchange(long energy, TransactionContext transaction) {
        ItemStack changed = context.getItemVariant().toStack();
        TestItems.TEST_BATTERY.setStoredEnergy(changed, energy);
        return context.exchange(ItemVariant.of(changed), 1, transaction) == 1;
    }
}
