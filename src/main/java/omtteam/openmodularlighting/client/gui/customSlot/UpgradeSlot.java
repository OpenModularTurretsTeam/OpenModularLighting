package omtteam.openmodularlighting.client.gui.customSlot;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import omtteam.openmodularlighting.items.UpgradeMetaItem;


public class UpgradeSlot extends Slot {
    @SuppressWarnings("SameParameterValue")
    public UpgradeSlot(IInventory par1iInventory, int index, int xPos, int yPos) {
        super(par1iInventory, index, xPos, yPos);
    }

    @Override
    public boolean isItemValid(ItemStack par1ItemStack) {
        return par1ItemStack.getItem() instanceof UpgradeMetaItem;
    }

    @Override
    public int getItemStackLimit(ItemStack stack) {
        return (4);
    }

    @Override
    public int getSlotStackLimit() {
        return 4;
    }
}
