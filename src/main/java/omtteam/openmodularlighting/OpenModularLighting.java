package omtteam.openmodularlighting;

import net.fabricmc.api.ModInitializer;
import omtteam.openmodularlighting.compat.BlockEnabledCondition;
import omtteam.openmodularlighting.compat.InteractiveBlock;
import omtteam.openmodularlighting.config.OMLConfig;
import omtteam.openmodularlighting.network.OMLPackets;
import omtteam.openmodularlighting.registry.ModBlockEntities;
import omtteam.openmodularlighting.registry.ModBlocks;
import omtteam.openmodularlighting.registry.ModCreativeTabs;
import omtteam.openmodularlighting.registry.ModItems;
import omtteam.openmodularlighting.registry.ModMenus;

/**
 * Common entrypoint of Open Modular Lighting.
 */
public class OpenModularLighting implements ModInitializer {
    @Override
    public void onInitialize() {
        OML.LOGGER.info("Initializing Open Modular Lighting");
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
