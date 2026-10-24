package omtteam.openmodularlighting.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import omtteam.openmodularlighting.config.OMLConfig;

/**
 * S2C: the server's config as JSON, sent on join.
 */
public final class ConfigSyncPacket implements IMessage {
    private static final int MAX_LENGTH = Short.MAX_VALUE;

    private String json;

    /** For Forge, which creates the packet before reading it. */
    public ConfigSyncPacket() {
    }

    public ConfigSyncPacket(String json) {
        this.json = json;
    }

    public String json() {
        return json;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        new PacketBuffer(buf).writeString(json);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        json = new PacketBuffer(buf).readString(MAX_LENGTH);
    }

    /** Client side: switching the config in effect is a single volatile write, so it runs on the network thread. */
    public static final class Handler implements IMessageHandler<ConfigSyncPacket, IMessage> {
        @Override
        public IMessage onMessage(ConfigSyncPacket message, MessageContext ctx) {
            OMLConfig.applyServerConfig(message.json);
            return null;
        }
    }
}
