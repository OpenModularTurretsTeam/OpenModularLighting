package omtteam.openmodularlighting.gametest;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import omtteam.openmodularlighting.compat.VersionCompat;
import omtteam.openmodularlighting.config.OMLConfig;
import team.reborn.energy.api.EnergyStorage;
import team.reborn.energy.api.base.SimpleEnergyItem;

/**
 * Test mod: registers a fake light source ({@link TestLightSourceBlock}) that game tests control directly, and a
 * battery item ({@link TestBatteryItem}).
 */
public class OMLGameTestMod implements ModInitializer {
    public static final String MOD_ID = "openmodularlighting-gametest";

    public static final TestLightSourceBlock TEST_LIGHT_SOURCE = Registry.register(BuiltInRegistries.BLOCK,
            VersionCompat.id(MOD_ID, "test_light_source"), new TestLightSourceBlock(BlockBehaviour.Properties.of()));

    public static final BlockEntityType<TestLightSourceBlockEntity> TEST_LIGHT_SOURCE_TYPE = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE, VersionCompat.id(MOD_ID, "test_light_source"),
            BlockEntityType.Builder.of(TestLightSourceBlockEntity::new, TEST_LIGHT_SOURCE).build(null));

    public static final TestBatteryItem TEST_BATTERY = Registry.register(BuiltInRegistries.ITEM,
            VersionCompat.id(MOD_ID, "test_battery"), new TestBatteryItem(new Item.Properties().stacksTo(1)));

    /**
     * Beam ranges for the tests: short enough to stay inside the 8 × 8 × 8 test structure (1.21 surrounds it with
     * barriers), so that beams neither leave it nor reach tests running next to it.
     */
    public static final int TEST_RANGE = 6;

    @Override
    public void onInitialize() {
        // Entrypoints do not run in dependency order: the main mod may load the config after this, so set the
        // ranges once the server starts
        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            OMLConfig.get().general.rangeStraightFloodlight = TEST_RANGE;
            OMLConfig.get().general.rangeConeFloodlight = TEST_RANGE;
            OMLConfig.get().general.rangeUVFloodlight = TEST_RANGE;
        });
        EnergyStorage.ITEM.registerForItems((stack, context) -> SimpleEnergyItem.createStorage(context,
                TestBatteryItem.CAPACITY, TestBatteryItem.CAPACITY, TestBatteryItem.CAPACITY), TEST_BATTERY);
    }
}
