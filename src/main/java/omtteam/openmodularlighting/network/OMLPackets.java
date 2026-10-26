package omtteam.openmodularlighting.network;

import omtteam.omlib.network.ConfigSyncPacket;
import omtteam.openmodularlighting.OML;
import omtteam.omlib.compat.network.NetworkCompat;
import omtteam.omlib.compat.network.PacketType;

/**
 * All packet types of the mod. Client handlers are registered in
 * {@code omtteam.openmodularlighting.client.network.ClientPacketHandlers}.
 */
public final class OMLPackets {
    public static final PacketType<ConfigSyncPacket> CONFIG_SYNC =
            NetworkCompat.registerS2C(OML.id("config_sync"), ConfigSyncPacket::write, ConfigSyncPacket::read);

    private OMLPackets() {
    }

    /** Loads the class, registering all packet types. */
    public static void init() {
    }
}
