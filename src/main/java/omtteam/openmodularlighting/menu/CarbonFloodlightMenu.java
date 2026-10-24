package omtteam.openmodularlighting.menu;

import net.minecraft.entity.player.InventoryPlayer;
import omtteam.openmodularlighting.blockentity.CarbonFloodlightBlockEntity;

/**
 * Carbon floodlight GUI: fuel slot and remaining burn time.
 */
public class CarbonFloodlightMenu extends FloodlightMenu {
    private static final int DATA_TICKS_REMAINING = 0;

    public CarbonFloodlightMenu(InventoryPlayer playerInventory, CarbonFloodlightBlockEntity light) {
        super(playerInventory, light, light::getTicksRemaining);
    }

    public long getTicksRemaining() {
        return value(DATA_TICKS_REMAINING);
    }
}
