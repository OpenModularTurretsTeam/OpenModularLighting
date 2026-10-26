package omtteam.openmodularlighting.gametest.platform.neoforge;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import omtteam.openmodularlighting.gametest.TestTransfer;

/**
 * NeoForge block capabilities, through NeoForge's transfer API ({@code EnergyHandler},
 * {@code ResourceHandler<ItemResource>}; the variant for 1.21.11).
 */
public final class NeoForgeTestTransfer implements TestTransfer {
    @Override
    public boolean hasEnergy(Level level, BlockPos pos, Direction side) {
        return level.getCapability(Capabilities.Energy.BLOCK, pos, side) != null;
    }

    @Override
    public long insertEnergy(Level level, BlockPos pos, Direction side, long amount) {
        EnergyHandler handler = level.getCapability(Capabilities.Energy.BLOCK, pos, side);
        if (handler == null) return 0;
        try (Transaction transaction = Transaction.openRoot()) {
            int inserted = handler.insert(clamp(amount), transaction);
            transaction.commit();
            return inserted;
        }
    }

    @Override
    public long insertItem(Level level, BlockPos pos, Direction side, ItemStack stack) {
        ResourceHandler<ItemResource> handler = level.getCapability(Capabilities.Item.BLOCK, pos, side);
        if (handler == null || stack.isEmpty()) return 0;
        try (Transaction transaction = Transaction.openRoot()) {
            int inserted = handler.insert(ItemResource.of(stack), stack.getCount(), transaction);
            transaction.commit();
            return inserted;
        }
    }

    @Override
    public long extractItem(Level level, BlockPos pos, Direction side, ItemStack like, long amount) {
        ResourceHandler<ItemResource> handler = level.getCapability(Capabilities.Item.BLOCK, pos, side);
        if (handler == null || like.isEmpty()) return 0;
        try (Transaction transaction = Transaction.openRoot()) {
            int extracted = handler.extract(ItemResource.of(like), clamp(amount), transaction);
            transaction.commit();
            return extracted;
        }
    }

    private static int clamp(long amount) {
        return (int) Math.min(Integer.MAX_VALUE, amount);
    }
}
