package omtteam.openmodularlighting.gametest;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import omtteam.omlib.compat.VersionCompat;

public final class TestBlockEntities {
    public static final BlockEntityType<TestLightSourceBlockEntity> TEST_LIGHT_SOURCE = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE, VersionCompat.id(OMLGameTestMod.MOD_ID, "test_light_source"),
            VersionCompat.blockEntityType(TestLightSourceBlockEntity::new, TestBlocks.TEST_LIGHT_SOURCE));

    private TestBlockEntities() {
    }

    /** Loads the class, registering the block entity types. */
    public static void init() {
    }
}
