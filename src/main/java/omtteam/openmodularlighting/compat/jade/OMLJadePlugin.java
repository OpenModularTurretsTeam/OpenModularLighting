package omtteam.openmodularlighting.compat.jade;

import omtteam.openmodularlighting.block.FloodlightBlock;
import omtteam.openmodularlighting.blockentity.FloodlightBlockEntity;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

/**
 * Jade plugin, only loaded if Jade is installed: Fabric Jade finds it through the {@code jade} entrypoint in
 * fabric.mod.json, NeoForge Jade through {@link WailaPlugin} (Fabric has no annotation scanning, so it is not registered
 * twice). Replaces the 1.7.10 WAILA handler.
 */
@WailaPlugin
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
