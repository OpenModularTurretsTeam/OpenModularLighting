package omtteam.openmodularlighting.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import omtteam.openmodularlighting.core.cluster.Cluster;
import omtteam.openmodularlighting.core.cluster.ClusterNode;
import omtteam.openmodularlighting.energy.ClusterEnergy;
import omtteam.openmodularlighting.energy.ElectricPower;
import org.jetbrains.annotations.Nullable;
import team.reborn.energy.api.EnergyStorage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Base of the panel lights (fluorescent and grow lights): electric, energy only from the back face, and energy shared
 * with panel lights directly next to it that face the same way (a {@link Cluster}, see {@link ClusterEnergy}).
 */
public abstract class PanelLightBlockEntity extends FloodlightBlockEntity implements ClusterNode<PanelLightBlockEntity> {
    /** Ticks to wait before looking for energy in the cluster again after finding none. */
    private static final int PULL_COOLDOWN = 10;

    /** Not saved: built again when the light first ticks after loading. */
    @Nullable
    private Cluster<PanelLightBlockEntity> cluster;
    private final ClusterEnergy.Storage clusterStorage = new ClusterEnergy.Storage(this);
    private int pullCooldown;

    protected PanelLightBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    /** 1.7.10 {@code canConnectEnergy}: only the face the light is mounted on. */
    @Override
    protected boolean acceptsEnergyFrom(@Nullable Direction side) {
        return side == null || side == facing().getOpposite();
    }

    /** The energy of the whole cluster: inserting fills the emptiest lights first, nothing can be extracted. */
    @Nullable
    @Override
    public EnergyStorage getEnergyStorage(@Nullable Direction side) {
        return acceptsEnergyFrom(side) ? clusterStorage : null;
    }

    @Override
    protected void beforePower(Level level) {
        if (cluster == null) {
            Cluster.join(this);
        }
        cluster.refresh();
        if (pullCooldown > 0) {
            pullCooldown--;
        } else if (!ClusterEnergy.pull(this)) {
            pullCooldown = PULL_COOLDOWN;
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        Cluster.leave(this);
    }

    // ---- ClusterNode ----

    @Nullable
    @Override
    public Cluster<PanelLightBlockEntity> cluster() {
        return cluster;
    }

    @Override
    public void setCluster(@Nullable Cluster<PanelLightBlockEntity> cluster) {
        this.cluster = cluster;
    }

    /** Panel lights (fluorescent and grow lights alike) on the 4 sides that face the same way. */
    @Override
    public Collection<PanelLightBlockEntity> neighbours() {
        List<PanelLightBlockEntity> result = new ArrayList<>(4);
        if (level == null) return result;
        Direction facing = facing();
        for (Direction side : Direction.values()) {
            if (side.getAxis() == facing.getAxis()) continue;
            BlockPos pos = worldPosition.relative(side);
            if (level.hasChunkAt(pos) && level.getBlockEntity(pos) instanceof PanelLightBlockEntity other
                    && !other.isRemoved() && other.facing() == facing) {
                result.add(other);
            }
        }
        return result;
    }

    /** This light's own buffer (panel lights are always electric). */
    public ElectricPower buffer() {
        return electricPower();
    }
}
