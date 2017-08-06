package omtteam.openmodularlighting;


import net.minecraft.creativetab.CreativeTabs;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import omtteam.openmodularlighting.client.gui.ModularLightingTab;
import omtteam.openmodularlighting.compatibility.ModCompatibility;
import omtteam.openmodularlighting.handler.ConfigHandler;
import omtteam.openmodularlighting.handler.GuiHandler;
import omtteam.openmodularlighting.proxy.CommonProxy;
import omtteam.openmodularlighting.reference.Reference;
import org.apache.logging.log4j.Logger;

@Mod(modid = Reference.MOD_ID, name = Reference.NAME, version = Reference.VERSION, acceptedMinecraftVersions = Reference.ACCEPTED_MINECRAFT_VERSION, dependencies = Reference.DEPENDENCIES)
public class OpenModularLighting {
    @SuppressWarnings("unused")
    @Mod.Instance(Reference.MOD_ID)
    public static OpenModularLighting instance;

    @SuppressWarnings({"CanBeFinal", "unused"})
    @SidedProxy(clientSide = "omtteam.openmodularlighting.proxy.ClientProxy", serverSide = "omtteam.openmodularlighting.proxy.ServerProxy")
    private static CommonProxy proxy;

    public static final CreativeTabs creativeTab = ModularLightingTab.getInstance();
    private static Logger logger;

    public static Logger getLogger() {
        return logger;
    }

    @SuppressWarnings("unused")
    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        logger = event.getModLog();
        ConfigHandler.init(event.getSuggestedConfigurationFile());
        proxy.preInit();
        ModCompatibility.preinit();
        NetworkRegistry.INSTANCE.registerGuiHandler(this, GuiHandler.getInstance());
    }

    @SuppressWarnings("unused")
    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        ModCompatibility.init();
        proxy.init();
    }

    @SuppressWarnings("unused")
    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        ConfigHandler.parseLists();
    }
}