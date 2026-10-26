package omtteam.openmodularlighting;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import omtteam.omlib.compat.VersionCompat;
import omtteam.openmodularlighting.config.OMLConfig;
import omtteam.openmodularlighting.network.OMLPackets;
import omtteam.openmodularlighting.registry.ModBlockEntities;
import omtteam.openmodularlighting.registry.ModBlocks;
import omtteam.openmodularlighting.registry.ModCreativeTabs;
import omtteam.openmodularlighting.registry.ModItems;
import omtteam.openmodularlighting.registry.ModMenus;
import omtteam.omlib.registry.RegistrationStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Shared constants and start-up of Open Modular Lighting.
 * The loader entrypoints ({@code platform/<loader>/}) call {@link #init()}, run the {@link #registrationSteps()} and
 * wire their loader's events.
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
     * Loads the config and registers the packets. Both entrypoints call it first, before any registration (recipes and
     * block properties come from the config; NeoForge accepts packets only until its payload registration event).
     */
    public static void init() {
        LOGGER.info("Initializing Open Modular Lighting");
        OMLPackets.init();
        OMLConfig.init();
    }

    /**
     * The registration steps in dependency order, each with the registry it fills. Fabric runs them in this order;
     * NeoForge runs each in the {@code RegisterEvent} of its registry.
     */
    public static List<RegistrationStep> registrationSteps() {
        return List.of(
                new RegistrationStep(Registries.BLOCK, ModBlocks::init),
                new RegistrationStep(Registries.ITEM, ModItems::init),
                new RegistrationStep(Registries.BLOCK_ENTITY_TYPE, ModBlockEntities::init),
                new RegistrationStep(Registries.MENU, ModMenus::init),
                new RegistrationStep(Registries.CREATIVE_MODE_TAB, ModCreativeTabs::init));
    }
}
