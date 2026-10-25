package omtteam.openmodularlighting.gametest;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockBehaviour;
import omtteam.openmodularlighting.compat.VersionCompat;

public final class TestBlocks {
    private static final ResourceLocation ID = VersionCompat.id(OMLGameTestMod.MOD_ID, "test_light_source");
    public static final TestLightSourceBlock TEST_LIGHT_SOURCE = Registry.register(BuiltInRegistries.BLOCK,
            ID, new TestLightSourceBlock(VersionCompat.blockProperties(BlockBehaviour.Properties.of(), ID)));

    private TestBlocks() {
    }

    /** Loads the class, registering the blocks. */
    public static void init() {
    }
}
