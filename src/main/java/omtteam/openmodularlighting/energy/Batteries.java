package omtteam.openmodularlighting.energy;

import net.minecraft.item.ItemStack;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.ItemStackHandler;
import omtteam.omlib.core.energy.EnergyStore;

import javax.annotation.Nullable;

/**
 * Battery slots: any item with extractable Forge Energy (1.7.10: RF {@code IEnergyContainerItem} and IC2 electric
 * items).
 */
public final class Batteries {
    private Batteries() {
    }

    public static boolean isBattery(ItemStack stack) {
        IEnergyStorage energy = energyOf(stack);
        return energy != null && energy.canExtract();
    }

    /**
     * Moves as much energy as possible from the battery in {@code slot} of {@code inventory} into {@code target} (via
     * {@link EnergyStore#insert}, so its {@code maxInsert} applies). The item's energy is changed in place.
     *
     * @return the amount moved
     */
    public static long discharge(ItemStackHandler inventory, int slot, EnergyStore target) {
        IEnergyStorage battery = energyOf(inventory.getStackInSlot(slot));
        if (battery == null || !battery.canExtract()) return 0;
        int room = (int) Math.min(Integer.MAX_VALUE, target.insert(Long.MAX_VALUE, true));
        int available = battery.extractEnergy(room, true);
        if (available <= 0) return 0;
        int extracted = battery.extractEnergy(available, false);
        return target.insert(extracted, false);
    }

    @Nullable
    private static IEnergyStorage energyOf(ItemStack stack) {
        if (stack.isEmpty() || !stack.hasCapability(CapabilityEnergy.ENERGY, null)) return null;
        return stack.getCapability(CapabilityEnergy.ENERGY, null);
    }
}
