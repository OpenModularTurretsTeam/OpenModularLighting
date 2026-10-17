package omtteam.openmodularlighting;

import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.ResourceLocation;
import omtteam.openmodularlighting.compat.BlockEnabledCondition;
import omtteam.openmodularlighting.compat.InteractiveBlock;
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
 * Common entrypoint of Open Modular Lighting, and its shared constants.
 */
public final class OML implements ModInitializer {
    public static final String MOD_ID = "openmodularlighting";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    /**
     * Creates a {@link ResourceLocation} in the mod namespace.
     */
    public static ResourceLocation id(String path) {
        return VersionCompat.id(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Open Modular Lighting");
        OMLPackets.init();
        OMLConfig.init(); // before blocks: recipes and block properties come from the config
        ModBlocks.init();
        ModItems.init();
        ModBlockEntities.init();
        ModMenus.init();
        ModCreativeTabs.init();
        BlockEnabledCondition.register();
        InteractiveBlock.registerCallback();
    }
}
