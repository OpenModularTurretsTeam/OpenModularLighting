package omtteam.openmodularlighting.network;

import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import omtteam.openmodularlighting.OML;

/**
 * The mod's network channel and all its packets.
 */
public final class OMLNetwork {
    public static final SimpleNetworkWrapper CHANNEL = NetworkRegistry.INSTANCE.newSimpleChannel(OML.MOD_ID);

    private static int nextId;

    private OMLNetwork() {
    }

    /** Registers all packets; called in pre-initialization. */
    public static void init() {
        CHANNEL.registerMessage(ConfigSyncPacket.Handler.class, ConfigSyncPacket.class, nextId++, Side.CLIENT);
    }
}
