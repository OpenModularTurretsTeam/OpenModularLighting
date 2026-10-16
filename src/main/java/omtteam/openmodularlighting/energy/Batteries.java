package omtteam.openmodularlighting.energy;

import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.minecraft.world.item.ItemStack;
import team.reborn.energy.api.EnergyStorage;
import team.reborn.energy.api.EnergyStorageUtil;

/**
 * Battery slots: any item with an extractable Team Reborn Energy storage (1.7.10: RF {@code IEnergyContainerItem}
 * and IC2 electric items).
 */
public final class Batteries {
    private Batteries() {
    }

    public static boolean isBattery(ItemStack stack) {
        if (stack.isEmpty()) return false;
        EnergyStorage storage = EnergyStorage.ITEM.find(stack, ContainerItemContext.withConstant(stack));
        return storage != null && storage.supportsExtraction();
    }

    /** Moves as much energy as possible from the battery in {@code slot} of {@code inventory} into {@code target}. */
    public static void discharge(InventoryStorage inventory, int slot, EnergyStorage target) {
        ContainerItemContext context = ContainerItemContext.ofSingleSlot(inventory.getSlot(slot));
        EnergyStorage battery = EnergyStorage.ITEM.find(context.getItemVariant().toStack(), context);
        if (battery != null) {
            EnergyStorageUtil.move(battery, target, Long.MAX_VALUE, null);
        }
    }
}
