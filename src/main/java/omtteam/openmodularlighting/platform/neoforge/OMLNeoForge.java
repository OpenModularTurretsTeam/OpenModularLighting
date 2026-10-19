package omtteam.openmodularlighting.platform.neoforge;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import omtteam.openmodularlighting.platform.Platform;
import omtteam.openmodularlighting.platform.PlatformServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * NeoForge entrypoint (the Fabric one is {@code OML}).
 * Porting in progress: nothing is registered yet.
 */
@Mod("openmodularlighting")
public final class OMLNeoForge {
    private static final Logger LOGGER = LoggerFactory.getLogger("openmodularlighting");

    public OMLNeoForge(IEventBus modBus) {
        Platform.install(new PlatformServices(new NeoForgeLoaderInfo(), new NeoForgeFuel(), new NeoForgeNetwork(modBus),
                new NeoForgeMenus(), new NeoForgeEnergy()));
        LOGGER.info("Open Modular Lighting on NeoForge: port in progress, no content registered yet");
    }
}
