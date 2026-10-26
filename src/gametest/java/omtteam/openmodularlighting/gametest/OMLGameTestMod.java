package omtteam.openmodularlighting.gametest;

import net.minecraft.core.registries.Registries;
import omtteam.openmodularlighting.config.OMLConfig;
import omtteam.omlib.registry.RegistrationStep;

import java.util.List;

/**
 * Test mod (never shipped): a fake light source ({@link TestLightSourceBlock}) that game tests control directly, a
 * battery item ({@link TestBatteryItem}), and the {@link TestTransfer} of the running loader. The loader entrypoints in
 * {@code platform/<loader>} install the transfer, run the registration steps and call {@link #onServerStarting}.
 */
public final class OMLGameTestMod {
    public static final String MOD_ID = "openmodularlighting_gametest";

    /**
     * Beam ranges for the tests: short enough to stay inside the 8 × 8 × 8 test structure (1.21 surrounds it with
     * barriers), so that beams neither leave it nor reach tests running next to it.
     */
    public static final int TEST_RANGE = 6;

    private static TestTransfer transfer;

    private OMLGameTestMod() {
    }

    /** Called once by the loader entrypoint, first thing. */
    public static void install(TestTransfer testTransfer) {
        if (transfer != null) {
            throw new IllegalStateException("Test transfer already installed");
        }
        transfer = testTransfer;
    }

    public static TestTransfer transfer() {
        if (transfer == null) {
            throw new IllegalStateException("Test transfer not installed yet");
        }
        return transfer;
    }

    /** Registration steps of the test content, like {@code OML#registrationSteps}. */
    public static List<RegistrationStep> registrationSteps() {
        return List.of(
                new RegistrationStep(Registries.BLOCK, TestBlocks::init),
                new RegistrationStep(Registries.ITEM, TestItems::init),
                new RegistrationStep(Registries.BLOCK_ENTITY_TYPE, TestBlockEntities::init));
    }

    /**
     * Sets the test beam ranges. Called when the server starts: entrypoints do not run in dependency order, so the main
     * mod may load the config after the test mod's entrypoint.
     */
    public static void onServerStarting() {
        OMLConfig.get().general.rangeStraightFloodlight = TEST_RANGE;
        OMLConfig.get().general.rangeConeFloodlight = TEST_RANGE;
        OMLConfig.get().general.rangeUVFloodlight = TEST_RANGE;
    }
}
