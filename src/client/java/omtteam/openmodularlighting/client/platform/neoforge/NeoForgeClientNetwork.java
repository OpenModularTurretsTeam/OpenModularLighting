package omtteam.openmodularlighting.client.platform.neoforge;

//? if >=1.21.11 {
/*import net.neoforged.neoforge.client.network.ClientPacketDistributor;
*///?} else {
import net.neoforged.neoforge.network.PacketDistributor;
//?}
import omtteam.openmodularlighting.client.platform.ClientNetwork;
import omtteam.openmodularlighting.compat.network.PacketType;
import omtteam.openmodularlighting.platform.neoforge.NeoForgeNetwork;

import java.util.function.Consumer;

/**
 * Client handlers are passed to {@link NeoForgeNetwork}, which registered the payloads with a dispatcher.
 */
public final class NeoForgeClientNetwork implements ClientNetwork {
    private final NeoForgeNetwork network;

    public NeoForgeClientNetwork(NeoForgeNetwork network) {
        this.network = network;
    }

    @Override
    public <T> void registerHandler(PacketType<T> type, Consumer<T> handler) {
        network.setClientHandler(type, handler);
    }

    @Override
    public <T> void sendToServer(PacketType<T> type, T packet) {
        //? if >=1.21.11 {
        /*ClientPacketDistributor.sendToServer(type.wrap(packet));
        *///?} else {
        PacketDistributor.sendToServer(type.wrap(packet));
        //?}
    }
}
