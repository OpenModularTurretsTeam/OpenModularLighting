package omtteam.openmodularlighting.network;

import omtteam.openmodularlighting.OML;
import omtteam.openmodularlighting.compat.network.NetworkCompat;
import omtteam.openmodularlighting.compat.network.PacketType;

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
