package omtteam.openmodularlighting.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import omtteam.openmodularlighting.config.OMLConfig;
import omtteam.openmodularlighting.core.energy.EnergyStore;
import omtteam.openmodularlighting.core.formula.EnergyCost;
import omtteam.openmodularlighting.energy.ElectricPower;
import omtteam.openmodularlighting.energy.LightPower;
import omtteam.openmodularlighting.registry.ModBlockEntities;

/**
 * Electric floodlight (1.7.10 {@code TileEntityElectricFloodlight}): {@code energyUsage} per tick, twice that in the
 * cone modes; energy accepted on all sides.
 */
public class ElectricFloodlightBlockEntity extends BeamFloodlightBlockEntity {
    public static final long CAPACITY = ElectricPower.CAPACITY;

    private final ElectricPower power = new ElectricPower(
            () -> EnergyCost.floodlight(OMLConfig.get().general.energyUsage, getMode()), this::setChanged);

    public ElectricFloodlightBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ELECTRIC_FLOODLIGHT, pos, state);
    }

    @Override
    protected LightPower power() {
        return power;
    }

    public EnergyStore getEnergyStore() {
        return power.store();
    }
}
