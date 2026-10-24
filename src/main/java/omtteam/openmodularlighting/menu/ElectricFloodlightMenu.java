package omtteam.openmodularlighting.menu;

import net.minecraft.entity.player.InventoryPlayer;
import omtteam.openmodularlighting.blockentity.FloodlightBlockEntity;
import omtteam.openmodularlighting.energy.ElectricPower;

/**
 * GUI of every electric light (1.7.10 {@code GuiElectricFloodlight}): battery slot and stored energy.
 */
public class ElectricFloodlightMenu extends FloodlightMenu {
    private static final int DATA_ENERGY = 0;
    private static final int DATA_CAPACITY = 1;

    public ElectricFloodlightMenu(InventoryPlayer playerInventory, FloodlightBlockEntity light, ElectricPower power) {
        super(playerInventory, light, () -> power.store().getAmount(), () -> power.store().getCapacity());
    }

    public long getEnergy() {
        return value(DATA_ENERGY);
    }

    public long getCapacity() {
        return value(DATA_CAPACITY);
    }
}
