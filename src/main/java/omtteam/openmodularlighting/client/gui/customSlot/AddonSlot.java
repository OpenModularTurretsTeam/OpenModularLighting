package omtteam.openmodularlighting.client.gui.customSlot;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import omtteam.openmodularlighting.items.AddonMetaItem;

public class AddonSlot extends Slot {
    @SuppressWarnings("SameParameterValue")
    public AddonSlot(IInventory par1iInventory, int index, int xPos, int yPos) {
        super(par1iInventory, index, xPos, yPos);
    }

    @Override
    public boolean isItemValid(ItemStack par1ItemStack) {
        return par1ItemStack.getItem() instanceof AddonMetaItem;
    }

    @Override
    public int getItemStackLimit(ItemStack stack) {
        return (stack.getItemDamage() == 1 || stack.getItemDamage() == 7 ? 4 : 1);
    }
}
