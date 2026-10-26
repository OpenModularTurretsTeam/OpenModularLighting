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

    // Camouflage menu
    public static final PacketType<CamoPackets.SetLight> CAMO_LIGHT = NetworkCompat.registerC2S(
            OML.id("camo_light"), CamoPackets.SetLight::write, CamoPackets.SetLight::read,
            CamoPacketHandler::handleSetLight);

    public static final PacketType<CamoPackets.OpenMenu> OPEN_CAMO_MENU = NetworkCompat.registerC2S(
            OML.id("open_camo_menu"), CamoPackets.OpenMenu::write, CamoPackets.OpenMenu::read,
            CamoPacketHandler::handleOpenMenu);

    public static final PacketType<CamoPackets.Back> CAMO_BACK = NetworkCompat.registerC2S(
            OML.id("camo_back"), CamoPackets.Back::write, CamoPackets.Back::read,
            CamoPacketHandler::handleBack);

    private OMLPackets() {
    }

    /** Loads the class, registering all packet types. */
    public static void init() {
    }
}
