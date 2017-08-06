package omtteam.openmodularlighting.network.messages;

import io.netty.buffer.ByteBuf;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import omtteam.openmodularlighting.tileentity.lights.AbstractLightSource;

@SuppressWarnings("unused")
public class MessageDropLights implements IMessage {
    private int x, y, z;

    public MessageDropLights() {
    }

    public static class MessageHandlerDropLights implements IMessageHandler<MessageDropLights, IMessage> {
        @Override
        public IMessage onMessage(MessageDropLights messageIn, MessageContext ctxIn) {
            final MessageDropLights message = messageIn;
            final MessageContext ctx = ctxIn;
            ((WorldServer) ctx.getServerHandler().playerEntity.getEntityWorld()).addScheduledTask(() -> {
                World world = ctx.getServerHandler().playerEntity.getEntityWorld();

                if (world.getTileEntity(new BlockPos(message.getX() + 1, message.getY(), message.getZ())) instanceof AbstractLightSource) {
                    world.destroyBlock(new BlockPos(message.getX() + 1, message.getY(), message.getZ()), true);
                }

                if (world.getTileEntity(new BlockPos(message.getX() - 1, message.getY(), message.getZ())) instanceof AbstractLightSource) {
                    world.destroyBlock(new BlockPos(message.getX() - 1, message.getY(), message.getZ()), true);
                }

                if (world.getTileEntity(new BlockPos(message.getX(), message.getY() + 1, message.getZ())) instanceof AbstractLightSource) {
                    world.destroyBlock(new BlockPos(message.getX(), message.getY() + 1, message.getZ()), true);
                }

                if (world.getTileEntity(new BlockPos(message.getX(), message.getY() - 1, message.getZ())) instanceof AbstractLightSource) {
                    world.destroyBlock(new BlockPos(message.getX(), message.getY() - 1, message.getZ()), true);
                }

                if (world.getTileEntity(new BlockPos(message.getX(), message.getY(), message.getZ() + 1)) instanceof AbstractLightSource) {
                    world.destroyBlock(new BlockPos(message.getX(), message.getY(), message.getZ() + 1), true);
                }

                if (world.getTileEntity(new BlockPos(message.getX(), message.getY(), message.getZ() - 1)) instanceof AbstractLightSource) {
                    world.destroyBlock(new BlockPos(message.getX(), message.getY(), message.getZ() - 1), true);
                }
            });
            return null;
        }
    }

    public MessageDropLights(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.x = buf.readInt();
        this.y = buf.readInt();
        this.z = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.x);
        buf.writeInt(this.y);
        buf.writeInt(this.z);
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
}
