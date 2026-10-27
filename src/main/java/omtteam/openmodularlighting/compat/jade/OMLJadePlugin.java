package omtteam.openmodularlighting.compat.jade;

import omtteam.openmodularlighting.blockentity.FloodlightBlockEntity;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

/**
 * Jade plugin, only loaded if Jade is installed: Fabric Jade finds it through the {@code jade} entrypoint in
 * fabric.mod.json, NeoForge and Forge Jade through {@link WailaPlugin} (Fabric has no annotation scanning, so it is not
 * registered twice). Replaces the 1.7.10 WAILA handler.
 * <p>
 * The server half is here; the tooltip is in {@code client.compat.jade.OMLJadeClientPlugin}, because Jade's tooltip
 * classes are client classes (from Jade 21 on 1.21.11, {@code ITooltip} extends one), which main cannot see.
 * Jade creates every plugin it finds on a dedicated server too, so the client half is not one of them: this plugin
 * loads it by name in {@link #registerClient}, which Jade calls only on a client.
 */
@WailaPlugin
public class OMLJadePlugin implements IWailaPlugin {
    private static final String CLIENT_PLUGIN = "omtteam.openmodularlighting.client.compat.jade.OMLJadeClientPlugin";

    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(FloodlightJadeProvider.INSTANCE, FloodlightBlockEntity.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        try {
            Class.forName(CLIENT_PLUGIN).asSubclass(IWailaPlugin.class).getDeclaredConstructor().newInstance()
                    .registerClient(registration);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Could not create the Jade client plugin " + CLIENT_PLUGIN, e);
        }
    }
}
