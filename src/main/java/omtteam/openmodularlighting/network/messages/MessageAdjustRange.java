package omtteam.openmodularlighting.network.messages;

import io.netty.buffer.ByteBuf;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import omtteam.openmodularlighting.tileentity.lights.Floodlight;

@SuppressWarnings("unused")
public class MessageAdjustRange implements IMessage {
    private int x, y, z;
    private int range;

    public MessageAdjustRange() {
    }

    public static class MessageHandlerAdjustRange implements IMessageHandler<MessageAdjustRange, IMessage> {
        @Override
        public IMessage onMessage(MessageAdjustRange messageIn, MessageContext ctxIn) {
            final MessageAdjustRange message = messageIn;
            final MessageContext ctx = ctxIn;
            ((WorldServer) ctx.getServerHandler().playerEntity.getEntityWorld()).addScheduledTask(() -> {
                World world = ctx.getServerHandler().playerEntity.getEntityWorld();
                Floodlight light = (Floodlight) world.getTileEntity(new BlockPos(message.getX(), message.getY(), message.getZ()));
                if (light != null) {
                    light.setCurrentRange(message.getRange());
                }
            });
            return null;
        }
    }

    public MessageAdjustRange(int x, int y, int z, int range) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.range = range;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.x = buf.readInt();
        this.y = buf.readInt();
        this.z = buf.readInt();
        this.range = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.x);
        buf.writeInt(this.y);
        buf.writeInt(this.z);
        buf.writeInt(this.range);
    }

    private int getX() {
        return x;
    }

    private int getY() {
        return y;
    }

    private int getZ() {
        return z;
    }

    public int getRange() {
        return range;
    }
}
