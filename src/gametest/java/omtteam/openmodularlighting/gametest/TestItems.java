package omtteam.openmodularlighting.gametest;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import omtteam.openmodularlighting.compat.VersionCompat;

public final class TestItems {
    private static final ResourceLocation ID = VersionCompat.id(OMLGameTestMod.MOD_ID, "test_battery");
    public static final TestBatteryItem TEST_BATTERY = Registry.register(BuiltInRegistries.ITEM,
            ID, new TestBatteryItem(VersionCompat.itemProperties(new Item.Properties().stacksTo(1), ID)));

    private TestItems() {
    }

    /** Loads the class, registering the items. */
    public static void init() {
    }
}
