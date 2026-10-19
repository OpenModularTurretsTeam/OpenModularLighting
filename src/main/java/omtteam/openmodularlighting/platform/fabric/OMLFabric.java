package omtteam.openmodularlighting.platform.fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import omtteam.openmodularlighting.OML;
import omtteam.openmodularlighting.compat.InteractiveBlock;
import omtteam.openmodularlighting.config.OMLConfig;
import omtteam.openmodularlighting.platform.Platform;
import omtteam.openmodularlighting.platform.PlatformServices;

/**
 * Fabric entrypoint: installs the platform services, starts the mod and wires Fabric's callbacks to the shared code.
 */
public final class OMLFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        Platform.install(new PlatformServices(new FabricLoaderInfo(), new FabricFuel(), new FabricNetwork(),
                new FabricMenus(), new FabricEnergy()));
        OML.init();
        OML.registrationSteps().forEach(step -> step.register().run());
        FabricBlockLookups.register();
        FabricBlockEnabledCondition.register();
        UseBlockCallback.EVENT.register(InteractiveBlock::handleUse);
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> OMLConfig.onPlayerJoin(handler.getPlayer()));
    }
}
