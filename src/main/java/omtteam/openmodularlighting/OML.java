package omtteam.openmodularlighting;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import omtteam.omlib.config.ConfigSync;
import omtteam.openmodularlighting.block.FloodlightInteraction;
import omtteam.openmodularlighting.config.OMLConfig;
import omtteam.openmodularlighting.handler.OMLGuiHandler;
import omtteam.openmodularlighting.registry.ModRecipes;
import omtteam.openmodularlighting.registry.Registration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The Forge 1.12.2 entrypoint of Open Modular Lighting, and the mod's shared constants. Registration runs in Forge's
 * registry events ({@link Registration}); the client side is started through {@link CommonProxy}.
 */
@Mod(modid = OML.MOD_ID, name = OML.MOD_NAME, useMetadata = true, acceptedMinecraftVersions = "[1.12.2]",
        dependencies = OML.DEPENDENCIES)
public final class OML {
    public static final String MOD_ID = "openmodularlighting";
    public static final String MOD_NAME = "Open Modular Lighting";
    /**
     * OMLib is required. The standalone build replaces the range with {@code [<omlib_version>,<next major>.0.0-alpha)};
     * dev runs keep {@code [0.0.0,)}, because OMLib's version stays unreplaced there.
     */
    public static final String DEPENDENCIES = "required-after:omlib@[0.0.0,)";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    @Mod.Instance(MOD_ID)
    public static OML instance;

    @SidedProxy(clientSide = "omtteam.openmodularlighting.client.ClientProxy",
            serverSide = "omtteam.openmodularlighting.CommonProxy")
    public static CommonProxy proxy;

    /**
     * Creates a {@link ResourceLocation} in the mod namespace.
     */
    public static ResourceLocation id(String path) {
        return new ResourceLocation(MOD_ID, path);
    }

    /**
     * Loads the config (block switches and recipes depend on it), registers it with OMLib's config sync and registers
     * the event handlers.
     * Forge fires the registry events after this.
     */
    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        LOGGER.info("Initializing Open Modular Lighting");
        OMLConfig.init(event.getModConfigurationDirectory().toPath());
        // OMLib sends the server's config on login and puts the local one back after disconnecting
        ConfigSync.register(MOD_ID);
        ConfigSync.registerValue(MOD_ID, OMLConfig.FILE_NAME, String.class, OMLConfig::toJson,
                OMLConfig::applyServerConfig);
        MinecraftForge.EVENT_BUS.register(Registration.class);
        MinecraftForge.EVENT_BUS.register(FloodlightInteraction.class);
        proxy.preInit();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        NetworkRegistry.INSTANCE.registerGuiHandler(this, new OMLGuiHandler());
        ModRecipes.registerSmelting();
        proxy.init();
    }
}
