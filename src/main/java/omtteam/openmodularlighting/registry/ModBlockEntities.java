package omtteam.openmodularlighting.registry;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.common.registry.GameRegistry;
import omtteam.openmodularlighting.OML;
import omtteam.openmodularlighting.blockentity.CarbonFloodlightBlockEntity;
import omtteam.openmodularlighting.blockentity.CubeLightBlockEntity;
import omtteam.openmodularlighting.blockentity.ElectricFloodlightBlockEntity;
import omtteam.openmodularlighting.blockentity.FluorescentLightBlockEntity;
import omtteam.openmodularlighting.blockentity.GrowLightBlockEntity;
import omtteam.openmodularlighting.blockentity.PhantomLightBlockEntity;
import omtteam.openmodularlighting.blockentity.UvFloodlightBlockEntity;
import omtteam.openmodularlighting.blockentity.UvLightBlockEntity;

/**
 * All block entity (tile entity) types of the mod, under the same ids as on the main branch. Their item and energy
 * access are capabilities of the block entities themselves.
 */
public final class ModBlockEntities {
    private ModBlockEntities() {
    }

    /** Registers all block entity classes; called from the block registry event. */
    public static void register() {
        register("phantom_light", PhantomLightBlockEntity.class);
        register("uv_light", UvLightBlockEntity.class);
        register("electric_floodlight", ElectricFloodlightBlockEntity.class);
        register("carbon_floodlight", CarbonFloodlightBlockEntity.class);
        register("uv_floodlight", UvFloodlightBlockEntity.class);
        // both fluorescent lights share one block entity type (1.7.10: one block with two metadata values)
        register("fluorescent_light", FluorescentLightBlockEntity.class);
        register("grow_light", GrowLightBlockEntity.class);
        register("cube_light", CubeLightBlockEntity.class);
    }

    private static void register(String name, Class<? extends TileEntity> type) {
        GameRegistry.registerTileEntity(type, OML.id(name));
    }
}
