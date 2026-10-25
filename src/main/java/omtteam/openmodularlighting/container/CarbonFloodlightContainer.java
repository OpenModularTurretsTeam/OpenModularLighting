package omtteam.openmodularlighting.container;

import net.minecraft.entity.player.InventoryPlayer;
import omtteam.openmodularlighting.tileentity.CarbonFloodlightTileEntity;

/**
 * Carbon floodlight GUI: fuel slot and remaining burn time.
 */
public class CarbonFloodlightContainer extends FloodlightContainer {
    private static final int DATA_TICKS_REMAINING = 0;

    public CarbonFloodlightContainer(InventoryPlayer playerInventory, CarbonFloodlightTileEntity light) {
        super(playerInventory, light, light::getTicksRemaining);
    }

    public long getTicksRemaining() {
        return value(DATA_TICKS_REMAINING);
    }
}
