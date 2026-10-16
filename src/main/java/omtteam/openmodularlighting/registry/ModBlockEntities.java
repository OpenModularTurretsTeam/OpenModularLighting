package omtteam.openmodularlighting.registry;

import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import omtteam.openmodularlighting.OML;
import omtteam.openmodularlighting.blockentity.CarbonFloodlightBlockEntity;
import omtteam.openmodularlighting.blockentity.ElectricFloodlightBlockEntity;
import omtteam.openmodularlighting.blockentity.FloodlightBlockEntity;
import omtteam.openmodularlighting.blockentity.FluorescentLightBlockEntity;
import omtteam.openmodularlighting.blockentity.GrowLightBlockEntity;
import omtteam.openmodularlighting.blockentity.PhantomLightBlockEntity;
import omtteam.openmodularlighting.blockentity.UvFloodlightBlockEntity;
import omtteam.openmodularlighting.blockentity.UvLightBlockEntity;
import team.reborn.energy.api.EnergyStorage;

import java.util.List;

/**
 * All block entity types of the mod and their API lookups (items via Transfer API, energy via Team Reborn Energy).
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

    private ModBlockEntities() {
    }

    private static <T extends BlockEntity> BlockEntityType<T> register(String name,
                                                                       BlockEntityType.BlockEntitySupplier<T> factory,
                                                                       Block... blocks) {
        return Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, OML.id(name),
                BlockEntityType.Builder.of(factory, blocks).build(null));
    }

    public static void init() {
        // Items on all sides (1.7.10); energy on all sides or only from the back (FloodlightBlockEntity#acceptsEnergyFrom)
        for (BlockEntityType<? extends FloodlightBlockEntity> type : List.of(ELECTRIC_FLOODLIGHT, CARBON_FLOODLIGHT,
                UV_FLOODLIGHT, FLUORESCENT_LIGHT, GROW_LIGHT)) {
            ItemStorage.SIDED.registerForBlockEntity((light, side) -> light.getItemStorage(), type);
            EnergyStorage.SIDED.registerForBlockEntity(FloodlightBlockEntity::getEnergyStorage, type);
        }
    }
}
