package omtteam.openmodularlighting.network.messages;


import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import omtteam.omlib.power.OMEnergyStorage;
import omtteam.omlib.tileentity.EnumMachineMode;
import omtteam.openmodularlighting.tileentity.LightingBase;

import static omtteam.omlib.proxy.ClientProxy.getWorld;


/**
 * Created by Keridos on 05.10.14.
 * This Class is the Message that the electric floodlights TileEntity uses.
 */
@SuppressWarnings("unused")
public class MessageLightingBase implements IMessage {
    private int x, y, z, rfStorageCurrent, rfStorageMax, tier, camoBlockMeta;
    private String owner, ownerName, camoBlockRegName;
    private EnumMachineMode mode;

    public MessageLightingBase() {
    }


    public static class MessageHandlerTurretBase implements IMessageHandler<MessageLightingBase, IMessage> {
        @Override
        @SideOnly(Side.CLIENT)
        @SuppressWarnings("deprecation")
        public IMessage onMessage(MessageLightingBase messageIn, MessageContext ctx) {
            final MessageLightingBase message = messageIn;
            Minecraft.getMinecraft().addScheduledTask(() -> {

                TileEntity tileEntity = getWorld(FMLClientHandler.instance().getClient()).getTileEntity(new BlockPos(message.x, message.y,
                        message.z));
                if (tileEntity instanceof LightingBase) {
                    LightingBase base = (LightingBase) tileEntity;
                    OMEnergyStorage storage = (OMEnergyStorage) base.getCapability(CapabilityEnergy.ENERGY, EnumFacing.DOWN);
                    base.setOwner(message.owner);
                    base.setOwnerName(message.ownerName);
                    if (storage != null) {
                        storage.setEnergyStored(message.rfStorageCurrent);
                        storage.setCapacity(message.rfStorageMax);
                    }

                    base.setTier(message.tier);
                    base.setMode(message.mode);
                    base.setCamoState(ForgeRegistries.BLOCKS.getValue(
                            new ResourceLocation(message.camoBlockRegName)).getStateFromMeta(message.camoBlockMeta));

                }
            });
            return null;
        }

    }

    public MessageLightingBase(TileEntity tileEntity) {
        if (tileEntity instanceof LightingBase) {
            LightingBase base = (LightingBase) tileEntity;
            this.x = base.getPos().getX();
            this.y = base.getPos().getY();
            this.z = base.getPos().getZ();
            this.tier = base.getTier();
            this.owner = base.getOwner();
            this.ownerName = base.getOwnerName();
            this.rfStorageCurrent = base.getEnergyLevel(EnumFacing.DOWN);
            this.rfStorageMax = base.getMaxEnergyLevel(EnumFacing.DOWN);
            this.camoBlockRegName = base.getCamoState().getBlock().getRegistryName().toString();
            this.camoBlockMeta = base.getCamoState().getBlock().getMetaFromState(base.getCamoState());
            this.mode = base.getMode();
        }
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.x = buf.readInt();
        this.y = buf.readInt();
        this.z = buf.readInt();
        this.tier = buf.readInt();
        int ownerLength = buf.readInt();
        this.owner = new String(buf.readBytes(ownerLength).array());
        int ownerNameLength = buf.readInt();
        this.ownerName = new String(buf.readBytes(ownerNameLength).array());
        this.rfStorageCurrent = buf.readInt();
        this.rfStorageMax = buf.readInt();
        this.mode = EnumMachineMode.values()[buf.readInt()];
        int camoBlockRegNameLength = buf.readInt();
        this.camoBlockRegName = new String(buf.readBytes(camoBlockRegNameLength).array());
        this.camoBlockMeta = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        buf.writeInt(tier);
        buf.writeInt(owner.length());
        buf.writeBytes(owner.getBytes());
        buf.writeInt(ownerName.length());
        buf.writeBytes(ownerName.getBytes());
        buf.writeInt(rfStorageCurrent);
        buf.writeInt(rfStorageMax);
        buf.writeInt(mode.ordinal());
        buf.writeInt(camoBlockRegName.length());
        buf.writeBytes(camoBlockRegName.getBytes());
        buf.writeInt(camoBlockMeta);
    }

    @Override
    public String toString() {
        return String.format(
                "MessageLightingBase - x:%s, y:%s, z:%s, owner:%s, rfstorage:%s", x, y, z, owner, rfStorageCurrent);
    }
}

