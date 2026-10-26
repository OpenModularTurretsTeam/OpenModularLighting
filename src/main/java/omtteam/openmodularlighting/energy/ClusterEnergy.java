package omtteam.openmodularlighting.energy;

import omtteam.openmodularlighting.blockentity.PanelLightBlockEntity;
import omtteam.openmodularlighting.core.cluster.Cluster;
import omtteam.openmodularlighting.core.energy.EnergyGroup;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Energy sharing of a {@link Cluster} of panel lights. Every light keeps its own buffer (so it is saved with the
 * block and nothing has to be merged or divided when lights are placed or broken); the cluster only decides where
 * energy goes in and where a light finds energy when its own buffer runs low:
 * <ul>
 *     <li>{@link View}: what cables see. Inserting fills the emptiest lights first. Nothing can be extracted.</li>
 *     <li>{@link #pull}: a light that cannot pay its next tick takes energy from the fullest other lights.</li>
 * </ul>
 */
public final class ClusterEnergy {
    /** A starved light asks for this many ticks of its cost at once, so that it does not scan every tick. */
    private static final int PULL_TICKS = 20;

    private ClusterEnergy() {
    }

    /**
     * If the light cannot pay this tick: takes energy from the other lights of its cluster, each giving at most half
     * of what it has, fullest first (so lights do not drain each other empty and ping-pong).
     *
     * @return false if the light did not need energy or found none (it should wait before looking again)
     */
    public static boolean pull(PanelLightBlockEntity light) {
        Cluster<PanelLightBlockEntity> cluster = light.cluster();
        ElectricPower own = light.buffer();
        if (cluster == null || cluster.size() < 2 || own == null) return true;
        long cost = own.cost();
        long stored = own.store().getAmount();
        if (cost == 0 || stored >= cost) return true;
        long wanted = Math.min(own.store().getCapacity(), cost * PULL_TICKS) - stored;
        List<PanelLightBlockEntity> donors = new ArrayList<>(cluster.members());
        donors.remove(light);
        donors.sort(Comparator.comparingLong((PanelLightBlockEntity donor) -> amount(donor)).reversed());
        long gained = 0;
        for (PanelLightBlockEntity donor : donors) {
            if (wanted <= 0) break;
            long available = amount(donor);
            if (available <= 0) break; // sorted: the rest is empty too
            long taken = donor.buffer().store().drain(Math.min(wanted, (available + 1) / 2));
            wanted -= taken;
            gained += taken;
        }
        own.store().generate(gained);
        return gained > 0;
    }

    private static long amount(PanelLightBlockEntity light) {
        ElectricPower buffer = light.buffer();
        return buffer == null ? 0 : buffer.store().getAmount();
    }

    /** The energy of a whole cluster, as seen from one of its lights. Insert only. */
    public static final class View implements EnergyGroup {
        private final PanelLightBlockEntity light;

        public View(PanelLightBlockEntity light) {
            this.light = light;
        }

        @Override
        public long insert(long maxAmount, boolean simulate) {
            return fill(maxAmount, (store, amount) -> store.insert(amount, simulate));
        }

        /** Inserts into the lights of the cluster, emptiest first, each through {@code inserter}. */
        @Override
        public long fill(long maxAmount, StoreInserter inserter) {
            List<PanelLightBlockEntity> members = members();
            members.sort(Comparator.comparingLong(ClusterEnergy::amount));
            long inserted = 0;
            for (PanelLightBlockEntity member : members) {
                if (inserted >= maxAmount) break;
                inserted += inserter.insert(member.buffer().store(), maxAmount - inserted);
            }
            return inserted;
        }

        @Override
        public long extract(long maxAmount, boolean simulate) {
            return 0;
        }

        @Override
        public long getAmount() {
            long sum = 0;
            for (PanelLightBlockEntity member : members()) sum += amount(member);
            return sum;
        }

        @Override
        public long getCapacity() {
            long sum = 0;
            for (PanelLightBlockEntity member : members()) sum += member.buffer().store().getCapacity();
            return sum;
        }

        /** The cluster's lights, or just this one if it has not joined yet. */
        private List<PanelLightBlockEntity> members() {
            Cluster<PanelLightBlockEntity> cluster = light.cluster();
            return cluster == null ? new ArrayList<>(List.of(light)) : new ArrayList<>(cluster.members());
        }
    }
}
