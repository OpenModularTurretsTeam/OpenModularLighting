package omtteam.openmodularlighting.platform.fabric;

import net.fabricmc.api.ModInitializer;
import omtteam.openmodularlighting.OML;
import omtteam.openmodularlighting.compat.BlockEnabledCondition;
import omtteam.openmodularlighting.compat.InteractiveBlock;
import omtteam.openmodularlighting.platform.Platform;
import omtteam.openmodularlighting.platform.PlatformServices;

/**
 * Fabric entrypoint: starts the mod and wires Fabric's callbacks to the shared code.
 */
public final class OMLFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        Platform.install(new PlatformServices(new FabricLoaderInfo(), new FabricFuel(), new FabricNetwork(),
                new FabricMenus(), new FabricEnergy()));
        OML.init();
        FabricBlockLookups.register();
        BlockEnabledCondition.register();
        InteractiveBlock.registerCallback();
    }
}
