package omtteam.openmodularlighting.compat.jade;

import omtteam.openmodularlighting.block.FloodlightBlock;
import omtteam.openmodularlighting.blockentity.FloodlightBlockEntity;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;

/**
 * Jade plugin (fabric.mod.json entrypoint {@code jade}; only loaded if Jade is installed). Replaces the 1.7.10 WAILA
 * handler.
 */
public class OMLJadePlugin implements IWailaPlugin {
    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(FloodlightJadeProvider.INSTANCE, FloodlightBlockEntity.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(FloodlightJadeProvider.INSTANCE, FloodlightBlock.class);
    }
}
