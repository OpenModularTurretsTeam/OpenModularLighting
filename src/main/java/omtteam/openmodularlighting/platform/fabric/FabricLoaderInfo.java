package omtteam.openmodularlighting.platform.fabric;

import net.fabricmc.loader.api.FabricLoader;
import omtteam.openmodularlighting.platform.LoaderInfo;

import java.nio.file.Path;

public final class FabricLoaderInfo implements LoaderInfo {
    @Override
    public Path configDir() {
        return FabricLoader.getInstance().getConfigDir();
    }

    @Override
    public boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }
}
