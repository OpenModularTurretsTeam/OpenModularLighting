package omtteam.openmodularlighting.init;

import net.minecraft.block.Block;
import net.minecraftforge.fml.common.registry.GameRegistry;
import omtteam.openmodularlighting.blocks.BlockFloodlight;
import omtteam.openmodularlighting.blocks.BlockLightingBase;
import omtteam.openmodularlighting.reference.OMLNames;
import omtteam.openmodularlighting.tileentity.LightingBase;
import omtteam.openmodularlighting.tileentity.lights.Floodlight;

import static omtteam.omlib.util.InitHelper.registerBlock;


public class ModBlocks {
    public static Block lightingBase;

    public static Block floodlight;


    public static void initBlocks() {
        lightingBase = registerBlock(new BlockLightingBase());
        floodlight = registerBlock(new BlockFloodlight());
    }

    public static void initTileEntities() {
        GameRegistry.registerTileEntity(LightingBase.class, OMLNames.Blocks.lightingBase);

        GameRegistry.registerTileEntity(Floodlight.class, OMLNames.Blocks.floodlight);
    }
}
