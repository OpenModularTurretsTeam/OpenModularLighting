package omtteam.openmodularlighting.proxy;

import net.minecraftforge.common.MinecraftForge;
import omtteam.openmodularlighting.handler.EventHandler;
import omtteam.openmodularlighting.handler.NetworkingHandler;
import omtteam.openmodularlighting.handler.recipes.RecipeHandler;
import omtteam.openmodularlighting.init.ModBlocks;
import omtteam.openmodularlighting.init.ModItems;
import omtteam.openmodularlighting.init.ModSounds;

public class CommonProxy {
    public void preInit() {
        ModItems.init();
        ModBlocks.initBlocks();
        ModBlocks.initTileEntities();
        ModSounds.init();
        initTileRenderers();
        initHandlers();
    }

    protected void initTileRenderers() {

    }

    protected void initEntityRenderers() {

    }

    protected void initHandlers() {
        NetworkingHandler.initNetworking();
    }

    public void init() {
        RecipeHandler.initRecipes();
        initEntityRenderers();
        MinecraftForge.EVENT_BUS.register(EventHandler.getInstance());

    }
}