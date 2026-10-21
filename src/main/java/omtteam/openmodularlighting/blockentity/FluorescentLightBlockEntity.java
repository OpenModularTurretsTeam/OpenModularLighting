package omtteam.openmodularlighting.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import omtteam.openmodularlighting.config.OMLConfig;
import omtteam.openmodularlighting.core.beam.BeamShape;
import omtteam.openmodularlighting.energy.ElectricPower;
import omtteam.openmodularlighting.energy.LightPower;
import omtteam.openmodularlighting.registry.ModBlockEntities;

/**
 * Small and square fluorescent lights (1.7.10 {@code TileEntitySmallFloodlight}): light the block in front and the 4
 * blocks beside them; {@code energyUsageSmallFloodlight} per tick, energy only from the back. No timeout after
 * turning off.
 */
public class FluorescentLightBlockEntity extends PanelLightBlockEntity {
    private static final int[] SHAPE_KEY = {0};

    private final ElectricPower power = new ElectricPower(
            () -> OMLConfig.get().general.energyUsageSmallFloodlight, this::setChanged);

    public FluorescentLightBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FLUORESCENT_LIGHT, pos, state);
    }

    @Override
    protected LightPower power() {
        return power;
    }

    @Override
    protected BeamShape currentShape() {
        return BeamShape.fluorescent();
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
