package omtteam.openmodularlighting.compat.jade;

import omtteam.openmodularlighting.blockentity.FloodlightBlockEntity;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

/**
 * Jade plugin, only loaded if Jade is installed: Fabric Jade finds it through the {@code jade} entrypoint in
 * fabric.mod.json, NeoForge and Forge Jade through {@link WailaPlugin} (Fabric has no annotation scanning, so it is not
 * registered twice). Replaces the 1.7.10 WAILA handler.
 * <p>
 * Only the server half; the tooltip is registered by {@code client.compat.jade.OMLJadeClientPlugin}, because Jade's
 * tooltip classes are client classes (from Jade 21 on 1.21.11, {@code ITooltip} extends one), which main cannot see.
 */
@WailaPlugin
public class OMLJadePlugin implements IWailaPlugin {
    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(FloodlightJadeProvider.INSTANCE, FloodlightBlockEntity.class);
    }
}
