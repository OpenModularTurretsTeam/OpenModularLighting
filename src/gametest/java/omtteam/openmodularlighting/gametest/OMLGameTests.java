package omtteam.openmodularlighting.gametest;

import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.player.Player;

/**
 * Implemented by every test class (replaces Fabric's {@code FabricGameTest}, so the tests run on every loader).
 * NeoForge test classes also carry {@code @GameTestHolder(OML.MOD_ID)} and {@code @PrefixGameTestTemplate(false)}.
 */
public interface OMLGameTests {
    /**
     * The empty 8 × 8 × 8 test structure ({@code data/openmodularlighting/structure/empty.nbt}). Fabric takes the full
     * id; NeoForge puts the namespace of {@code @GameTestHolder} in front.
     */
    //? if fabric {
    String EMPTY_STRUCTURE = "openmodularlighting:empty";
    //?} else {
    /*String EMPTY_STRUCTURE = "empty";
    *///?}

    /**
     * A mock player logged in to the test level (vanilla's {@code makeMockServerPlayerInLevel}).
     */
    static Player mockPlayer(GameTestHelper helper) {
        return helper.makeMockServerPlayerInLevel();
    }
}
