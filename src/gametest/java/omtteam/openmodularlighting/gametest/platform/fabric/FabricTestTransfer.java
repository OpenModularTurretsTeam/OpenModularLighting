package omtteam.openmodularlighting.gametest.platform.fabric;

import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import omtteam.openmodularlighting.gametest.TestTransfer;
import team.reborn.energy.api.EnergyStorage;

/**
 * Team Reborn Energy and the Transfer API, each operation in its own committed transaction.
 */
public final class FabricTestTransfer implements TestTransfer {
    @Override
    public boolean hasEnergy(Level level, BlockPos pos, Direction side) {
        return EnergyStorage.SIDED.find(level, pos, side) != null;
    }

    @Override
    public long insertEnergy(Level level, BlockPos pos, Direction side, long amount) {
        EnergyStorage storage = EnergyStorage.SIDED.find(level, pos, side);
        if (storage == null) return 0;
        try (Transaction transaction = Transaction.openOuter()) {
            long inserted = storage.insert(amount, transaction);
            transaction.commit();
            return inserted;
        }
    }

    @Override
    public long insertItem(Level level, BlockPos pos, Direction side, ItemStack stack) {
        Storage<ItemVariant> storage = ItemStorage.SIDED.find(level, pos, side);
        if (storage == null || stack.isEmpty()) return 0;
        try (Transaction transaction = Transaction.openOuter()) {
            long inserted = storage.insert(ItemVariant.of(stack), stack.getCount(), transaction);
            transaction.commit();
            return inserted;
        }
    }

    @Override
    public long extractItem(Level level, BlockPos pos, Direction side, ItemStack like, long amount) {
        Storage<ItemVariant> storage = ItemStorage.SIDED.find(level, pos, side);
        if (storage == null || like.isEmpty()) return 0;
        try (Transaction transaction = Transaction.openOuter()) {
            long extracted = storage.extract(ItemVariant.of(like), amount, transaction);
            transaction.commit();
            return extracted;
        }
    }
}
