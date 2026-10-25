package omtteam.openmodularlighting.tileentity;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import omtteam.omlib.core.energy.EnergyAccess;
import omtteam.openmodularlighting.core.cluster.Cluster;
import omtteam.openmodularlighting.core.cluster.ClusterNode;
import omtteam.openmodularlighting.energy.ClusterEnergy;
import omtteam.openmodularlighting.energy.ElectricPower;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Base of the lights that share energy (fluorescent, grow and cube lights): electric, and energy shared with the lights
 * directly next to it that {@link #connectsTo connect} (a {@link Cluster}, see {@link ClusterEnergy}). Panel lights
 * take energy only from the back face and connect to panel lights beside them that face the same way.
 */
public abstract class PanelLightTileEntity extends FloodlightTileEntity implements ClusterNode<PanelLightTileEntity> {
    /** Ticks to wait before looking for energy in the cluster again after finding none. */
    private static final int PULL_COOLDOWN = 10;

    /** Not saved: built again when the light first ticks after loading. */
    @Nullable
    private Cluster<PanelLightTileEntity> cluster;
    private final ClusterEnergy.View clusterEnergy = new ClusterEnergy.View(this);
    private int pullCooldown;

    /** 1.7.10 {@code canConnectEnergy}: only the face the light is mounted on. */
    @Override
    protected boolean acceptsEnergyFrom(@Nullable EnumFacing side) {
        return side == null || side == facing().getOpposite();
    }

    /** The energy of the whole cluster: inserting fills the emptiest lights first, nothing can be extracted. */
    @Nullable
    @Override
    public EnergyAccess getEnergyAccess(@Nullable EnumFacing side) {
        return acceptsEnergyFrom(side) ? clusterEnergy : null;
    }

    @Override
    protected void beforePower(World world) {
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
    public void invalidate() {
        super.invalidate();
        Cluster.leave(this);
    }

    @Override
    public void onChunkUnload() {
        super.onChunkUnload();
        Cluster.leave(this);
    }

    // ---- ClusterNode ----

    @Nullable
    @Override
    public Cluster<PanelLightTileEntity> cluster() {
        return cluster;
    }

    @Override
    public void setCluster(@Nullable Cluster<PanelLightTileEntity> cluster) {
        this.cluster = cluster;
    }

    @Override
    public Collection<PanelLightTileEntity> neighbours() {
        List<PanelLightTileEntity> result = new ArrayList<>(6);
        if (world == null) return result;
        for (EnumFacing side : EnumFacing.values()) {
            BlockPos other = pos.offset(side);
            if (!world.isBlockLoaded(other)) continue;
            TileEntity tile = world.getTileEntity(other);
            if (tile instanceof PanelLightTileEntity && !tile.isInvalid()
                    && connectsTo(side, (PanelLightTileEntity) tile)) {
                result.add((PanelLightTileEntity) tile);
            }
        }
        return result;
    }

    /**
     * Whether the light on {@code side} is part of the same cluster. Must be symmetric. Panel lights (fluorescent and
     * grow lights alike) connect on the 4 sides to panel lights that face the same way.
     */
    protected boolean connectsTo(EnumFacing side, PanelLightTileEntity other) {
        return side.getAxis() != facing().getAxis() && !(other instanceof CubeLightTileEntity)
                && other.facing() == facing();
    }

    /** This light's own buffer (panel lights are always electric). */
    public ElectricPower buffer() {
        return Objects.requireNonNull(electricPower(), "panel lights are electric");
    }
}
