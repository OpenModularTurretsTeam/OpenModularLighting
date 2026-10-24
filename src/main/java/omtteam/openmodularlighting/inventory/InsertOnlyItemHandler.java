package omtteam.openmodularlighting.inventory;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nonnull;

/**
 * Slots of a light as other blocks see them (hoppers, pipes): anything the slots accept goes in
 * ({@link IItemHandler#isItemValid}), nothing comes out (1.7.10).
 */
public final class InsertOnlyItemHandler implements IItemHandler {
    private final IItemHandlerModifiable slots;

    public InsertOnlyItemHandler(IItemHandlerModifiable slots) {
        this.slots = slots;
    }

    @Override
    public int getSlots() {
        return slots.getSlots();
    }

    @Nonnull
    @Override
    public ItemStack getStackInSlot(int slot) {
        return slots.getStackInSlot(slot);
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        if (!slots.isItemValid(slot, stack)) return stack;
        return slots.insertItem(slot, stack, simulate);
    }

    @Nonnull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        return ItemStack.EMPTY;
    }

    @Override
    public int getSlotLimit(int slot) {
        return slots.getSlotLimit(slot);
    }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
        return slots.isItemValid(slot, stack);
    }
}
