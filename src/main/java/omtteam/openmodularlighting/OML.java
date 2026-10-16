package omtteam.openmodularlighting;

import net.minecraft.resources.ResourceLocation;
import omtteam.openmodularlighting.compat.VersionCompat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Shared constants of Open Modular Lighting.
 */
public final class OML {
    public static final String MOD_ID = "openmodularlighting";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private OML() {
    }

    /**
     * Creates a {@link ResourceLocation} in the mod namespace.
     */
    public static ResourceLocation id(String path) {
        return VersionCompat.id(MOD_ID, path);
    }
}
