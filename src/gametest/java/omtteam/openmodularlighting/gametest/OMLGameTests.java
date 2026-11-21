package omtteam.openmodularlighting.gametest;

import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.player.Player;

/**
 * Implemented by every test class (replaces Fabric's {@code FabricGameTest}, so the tests run on every loader).
 * The test mod registers the tests itself ({@code GameTestCases}).
 */
public interface OMLGameTests {
    /**
     * The empty 8 × 8 × 8 test structure ({@code data/openmodularlighting/structure/empty.nbt}), taken by its full id.
     */
    String EMPTY_STRUCTURE = "openmodularlighting:empty";

    /**
     * A mock player logged in to the test level (vanilla's {@code makeMockServerPlayerInLevel}).
     */
    static Player mockPlayer(GameTestHelper helper) {
        return helper.makeMockServerPlayerInLevel();
    }
}
