package omtteam.openmodularlighting;

import net.minecraft.resources.ResourceLocation;
import omtteam.openmodularlighting.compat.VersionCompat;
import omtteam.openmodularlighting.config.OMLConfig;
import omtteam.openmodularlighting.network.OMLPackets;
import omtteam.openmodularlighting.registry.ModBlockEntities;
import omtteam.openmodularlighting.registry.ModBlocks;
import omtteam.openmodularlighting.registry.ModCreativeTabs;
import omtteam.openmodularlighting.registry.ModItems;
import omtteam.openmodularlighting.registry.ModMenus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Shared constants and start-up of Open Modular Lighting.
 * The loader entrypoints ({@code platform/<loader>/}) call {@link #init()} and wire their loader's events.
 */
public final class OML {
    public static final String MOD_ID = "openmodularlighting";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    /**
     * Creates a {@link ResourceLocation} in the mod namespace.
     */
    public static ResourceLocation id(String path) {
        return VersionCompat.id(MOD_ID, path);
    }

    private OML() {
    }

    /**
     * Loads the config and registers all content. Called once by the loader entrypoint.
     */
    public static void init() {
        LOGGER.info("Initializing Open Modular Lighting");
        OMLPackets.init();
        OMLConfig.init(); // before blocks: recipes and block properties come from the config
        ModBlocks.init();
        ModItems.init();
        ModBlockEntities.init();
        ModMenus.init();
        ModCreativeTabs.init();
    }
}
