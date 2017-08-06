package omtteam.openmodularlighting.client.gui.containers;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import omtteam.omlib.util.DebugHandler;
import omtteam.omlib.util.compat.ItemStackTools;
import omtteam.openmodularlighting.client.gui.customSlot.AddonSlot;
import omtteam.openmodularlighting.client.gui.customSlot.UpgradeSlot;
import omtteam.openmodularlighting.handler.NetworkingHandler;
import omtteam.openmodularlighting.items.AddonMetaItem;
import omtteam.openmodularlighting.items.UpgradeMetaItem;
import omtteam.openmodularlighting.network.messages.MessageLightingBase;
import omtteam.openmodularlighting.tileentity.LightingBase;

import javax.annotation.ParametersAreNonnullByDefault;

import static omtteam.omlib.util.InvUtil.mergeItemStackWithStackLimit;
import static omtteam.omlib.util.compat.ItemStackTools.getStackSize;

/**
 * Created by Keridos on 09/12/2015.
 * This Class
 */
public abstract class LightingBaseContainer extends Container {
    LightingBase tileEntity;

    @Override
    @ParametersAreNonnullByDefault
    public boolean canInteractWith(EntityPlayer player) {
        return tileEntity.isUsable(player);
    }

    @SuppressWarnings("ConstantConditions")
    @ParametersAreNonnullByDefault
    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int slot) {
        ItemStack stack = ItemStackTools.getEmptyStack();
        Slot slotObject = inventorySlots.get(slot);

        // null checks and checks if the item can be stacked (maxStackSize > 1)
        if (slotObject != null && slotObject.getHasStack()) {
            ItemStack stackInSlot = slotObject.getStack();
            stack = ItemStackTools.safeCopy(stackInSlot);

            // Actual slot start index of turret base
            int slotStart = 36;

            int addonSlotStart = 100, addonSlotEnd = 0, upgSlotStart = 100, upgSlotEnd = 0;
            // Determine the slot range for each type( According to the class constructor )
            for (int i = slotStart; i < this.inventorySlots.size(); i++) {
                Class slotClass = this.getSlot(i).getClass();
                if (slotClass == AddonSlot.class) {
                    addonSlotStart = Math.min(i, addonSlotStart);
                    addonSlotEnd = Math.max(i + 1, addonSlotEnd);
                } else if (slotClass == UpgradeSlot.class) {
                    upgSlotStart = Math.min(i, upgSlotStart);
                    upgSlotEnd = Math.max(i + 1, upgSlotEnd);
                }
            }

            // Transfer from player inventory
            if (slot < slotStart) {
                // Priority addon and upgrade slot first
                if (stackInSlot.getItem() instanceof AddonMetaItem) {
                    if (!mergeItemStackWithStackLimit(stackInSlot, addonSlotStart, addonSlotEnd, false, this)) {
                        return ItemStackTools.getEmptyStack();
                    }
                } else if (stackInSlot.getItem() instanceof UpgradeMetaItem) {
                    if (!mergeItemStackWithStackLimit(stackInSlot, upgSlotStart, upgSlotEnd, false, this)) {
                        return ItemStackTools.getEmptyStack();
                    }
                } else {
                    if (!mergeItemStackWithStackLimit(stackInSlot, slotStart, slotStart + 9, false, this)) {
                        return ItemStackTools.getEmptyStack();
                    }
                }
            } else // Transfer from turret base inventory
            {
                if (!mergeItemStackWithStackLimit(stackInSlot, 0, slotStart, false, this)) {
                    return ItemStackTools.getEmptyStack();
                }
            }

            if (getStackSize(stackInSlot) == 0) {
                slotObject.putStack(ItemStackTools.getEmptyStack());
            } else {
                slotObject.onSlotChanged();
            }

            if (getStackSize(stackInSlot) == getStackSize(stack)) {
                return ItemStackTools.getEmptyStack();
            }
            slotObject.onSlotChanged();
        }
        return stack;
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        DebugHandler.getInstance().setListeners(this.listeners);
        for (IContainerListener listener : this.listeners) {
            if (listener instanceof EntityPlayerMP) {
                NetworkingHandler.INSTANCE.sendTo(new MessageLightingBase(this.tileEntity), (EntityPlayerMP) listener);
            }
        }
    }
}
