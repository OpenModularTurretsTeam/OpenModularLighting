package omtteam.openmodularlighting.platform.fabric;

import omtteam.omlib.platform.fabric.FabricBlockLookups;
import net.fabricmc.api.ModInitializer;
import omtteam.openmodularlighting.OML;
import omtteam.openmodularlighting.registry.ModBlockEntities;

/**
 * Fabric entrypoint: starts the mod and wires Fabric's callbacks to the shared code. OMLib creates the platform
 * services on first use and registers block use and the config sync itself.
 */
public final class OMLFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        OML.init();
        OML.registrationSteps().forEach(step -> step.register().run());
        FabricBlockLookups.register(ModBlockEntities.transferTypes());
        FabricBlockEnabledCondition.register();
    }
}
