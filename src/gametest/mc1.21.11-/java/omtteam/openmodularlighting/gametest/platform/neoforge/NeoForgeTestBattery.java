package omtteam.openmodularlighting.gametest.platform.neoforge;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.energy.ItemAccessEnergyHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import omtteam.openmodularlighting.gametest.TestBatteryItem;
import omtteam.openmodularlighting.gametest.TestItems;

/**
 * Energy handler of a {@link TestBatteryItem}, through NeoForge's transfer API: the item access writes the changed
 * battery back (e.g. into its slot) when the transaction commits. The energy stays in the item's custom data, as on
 * Fabric, instead of a data component of its own. The variant for 1.21.11.
 */
final class NeoForgeTestBattery extends ItemAccessEnergyHandler {
    private NeoForgeTestBattery(ItemAccess access) {
        // no energy component: getAmountFrom and update use the custom data
        super(access, null, (int) TestBatteryItem.CAPACITY);
    }

    static void register(RegisterCapabilitiesEvent event) {
        event.registerItem(Capabilities.Energy.ITEM, (stack, access) -> new NeoForgeTestBattery(access),
                TestItems.TEST_BATTERY);
    }

    @Override
    protected int getAmountFrom(ItemResource resource) {
        return resource.is(TestItems.TEST_BATTERY) ? (int) TestItems.TEST_BATTERY.getStoredEnergy(resource.toStack()) : 0;
    }

    @Override
    protected ItemResource update(ItemResource resource, int amount) {
        ItemStack stack = resource.toStack();
        TestItems.TEST_BATTERY.setStoredEnergy(stack, amount);
        return ItemResource.of(stack);
    }
}
