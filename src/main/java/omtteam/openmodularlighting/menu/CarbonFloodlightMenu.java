package omtteam.openmodularlighting.menu;

import net.minecraft.core.BlockPos;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import omtteam.openmodularlighting.blockentity.CarbonFloodlightBlockEntity;
import omtteam.openmodularlighting.energy.FuelPower;
import omtteam.openmodularlighting.registry.ModMenus;

/**
 * Carbon floodlight GUI: fuel slot and remaining burn time.
 */
public class CarbonFloodlightMenu extends FloodlightMenu {
    private static final int DATA_TICKS_REMAINING = 0;

    /** The player's level, for the fuel values (the light is only known on the server). */
    private final Level level;

    private CarbonFloodlightMenu(int containerId, Inventory playerInventory, BlockPos pos,
                                 CarbonFloodlightBlockEntity light, SimpleContainer slot, ContainerData data) {
        super(ModMenus.CARBON_FLOODLIGHT, containerId, playerInventory, pos, light, slot, data);
        this.level = playerInventory.player.level();
    }

    @Override
    protected boolean isValidItem(ItemStack stack) {
        return FuelPower.burnTime(level, stack) > 0;
    }

    /** Client side. Uses its own container; the server keeps it in sync. */
    public static CarbonFloodlightMenu client(int containerId, Inventory playerInventory, BlockPos pos) {
        return new CarbonFloodlightMenu(containerId, playerInventory, pos, null, new SimpleContainer(1),
                new SimpleContainerData(LongContainerData.size(1)));
    }

    public static CarbonFloodlightMenu server(int containerId, Inventory playerInventory, CarbonFloodlightBlockEntity light) {
        return new CarbonFloodlightMenu(containerId, playerInventory, light.getBlockPos(), light, light.getInventory(),
                new LongContainerData(light::getTicksRemaining));
    }

    public long getTicksRemaining() {
        return LongContainerData.read(data, DATA_TICKS_REMAINING);
    }
}
