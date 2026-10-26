package omtteam.openmodularlighting.platform.fabric;

import omtteam.omlib.platform.fabric.FabricBlockLookups;
import net.fabricmc.api.ModInitializer;
import omtteam.openmodularlighting.OML;
import omtteam.openmodularlighting.registry.ModBlockEntities;

/**
 * Fabric entrypoint: installs the platform services, starts the mod and wires Fabric's callbacks to the shared code.
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
