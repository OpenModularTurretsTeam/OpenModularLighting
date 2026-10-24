package omtteam.openmodularlighting.registry;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import omtteam.openmodularlighting.OML;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * All items of the mod; registered in {@link Registration}. Block items use the block's id. The creative tab lists
 * items in registration order, so {@link #ALL} is the tab order.
 */
public final class ModItems {
    public static final ItemBlock ELECTRIC_FLOODLIGHT = blockItem(ModBlocks.ELECTRIC_FLOODLIGHT);
    public static final ItemBlock CARBON_FLOODLIGHT = blockItem(ModBlocks.CARBON_FLOODLIGHT);
    public static final ItemBlock UV_FLOODLIGHT = blockItem(ModBlocks.UV_FLOODLIGHT);
    public static final ItemBlock SMALL_FLUORESCENT_LIGHT = blockItem(ModBlocks.SMALL_FLUORESCENT_LIGHT);
    public static final ItemBlock SQUARE_FLUORESCENT_LIGHT = blockItem(ModBlocks.SQUARE_FLUORESCENT_LIGHT);
    public static final ItemBlock GROW_LIGHT = blockItem(ModBlocks.GROW_LIGHT);
    public static final ItemBlock CUBE_LIGHT = blockItem(ModBlocks.CUBE_LIGHT);

    public static final ItemBlock LIGHTPROOF_GLASS = blockItem(ModBlocks.LIGHTPROOF_GLASS);

    // Crafting items (1.7.10 ModItems)
    public static final Item RAW_FILAMENT = item("raw_filament");
    public static final Item GLOWING_FILAMENT = item("glowing_filament");
    /** 1.7.10 {@code electricIncandescentLightBulb}. */
    public static final Item LIGHT_BULB = item("light_bulb");
    public static final Item CARBON_DISSOLVER = item("carbon_dissolver");
    public static final Item CARBON_LANTERN = item("carbon_lantern");
    public static final Item MANTLE = item("mantle");
    /** While held, shows wireframes around light blocks (client {@code LightBlockDebugRenderer}). */
    public static final Item LIGHT_DEBUG_TOOL = item("light_debug_tool");

    /** Crafting items in creative tab order. */
    public static final List<Item> CRAFTING_ITEMS = Collections.unmodifiableList(Arrays.asList(RAW_FILAMENT,
            GLOWING_FILAMENT, LIGHT_BULB, MANTLE, CARBON_DISSOLVER, CARBON_LANTERN, LIGHT_DEBUG_TOOL));

    /** Every item, in registration (and creative tab) order. */
    public static final List<Item> ALL;

    static {
        List<Item> all = new ArrayList<>(Arrays.asList(ELECTRIC_FLOODLIGHT, CARBON_FLOODLIGHT, UV_FLOODLIGHT,
                SMALL_FLUORESCENT_LIGHT, SQUARE_FLUORESCENT_LIGHT, GROW_LIGHT, CUBE_LIGHT, LIGHTPROOF_GLASS));
        all.addAll(CRAFTING_ITEMS);
        ALL = Collections.unmodifiableList(all);
    }

    private ModItems() {
    }

    /** Translation key {@code item.openmodularlighting.<name>.name}. */
    private static Item item(String name) {
        Item item = new Item();
        item.setRegistryName(OML.id(name));
        item.setUnlocalizedName(OML.MOD_ID + "." + name);
        item.setCreativeTab(ModCreativeTabs.MAIN);
        return item;
    }

    private static ItemBlock blockItem(Block block) {
        ItemBlock item = new ItemBlock(block);
        item.setRegistryName(Objects.requireNonNull(block.getRegistryName()));
        block.setCreativeTab(ModCreativeTabs.MAIN);
        return item;
    }
}
