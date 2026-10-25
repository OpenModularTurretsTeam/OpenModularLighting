package omtteam.openmodularlighting.tileentity;

import omtteam.omlib.core.energy.EnergyStore;
import omtteam.openmodularlighting.config.OMLConfig;
import omtteam.openmodularlighting.core.formula.EnergyCost;
import omtteam.openmodularlighting.energy.ElectricPower;
import omtteam.openmodularlighting.energy.LightPower;

/**
 * Electric floodlight (1.7.10 {@code TileEntityElectricFloodlight}): {@code energyUsage} per tick, twice that in the
 * cone modes; energy accepted on all sides.
 */
public class ElectricFloodlightTileEntity extends BeamFloodlightTileEntity {
    public static final long CAPACITY = ElectricPower.CAPACITY;

    private final ElectricPower power = new ElectricPower(
            () -> EnergyCost.floodlight(OMLConfig.get().general.energyUsage, getMode()), this::markDirty);

    @Override
    protected LightPower power() {
        return power;
    }

    public EnergyStore getEnergyStore() {
        return power.store();
    }
}
