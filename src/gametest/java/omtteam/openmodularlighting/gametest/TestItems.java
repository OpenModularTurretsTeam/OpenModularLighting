package omtteam.openmodularlighting.gametest;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import omtteam.openmodularlighting.compat.VersionCompat;

public final class TestItems {
    public static final TestBatteryItem TEST_BATTERY = Registry.register(BuiltInRegistries.ITEM,
            VersionCompat.id(OMLGameTestMod.MOD_ID, "test_battery"), new TestBatteryItem(new Item.Properties().stacksTo(1)));

    private TestItems() {
    }

    /** Loads the class, registering the items. */
    public static void init() {
    }
}
