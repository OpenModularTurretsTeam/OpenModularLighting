package omtteam.openmodularlighting.tileentity;

import omtteam.openmodularlighting.energy.FuelPower;
import omtteam.openmodularlighting.energy.LightPower;

/**
 * Carbon (non-electric) floodlight (1.7.10 {@code TileEntityCarbonFloodlight}): burns furnace fuel from its slot.
 */
public class CarbonFloodlightTileEntity extends BeamFloodlightTileEntity {
    private final FuelPower power = new FuelPower(this::getMode);

    @Override
    protected LightPower power() {
        return power;
    }

    /** Ticks the light can still run in its current mode (the stored burn time is used up twice as fast in cones). */
    public int getTicksRemaining() {
        return power.runTicks();
    }
}
