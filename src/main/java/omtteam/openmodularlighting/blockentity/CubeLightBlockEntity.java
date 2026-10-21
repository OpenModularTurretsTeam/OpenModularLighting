package omtteam.openmodularlighting.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import omtteam.openmodularlighting.config.OMLConfig;
import omtteam.openmodularlighting.core.beam.BeamShape;
import omtteam.openmodularlighting.energy.ElectricPower;
import omtteam.openmodularlighting.energy.LightPower;
import omtteam.openmodularlighting.registry.ModBlockEntities;
import org.jetbrains.annotations.Nullable;

/**
 * Cube light: a full block that lights the 6 blocks around it. Takes energy from every side and shares it with the
 * cube lights that touch it on any face. Like the UV floodlight it has no timeout after turning off.
 */
public class CubeLightBlockEntity extends PanelLightBlockEntity {
    private static final int[] SHAPE_KEY = {0};

    private final ElectricPower power = new ElectricPower(
            () -> OMLConfig.get().general.energyUsageCubeLight, this::setChanged);

    public CubeLightBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CUBE_LIGHT, pos, state);
    }

    @Override
    protected boolean acceptsEnergyFrom(@Nullable Direction side) {
        return true;
    }

    @Override
    protected boolean connectsTo(Direction side, PanelLightBlockEntity other) {
        return other instanceof CubeLightBlockEntity;
    }

    @Override
    protected LightPower power() {
        return power;
    }

    @Override
    protected BeamShape currentShape() {
        return BeamShape.cube();
    }

    @Override
    protected int[] shapeKey() {
        return SHAPE_KEY;
    }

    @Override
    protected int timeoutTicks() {
        return 0;
    }
}
