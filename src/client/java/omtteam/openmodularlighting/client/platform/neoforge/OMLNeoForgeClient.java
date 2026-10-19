package omtteam.openmodularlighting.client.platform.neoforge;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import omtteam.openmodularlighting.client.platform.ClientPlatform;
import omtteam.openmodularlighting.client.platform.ClientPlatformServices;
import omtteam.openmodularlighting.platform.Platform;
import omtteam.openmodularlighting.platform.neoforge.NeoForgeNetwork;

/**
 * NeoForge client entrypoint. FML constructs it after the common {@code OMLNeoForge} (entrypoints for more sides
 * first), so {@link Platform} is already installed.
 * Porting in progress: nothing client-side is wired yet.
 */
@Mod(value = "openmodularlighting", dist = Dist.CLIENT)
public final class OMLNeoForgeClient {
    public OMLNeoForgeClient() {
        ClientPlatform.install(new ClientPlatformServices(new NeoForgeClientNetwork((NeoForgeNetwork) Platform.network())));
    }
}
