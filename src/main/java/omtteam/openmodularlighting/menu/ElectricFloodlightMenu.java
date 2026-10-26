package omtteam.openmodularlighting.menu;

import omtteam.omlib.menu.LongContainerData;
import net.minecraft.core.BlockPos;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import omtteam.openmodularlighting.blockentity.FloodlightBlockEntity;
import omtteam.openmodularlighting.energy.Batteries;
import omtteam.openmodularlighting.energy.ElectricPower;
import omtteam.openmodularlighting.registry.ModMenus;

/**
 * GUI of every electric light (1.7.10 {@code GuiElectricFloodlight}): battery slot and stored energy.
 */
public class ElectricFloodlightMenu extends FloodlightMenu {
    private static final int DATA_ENERGY = 0;
    private static final int DATA_CAPACITY = 1;

    private ElectricFloodlightMenu(int containerId, Inventory playerInventory, BlockPos pos,
                                   FloodlightBlockEntity light, SimpleContainer slot,
                                   ContainerData data) {
        super(ModMenus.ELECTRIC_FLOODLIGHT, containerId, playerInventory, pos, light, slot, data);
    }

    @Override
    protected boolean isValidItem(ItemStack stack) {
        return Batteries.isBattery(stack);
    }

    /** Client side. Uses its own container; the server keeps it in sync. */
    public static ElectricFloodlightMenu client(int containerId, Inventory playerInventory, BlockPos pos) {
        return new ElectricFloodlightMenu(containerId, playerInventory, pos, null, new SimpleContainer(1),
                new SimpleContainerData(LongContainerData.size(2)));
    }

    public static ElectricFloodlightMenu server(int containerId, Inventory playerInventory, FloodlightBlockEntity light,
                                                ElectricPower power) {
        return new ElectricFloodlightMenu(containerId, playerInventory, light.getBlockPos(), light, light.getInventory(),
                new LongContainerData(() -> power.store().getAmount(), () -> power.store().getCapacity()));
    }

    public long getEnergy() {
        return LongContainerData.read(data, DATA_ENERGY);
    }

    public long getCapacity() {
        return LongContainerData.read(data, DATA_CAPACITY);
    }
}
