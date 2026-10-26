package omtteam.openmodularlighting.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import omtteam.openmodularlighting.core.cluster.Cluster;
import omtteam.openmodularlighting.core.cluster.ClusterNode;
import omtteam.omlib.core.energy.EnergyAccess;
import omtteam.openmodularlighting.energy.ClusterEnergy;
import omtteam.openmodularlighting.energy.ElectricPower;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Base of the lights that share energy (fluorescent, grow and cube lights): electric, and energy shared with the lights
 * directly next to it that {@link #connectsTo connect} (a {@link Cluster}, see {@link ClusterEnergy}). Panel lights
 * take energy only from the back face and connect to panel lights beside them that face the same way.
 */
public abstract class PanelLightBlockEntity extends FloodlightBlockEntity implements ClusterNode<PanelLightBlockEntity> {
    /** Ticks to wait before looking for energy in the cluster again after finding none. */
    private static final int PULL_COOLDOWN = 10;

    /** Not saved: built again when the light first ticks after loading. */
    @Nullable
    private Cluster<PanelLightBlockEntity> cluster;
    private final ClusterEnergy.View clusterEnergy = new ClusterEnergy.View(this);
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
    public EnergyAccess getEnergyAccess(@Nullable Direction side) {
        return acceptsEnergyFrom(side) ? clusterEnergy : null;
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

    @Override
    public Collection<PanelLightBlockEntity> neighbours() {
        List<PanelLightBlockEntity> result = new ArrayList<>(6);
        if (level == null) return result;
        for (Direction side : Direction.values()) {
            BlockPos pos = worldPosition.relative(side);
            if (level.hasChunkAt(pos) && level.getBlockEntity(pos) instanceof PanelLightBlockEntity other
                    && !other.isRemoved() && connectsTo(side, other)) {
                result.add(other);
            }
        }
        return result;
    }

    /**
     * Whether the light on {@code side} is part of the same cluster. Must be symmetric. Panel lights (fluorescent and
     * grow lights alike) connect on the 4 sides to panel lights that face the same way.
     */
    protected boolean connectsTo(Direction side, PanelLightBlockEntity other) {
        return side.getAxis() != facing().getAxis() && !(other instanceof CubeLightBlockEntity)
                && other.facing() == facing();
    }

    /** This light's own buffer (panel lights are always electric). */
    public ElectricPower buffer() {
        return electricPower();
    }
}
