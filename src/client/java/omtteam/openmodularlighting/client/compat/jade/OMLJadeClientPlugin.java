package omtteam.openmodularlighting.client.compat.jade;

import omtteam.openmodularlighting.block.FloodlightBlock;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaPlugin;

/**
 * Client half of the Jade plugin ({@code compat.jade.OMLJadePlugin} has the server half). Not registered with Jade
 * itself (no {@code jade} entrypoint, no {@code @WailaPlugin}): Jade creates every plugin it finds on a dedicated server
 * too, where this class cannot be loaded. {@code OMLJadePlugin.registerClient} creates it by name on a client and
 * forwards to {@link #registerClient}.
 */
public class OMLJadeClientPlugin implements IWailaPlugin {
    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(FloodlightJadeComponent.INSTANCE, FloodlightBlock.class);
    }
}
