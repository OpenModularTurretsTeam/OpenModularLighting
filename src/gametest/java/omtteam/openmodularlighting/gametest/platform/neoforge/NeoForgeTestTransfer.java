package omtteam.openmodularlighting.gametest.platform.neoforge;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import omtteam.openmodularlighting.gametest.TestTransfer;

/**
 * NeoForge block capabilities ({@code IEnergyStorage}, {@code IItemHandler}; the variant for 1.21.1).
 */
public final class NeoForgeTestTransfer implements TestTransfer {
    @Override
    public boolean hasEnergy(Level level, BlockPos pos, Direction side) {
        return level.getCapability(Capabilities.EnergyStorage.BLOCK, pos, side) != null;
    }

    @Override
    public long insertEnergy(Level level, BlockPos pos, Direction side, long amount) {
        IEnergyStorage storage = level.getCapability(Capabilities.EnergyStorage.BLOCK, pos, side);
        return storage == null ? 0 : storage.receiveEnergy((int) Math.min(Integer.MAX_VALUE, amount), false);
    }

    @Override
    public long insertItem(Level level, BlockPos pos, Direction side, ItemStack stack) {
        IItemHandler handler = level.getCapability(Capabilities.ItemHandler.BLOCK, pos, side);
        if (handler == null || stack.isEmpty()) return 0;
        return stack.getCount() - ItemHandlerHelper.insertItem(handler, stack.copy(), false).getCount();
    }

    @Override
    public long extractItem(Level level, BlockPos pos, Direction side, ItemStack like, long amount) {
        IItemHandler handler = level.getCapability(Capabilities.ItemHandler.BLOCK, pos, side);
        if (handler == null || like.isEmpty()) return 0;
        long extracted = 0;
        for (int slot = 0; slot < handler.getSlots() && extracted < amount; slot++) {
            if (ItemStack.isSameItemSameComponents(handler.getStackInSlot(slot), like)) {
                extracted += handler.extractItem(slot, (int) Math.min(Integer.MAX_VALUE, amount - extracted), false).getCount();
            }
        }
        return extracted;
    }
}
