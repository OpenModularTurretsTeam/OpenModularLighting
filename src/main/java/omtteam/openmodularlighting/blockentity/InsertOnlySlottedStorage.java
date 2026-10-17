package omtteam.openmodularlighting.blockentity;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.SlottedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

import java.util.Iterator;

/**
 * A {@link SlottedStorage} that accepts insertion but nothing can be extracted. Unlike
 * {@code FilteringStorage.insertOnlyOf}, it keeps the slots, so mods that read inventories slot by slot (Jade) can
 * still show the contents.
 */
public final class InsertOnlySlottedStorage implements SlottedStorage<ItemVariant> {
    private final SlottedStorage<ItemVariant> backing;

    public InsertOnlySlottedStorage(SlottedStorage<ItemVariant> backing) {
        this.backing = backing;
    }

    @Override
    public boolean supportsExtraction() {
        return false;
    }

    @Override
    public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
        return backing.insert(resource, maxAmount, transaction);
    }

    @Override
    public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
        return 0;
    }

    @Override
    public int getSlotCount() {
        return backing.getSlotCount();
    }

    @Override
    public SingleSlotStorage<ItemVariant> getSlot(int slot) {
        return new Slot(backing.getSlot(slot));
    }

    @Override
    public Iterator<StorageView<ItemVariant>> iterator() {
        return new Iterator<>() {
            private int next;

            @Override
            public boolean hasNext() {
                return next < getSlotCount();
            }

            @Override
            public StorageView<ItemVariant> next() {
                return getSlot(next++);
            }
        };
    }

    private record Slot(SingleSlotStorage<ItemVariant> backing) implements SingleSlotStorage<ItemVariant> {
        @Override
        public boolean supportsExtraction() {
            return false;
        }

        @Override
        public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
            return backing.insert(resource, maxAmount, transaction);
        }

        @Override
        public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
            return 0;
        }

        @Override
        public boolean isResourceBlank() {
            return backing.isResourceBlank();
        }

        @Override
        public ItemVariant getResource() {
            return backing.getResource();
        }

        @Override
        public long getAmount() {
            return backing.getAmount();
        }

        @Override
        public long getCapacity() {
            return backing.getCapacity();
        }
    }
}
