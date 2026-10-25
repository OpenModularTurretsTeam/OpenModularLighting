package omtteam.openmodularlighting.client.compat.jade;

import omtteam.openmodularlighting.block.FloodlightBlock;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

/**
 * Client half of the Jade plugin ({@code compat.jade.OMLJadePlugin} has the server half), found the same way: the
 * {@code jade} entrypoint on Fabric, {@link WailaPlugin} on NeoForge and Forge. Jade also creates it on a dedicated
 * server, but calls {@link #registerClient} only on a client, so no client class is loaded there.
 */
@WailaPlugin
public class OMLJadeClientPlugin implements IWailaPlugin {
    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(FloodlightJadeComponent.INSTANCE, FloodlightBlock.class);
    }
}
