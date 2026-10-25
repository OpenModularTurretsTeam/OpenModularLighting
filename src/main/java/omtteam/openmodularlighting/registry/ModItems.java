package omtteam.openmodularlighting.registry;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import omtteam.openmodularlighting.OML;
import omtteam.openmodularlighting.compat.VersionCompat;

import java.util.List;

/**
 * All items of the mod. Block items use the block's id.
 */
public final class ModItems {
    public static final BlockItem ELECTRIC_FLOODLIGHT = registerBlockItem("electric_floodlight", ModBlocks.ELECTRIC_FLOODLIGHT);
    public static final BlockItem CARBON_FLOODLIGHT = registerBlockItem("carbon_floodlight", ModBlocks.CARBON_FLOODLIGHT);
    public static final BlockItem UV_FLOODLIGHT = registerBlockItem("uv_floodlight", ModBlocks.UV_FLOODLIGHT);
    public static final BlockItem SMALL_FLUORESCENT_LIGHT =
            registerBlockItem("small_fluorescent_light", ModBlocks.SMALL_FLUORESCENT_LIGHT);
    public static final BlockItem SQUARE_FLUORESCENT_LIGHT =
            registerBlockItem("square_fluorescent_light", ModBlocks.SQUARE_FLUORESCENT_LIGHT);
    public static final BlockItem GROW_LIGHT = registerBlockItem("grow_light", ModBlocks.GROW_LIGHT);
    public static final BlockItem CUBE_LIGHT = registerBlockItem("cube_light", ModBlocks.CUBE_LIGHT);

    public static final BlockItem LIGHTPROOF_GLASS = registerBlockItem("lightproof_glass", ModBlocks.LIGHTPROOF_GLASS);

    // Crafting items (1.7.10 ModItems)
    public static final Item RAW_FILAMENT = register("raw_filament");
    public static final Item GLOWING_FILAMENT = register("glowing_filament");
    /** 1.7.10 {@code electricIncandescentLightBulb}. */
    public static final Item LIGHT_BULB = register("light_bulb");
    public static final Item CARBON_DISSOLVER = register("carbon_dissolver");
    public static final Item CARBON_LANTERN = register("carbon_lantern");
    public static final Item MANTLE = register("mantle");
    /** While held, shows wireframes around light blocks (client {@code LightBlockDebugRenderer}). */
    public static final Item LIGHT_DEBUG_TOOL = register("light_debug_tool");

    /** Crafting items in creative tab order. */
    public static final List<Item> CRAFTING_ITEMS = List.of(RAW_FILAMENT, GLOWING_FILAMENT, LIGHT_BULB, MANTLE,
            CARBON_DISSOLVER, CARBON_LANTERN, LIGHT_DEBUG_TOOL);

    private ModItems() {
    }

    private static Item register(String name) {
        ResourceLocation id = OML.id(name);
        return Registry.register(BuiltInRegistries.ITEM, id, new Item(VersionCompat.itemProperties(new Item.Properties(), id)));
    }

    private static BlockItem registerBlockItem(String name, Block block) {
        ResourceLocation id = OML.id(name);
        return Registry.register(BuiltInRegistries.ITEM, id,
                new BlockItem(block, VersionCompat.blockItemProperties(new Item.Properties(), id)));
    }

    /** Loads the class, registering all items. */
    public static void init() {
    }
}
