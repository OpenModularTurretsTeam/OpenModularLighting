package omtteam.openmodularlighting.registry;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import omtteam.openmodularlighting.OML;
import omtteam.openmodularlighting.blockentity.CarbonFloodlightBlockEntity;
import omtteam.openmodularlighting.blockentity.ElectricFloodlightBlockEntity;
import omtteam.openmodularlighting.blockentity.FloodlightBlockEntity;
import omtteam.openmodularlighting.blockentity.TransferHost;
import omtteam.openmodularlighting.blockentity.CubeLightBlockEntity;
import omtteam.openmodularlighting.blockentity.FluorescentLightBlockEntity;
import omtteam.openmodularlighting.blockentity.GrowLightBlockEntity;
import omtteam.openmodularlighting.blockentity.PhantomLightBlockEntity;
import omtteam.openmodularlighting.blockentity.UvFloodlightBlockEntity;
import omtteam.openmodularlighting.blockentity.UvLightBlockEntity;
import omtteam.openmodularlighting.compat.VersionCompat;

import java.util.List;
import java.util.function.BiFunction;

/**
 * All block entity types of the mod. Their item and energy access is registered per loader ({@code platform/<loader>}).
 */
public final class ModBlockEntities {
    public static final BlockEntityType<PhantomLightBlockEntity> PHANTOM_LIGHT =
            register("phantom_light", PhantomLightBlockEntity::new, ModBlocks.PHANTOM_LIGHT);
    public static final BlockEntityType<UvLightBlockEntity> UV_LIGHT =
            register("uv_light", UvLightBlockEntity::new, ModBlocks.UV_LIGHT);

    public static final BlockEntityType<ElectricFloodlightBlockEntity> ELECTRIC_FLOODLIGHT =
            register("electric_floodlight", ElectricFloodlightBlockEntity::new, ModBlocks.ELECTRIC_FLOODLIGHT);
    public static final BlockEntityType<CarbonFloodlightBlockEntity> CARBON_FLOODLIGHT =
            register("carbon_floodlight", CarbonFloodlightBlockEntity::new, ModBlocks.CARBON_FLOODLIGHT);
    public static final BlockEntityType<UvFloodlightBlockEntity> UV_FLOODLIGHT =
            register("uv_floodlight", UvFloodlightBlockEntity::new, ModBlocks.UV_FLOODLIGHT);
    /** Both fluorescent lights share one block entity type (1.7.10: one block with two metadata values). */
    public static final BlockEntityType<FluorescentLightBlockEntity> FLUORESCENT_LIGHT = register("fluorescent_light",
            FluorescentLightBlockEntity::new, ModBlocks.SMALL_FLUORESCENT_LIGHT, ModBlocks.SQUARE_FLUORESCENT_LIGHT);
    public static final BlockEntityType<GrowLightBlockEntity> GROW_LIGHT =
            register("grow_light", GrowLightBlockEntity::new, ModBlocks.GROW_LIGHT);

    public static final BlockEntityType<CubeLightBlockEntity> CUBE_LIGHT =
            register("cube_light", CubeLightBlockEntity::new, ModBlocks.CUBE_LIGHT);

    private ModBlockEntities() {
    }

    private static <T extends BlockEntity> BlockEntityType<T> register(String name,
                                                                       BiFunction<BlockPos, BlockState, T> factory,
                                                                       Block... blocks) {
        return Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, OML.id(name),
                VersionCompat.blockEntityType(factory, blocks));
    }

    /** Loads the class, registering all block entity types. */
    public static void init() {
    }

    /** The block entities whose items and energy other blocks may use (the lights); each loader registers them. */
    public static List<BlockEntityType<? extends TransferHost>> transferTypes() {
        return List.of(ELECTRIC_FLOODLIGHT, CARBON_FLOODLIGHT, UV_FLOODLIGHT, FLUORESCENT_LIGHT, GROW_LIGHT, CUBE_LIGHT);
    }
}
