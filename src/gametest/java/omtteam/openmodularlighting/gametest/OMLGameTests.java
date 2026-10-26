package omtteam.openmodularlighting.gametest;

import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.player.Player;
//? if forge {
/*import com.mojang.authlib.GameProfile;
import io.netty.channel.embedded.EmbeddedChannel;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;
*///?}

/**
 * Implemented by every test class (replaces Fabric's {@code FabricGameTest}, so the tests run on every loader).
 * Before 1.21.11, NeoForge and Forge test classes also carry {@code @GameTestHolder(OML.MOD_ID)} and
 * {@code @PrefixGameTestTemplate(false)}; from 1.21.11 on the test mod registers the tests itself
 * ({@code GameTestCases}).
 */
public interface OMLGameTests {
    /**
     * The empty 8 × 8 × 8 test structure ({@code data/openmodularlighting/structure/empty.nbt}, {@code structures/} on
     * 1.20.1). Fabric and 1.21.11 take the full id; NeoForge and Forge before put the namespace of
     * {@code @GameTestHolder} in front.
     */
    //? if fabric || >=1.21.11 {
    String EMPTY_STRUCTURE = "openmodularlighting:empty";
    //?} else {
    /*String EMPTY_STRUCTURE = "empty";
    *///?}

    /**
     * A mock player logged in to the test level (vanilla's {@code makeMockServerPlayerInLevel}). On Forge 1.20.1 its
     * connection needs a netty channel, which vanilla 1.20.1 does not give it (Forge's login handling reads the
     * channel's pipeline), so the player is built there as vanilla 1.20.5+ does it, with an {@link EmbeddedChannel}.
     */
    static Player mockPlayer(GameTestHelper helper) {
        //? if forge {
        /*ServerPlayer player = new ServerPlayer(helper.getLevel().getServer(), helper.getLevel(),
                new GameProfile(UUID.randomUUID(), "test-mock-player")) {
            @Override
            public boolean isSpectator() {
                return false;
            }

            @Override
            public boolean isCreative() {
                return true;
            }
        };
        Connection connection = new Connection(PacketFlow.SERVERBOUND);
        new EmbeddedChannel(connection);
        helper.getLevel().getServer().getPlayerList().placeNewPlayer(connection, player);
        return player;
        *///?} else {
        return helper.makeMockServerPlayerInLevel();
        //?}
    }
}
