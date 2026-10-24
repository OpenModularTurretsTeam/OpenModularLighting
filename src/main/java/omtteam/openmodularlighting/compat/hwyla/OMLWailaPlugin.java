package omtteam.openmodularlighting.compat.hwyla;

import mcp.mobius.waila.api.IWailaPlugin;
import mcp.mobius.waila.api.IWailaRegistrar;
import mcp.mobius.waila.api.WailaPlugin;
import omtteam.openmodularlighting.OML;
import omtteam.openmodularlighting.blockentity.FloodlightBlockEntity;

/**
 * Hwyla plugin (the 1.12.2 counterpart of the main branch's {@code OMLJadePlugin}), found by Hwyla through
 * {@link WailaPlugin} and only loaded if Hwyla is installed. Replaces the 1.7.10 WAILA handler.
 */
@WailaPlugin
public class OMLWailaPlugin implements IWailaPlugin {
    @Override
    public void register(IWailaRegistrar registrar) {
        registrar.addConfig(OML.MOD_NAME, FloodlightWailaProvider.CONFIG_KEY, true);
        registrar.registerNBTProvider(FloodlightWailaProvider.INSTANCE, FloodlightBlockEntity.class);
        registrar.registerBodyProvider(FloodlightWailaProvider.INSTANCE, FloodlightBlockEntity.class);
    }
}
