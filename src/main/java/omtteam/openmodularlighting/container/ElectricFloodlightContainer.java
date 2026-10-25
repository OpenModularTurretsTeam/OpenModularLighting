package omtteam.openmodularlighting.container;

import net.minecraft.entity.player.InventoryPlayer;
import omtteam.openmodularlighting.energy.ElectricPower;
import omtteam.openmodularlighting.tileentity.FloodlightTileEntity;

/**
 * GUI of every electric light (1.7.10 {@code GuiElectricFloodlight}): battery slot and stored energy.
 */
public class ElectricFloodlightContainer extends FloodlightContainer {
    private static final int DATA_ENERGY = 0;
    private static final int DATA_CAPACITY = 1;

    public ElectricFloodlightContainer(InventoryPlayer playerInventory, FloodlightTileEntity light, ElectricPower power) {
        super(playerInventory, light, () -> power.store().getAmount(), () -> power.store().getCapacity());
    }

    public long getEnergy() {
        return value(DATA_ENERGY);
    }

    public long getCapacity() {
        return value(DATA_CAPACITY);
    }
}
