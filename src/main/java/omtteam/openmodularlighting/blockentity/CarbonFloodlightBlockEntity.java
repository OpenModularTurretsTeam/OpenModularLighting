package omtteam.openmodularlighting.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import omtteam.openmodularlighting.energy.FuelPower;
import omtteam.openmodularlighting.energy.LightPower;
import omtteam.openmodularlighting.registry.ModBlockEntities;

/**
 * Carbon (non-electric) floodlight (1.7.10 {@code TileEntityCarbonFloodlight}): burns furnace fuel from its slot.
 */
public class CarbonFloodlightBlockEntity extends BeamFloodlightBlockEntity {
    private final FuelPower power = new FuelPower(this::getMode);

    public CarbonFloodlightBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CARBON_FLOODLIGHT, pos, state);
    }

    @Override
    protected LightPower power() {
        return power;
    }

    /** Ticks the light can still run in its current mode (the stored burn time is used up twice as fast in cones). */
    public int getTicksRemaining() {
        return power.runTicks();
    }
}
