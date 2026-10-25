package omtteam.openmodularlighting.platform.neoforge;

import com.google.common.collect.MapMaker;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import omtteam.openmodularlighting.core.energy.EnergyAccess;
import omtteam.openmodularlighting.core.energy.EnergyStore;
import omtteam.openmodularlighting.energy.ClusterEnergy;

import java.util.Map;

/**
 * NeoForge views ({@link EnergyHandler}, FE, {@code int}) of {@link EnergyAccess}; 1 E = 1 FE, larger amounts are
 * clamped to {@link Integer#MAX_VALUE}. A store gets one {@link SnapshotJournal} for exact rollback in transactions; a
 * cluster view distributes into its members' store handlers (like {@code FabricEnergyStorages}).
 */
public final class NeoForgeEnergyHandler {
    /**
     * One handler per store, so that a transaction snapshots each store once. Weak values: a handler is kept alive by
     * the transactions it takes part in, and between transactions a new one is just as good (no snapshot is open).
     */
    private static final Map<EnergyStore, StoreHandler> STORES = new MapMaker().weakKeys().weakValues().makeMap();

    private NeoForgeEnergyHandler() {
    }

    public static EnergyHandler of(EnergyAccess access) {
        if (access instanceof EnergyStore store) {
            return STORES.computeIfAbsent(store, StoreHandler::new);
        }
        if (access instanceof ClusterEnergy.View view) {
            return new ClusterHandler(view);
        }
        throw new IllegalArgumentException("No NeoForge energy handler for " + access.getClass());
    }

    static int clamp(long amount) {
        return (int) Math.min(Integer.MAX_VALUE, Math.max(0, amount));
    }

    private static final class StoreHandler extends SnapshotJournal<Long> implements EnergyHandler {
        private final EnergyStore store;

        StoreHandler(EnergyStore store) {
            this.store = store;
        }

        @Override
        public long getAmountAsLong() {
            return store.getAmount();
        }

        @Override
        public long getCapacityAsLong() {
            return store.getCapacity();
        }

        @Override
        public int insert(int amount, TransactionContext transaction) {
            TransferPreconditions.checkNonNegative(amount);
            int inserted = clamp(store.insert(amount, true));
            if (inserted > 0) {
                updateSnapshots(transaction);
                store.insert(inserted, false);
            }
            return inserted;
        }

        @Override
        public int extract(int amount, TransactionContext transaction) {
            TransferPreconditions.checkNonNegative(amount);
            int extracted = clamp(store.extract(amount, true));
            if (extracted > 0) {
                updateSnapshots(transaction);
                store.extract(extracted, false);
            }
            return extracted;
        }

        @Override
        protected Long createSnapshot() {
            return store.getAmount();
        }

        @Override
        protected void revertToSnapshot(Long snapshot) {
            store.setAmountUnchecked(snapshot);
        }
    }

    /** Insert only, emptiest light first ({@link ClusterEnergy.View#fill}). */
    private static final class ClusterHandler implements EnergyHandler {
        private final ClusterEnergy.View view;

        ClusterHandler(ClusterEnergy.View view) {
            this.view = view;
        }

        @Override
        public long getAmountAsLong() {
            return view.getAmount();
        }

        @Override
        public long getCapacityAsLong() {
            return view.getCapacity();
        }

        @Override
        public int insert(int amount, TransactionContext transaction) {
            TransferPreconditions.checkNonNegative(amount);
            return clamp(view.fill(amount, (store, part) -> of(store).insert(clamp(part), transaction)));
        }

        @Override
        public int extract(int amount, TransactionContext transaction) {
            return 0;
        }
    }
}
