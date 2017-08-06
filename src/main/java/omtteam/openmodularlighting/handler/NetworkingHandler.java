package omtteam.openmodularlighting.handler;

import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import omtteam.openmodularlighting.network.messages.*;
import omtteam.openmodularlighting.reference.Reference;

public class NetworkingHandler {
    public final static SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(Reference.MOD_ID);

    public static void initNetworking() {

        INSTANCE.registerMessage(MessageAdjustRange.MessageHandlerAdjustRange.class,
                MessageAdjustRange.class, 0, Side.SERVER);

        INSTANCE.registerMessage(MessageDropLights.MessageHandlerDropLights.class, MessageDropLights.class, 1,
                Side.SERVER);

        INSTANCE.registerMessage(MessageDropBase.MessageHandlerDropBase.class, MessageDropBase.class,
                2, Side.SERVER);

        INSTANCE.registerMessage(MessageLightingBase.MessageHandlerTurretBase.class, MessageLightingBase.class, 3,
                Side.CLIENT);

        INSTANCE.registerMessage(MessageToggleMode.MessageHandlerToggleMode.class, MessageToggleMode.class, 4,
                Side.SERVER);
    }
}
