package omtteam.openmodularlighting.registry;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.common.registry.GameRegistry;
import omtteam.openmodularlighting.OML;
import omtteam.openmodularlighting.tileentity.CarbonFloodlightTileEntity;
import omtteam.openmodularlighting.tileentity.CubeLightTileEntity;
import omtteam.openmodularlighting.tileentity.ElectricFloodlightTileEntity;
import omtteam.openmodularlighting.tileentity.FluorescentLightTileEntity;
import omtteam.openmodularlighting.tileentity.GrowLightTileEntity;
import omtteam.openmodularlighting.tileentity.PhantomLightTileEntity;
import omtteam.openmodularlighting.tileentity.UvFloodlightTileEntity;
import omtteam.openmodularlighting.tileentity.UvLightTileEntity;

/**
 * All block entity (tile entity) types of the mod, under the same ids as on the main branch. Their item and energy
 * access are capabilities of the block entities themselves.
 */
public final class ModTileEntities {
    private ModTileEntities() {
    }

    /** Registers all block entity classes; called from the block registry event. */
    public static void register() {
        register("phantom_light", PhantomLightTileEntity.class);
        register("uv_light", UvLightTileEntity.class);
        register("electric_floodlight", ElectricFloodlightTileEntity.class);
        register("carbon_floodlight", CarbonFloodlightTileEntity.class);
        register("uv_floodlight", UvFloodlightTileEntity.class);
        // both fluorescent lights share one block entity type (1.7.10: one block with two metadata values)
        register("fluorescent_light", FluorescentLightTileEntity.class);
        register("grow_light", GrowLightTileEntity.class);
        register("cube_light", CubeLightTileEntity.class);
    }

    private static void register(String name, Class<? extends TileEntity> type) {
        GameRegistry.registerTileEntity(type, OML.id(name));
    }
}
