package omtteam.openmodularlighting.gametest.platform.forge;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import omtteam.openmodularlighting.gametest.TestTransfer;
import org.jetbrains.annotations.Nullable;

/**
 * Forge block entity capabilities.
 */
public final class ForgeTestTransfer implements TestTransfer {
    @Override
    public boolean hasEnergy(Level level, BlockPos pos, Direction side) {
        return capability(level, pos, side, ForgeCapabilities.ENERGY) != null;
    }

    @Override
    public long insertEnergy(Level level, BlockPos pos, Direction side, long amount) {
        IEnergyStorage storage = capability(level, pos, side, ForgeCapabilities.ENERGY);
        return storage == null ? 0 : storage.receiveEnergy((int) Math.min(Integer.MAX_VALUE, amount), false);
    }

    @Override
    public long insertItem(Level level, BlockPos pos, Direction side, ItemStack stack) {
        IItemHandler handler = capability(level, pos, side, ForgeCapabilities.ITEM_HANDLER);
        if (handler == null || stack.isEmpty()) return 0;
        return stack.getCount() - ItemHandlerHelper.insertItem(handler, stack.copy(), false).getCount();
    }

    @Override
    public long extractItem(Level level, BlockPos pos, Direction side, ItemStack like, long amount) {
        IItemHandler handler = capability(level, pos, side, ForgeCapabilities.ITEM_HANDLER);
        if (handler == null || like.isEmpty()) return 0;
        long extracted = 0;
        for (int slot = 0; slot < handler.getSlots() && extracted < amount; slot++) {
            if (ItemStack.isSameItemSameTags(handler.getStackInSlot(slot), like)) {
                extracted += handler.extractItem(slot, (int) Math.min(Integer.MAX_VALUE, amount - extracted), false).getCount();
            }
        }
        return extracted;
    }

    @Nullable
    private static <T> T capability(Level level, BlockPos pos, Direction side, Capability<T> capability) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        return blockEntity == null ? null : blockEntity.getCapability(capability, side).resolve().orElse(null);
    }
}
