package omtteam.openmodularlighting.gametest.platform.neoforge;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.energy.IEnergyStorage;
import omtteam.openmodularlighting.gametest.TestBatteryItem;
import omtteam.openmodularlighting.gametest.TestItems;

/**
 * FE view of a {@link TestBatteryItem}; changes the stack directly (item capabilities work on the stack in the slot).
 */
final class NeoForgeTestBattery implements IEnergyStorage {
    private final ItemStack stack;

    NeoForgeTestBattery(ItemStack stack) {
        this.stack = stack;
    }

    @Override
    public int receiveEnergy(int toReceive, boolean simulate) {
        int received = (int) Math.min(Math.max(0, toReceive), TestBatteryItem.CAPACITY - stored());
        if (!simulate && received > 0) TestItems.TEST_BATTERY.setStoredEnergy(stack, stored() + received);
        return received;
    }

    @Override
    public int extractEnergy(int toExtract, boolean simulate) {
        int extracted = (int) Math.min(Math.max(0, toExtract), stored());
        if (!simulate && extracted > 0) TestItems.TEST_BATTERY.setStoredEnergy(stack, stored() - extracted);
        return extracted;
    }

    @Override
    public int getEnergyStored() {
        return (int) stored();
    }

    @Override
    public int getMaxEnergyStored() {
        return (int) TestBatteryItem.CAPACITY;
    }

    @Override
    public boolean canExtract() {
        return true;
    }

    @Override
    public boolean canReceive() {
        return true;
    }

    private long stored() {
        return TestItems.TEST_BATTERY.getStoredEnergy(stack);
    }
}
