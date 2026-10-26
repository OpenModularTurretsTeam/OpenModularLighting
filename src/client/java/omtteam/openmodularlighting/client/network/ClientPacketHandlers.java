package omtteam.openmodularlighting.client.network;

import omtteam.omlib.client.compat.ClientNetworkCompat;
import omtteam.openmodularlighting.config.OMLConfig;
import omtteam.openmodularlighting.network.OMLPackets;

public final class ClientPacketHandlers {
    private ClientPacketHandlers() {
    }

    public static void init() {
        ClientNetworkCompat.registerHandler(OMLPackets.CONFIG_SYNC, packet -> OMLConfig.applyServerConfig(packet.json()));
    }
}
