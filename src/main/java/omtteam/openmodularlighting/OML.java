package omtteam.openmodularlighting;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import omtteam.openmodularlighting.block.FloodlightInteraction;
import omtteam.openmodularlighting.config.OMLConfig;
import omtteam.openmodularlighting.menu.OMLGuiHandler;
import omtteam.openmodularlighting.network.OMLNetwork;
import omtteam.openmodularlighting.registry.ModRecipes;
import omtteam.openmodularlighting.registry.Registration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The Forge 1.12.2 entrypoint of Open Modular Lighting, and the mod's shared constants. Registration runs in Forge's
 * registry events ({@link Registration}); the client side is started through {@link CommonProxy}.
 */
@Mod(modid = OML.MOD_ID, name = OML.MOD_NAME, useMetadata = true, acceptedMinecraftVersions = "[1.12.2]")
public final class OML {
    public static final String MOD_ID = "openmodularlighting";
    public static final String MOD_NAME = "Open Modular Lighting";
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
     * Loads the config (block switches and recipes depend on it) and registers the packets and event handlers.
     * Forge fires the registry events after this.
     */
    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        LOGGER.info("Initializing Open Modular Lighting");
        OMLConfig.init(event.getModConfigurationDirectory().toPath());
        OMLNetwork.init();
        MinecraftForge.EVENT_BUS.register(Registration.class);
        MinecraftForge.EVENT_BUS.register(FloodlightInteraction.class);
        MinecraftForge.EVENT_BUS.register(OMLConfig.class);
        proxy.preInit();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        NetworkRegistry.INSTANCE.registerGuiHandler(this, new OMLGuiHandler());
        ModRecipes.registerSmelting();
        proxy.init();
    }
}
