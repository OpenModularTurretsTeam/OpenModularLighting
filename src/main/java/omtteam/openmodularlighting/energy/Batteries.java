package omtteam.openmodularlighting.energy;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import omtteam.openmodularlighting.core.energy.EnergyStore;
import omtteam.openmodularlighting.platform.Platform;

/**
 * Battery slots: any item with extractable energy in the loader's energy API ({@code platform.Energy}; 1.7.10: RF
 * {@code IEnergyContainerItem} and IC2 electric items).
 */
public final class Batteries {
    private Batteries() {
    }

    public static boolean isBattery(ItemStack stack) {
        return !stack.isEmpty() && Platform.energy().isBattery(stack);
    }

    /** Moves as much energy as possible from the battery in {@code slot} of {@code inventory} into {@code target}. */
    public static void discharge(Container inventory, int slot, EnergyStore target) {
        Platform.energy().dischargeItem(inventory, slot, target, Long.MAX_VALUE);
    }
}
