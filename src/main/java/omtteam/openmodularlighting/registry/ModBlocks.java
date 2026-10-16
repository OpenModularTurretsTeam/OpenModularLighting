package omtteam.openmodularlighting.registry;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import omtteam.openmodularlighting.OML;
import omtteam.openmodularlighting.block.CarbonFloodlightBlock;
import omtteam.openmodularlighting.block.ElectricFloodlightBlock;
import omtteam.openmodularlighting.block.FloodlightBlock;
import omtteam.openmodularlighting.block.FluorescentLightBlock;
import omtteam.openmodularlighting.block.GrowLightBlock;
import omtteam.openmodularlighting.block.PanelLightBlock;
import omtteam.openmodularlighting.block.PhantomLightBlock;
import omtteam.openmodularlighting.block.UvFloodlightBlock;
import omtteam.openmodularlighting.block.UvLightBlock;

import java.util.List;

/**
 * All blocks of the mod. 1.7.10 ids are mapped in {@code docs/id-mapping.md}.
 */
public final class ModBlocks {
    /** Invisible light block of the floodlights; no item, not in the creative tab. */
    public static final PhantomLightBlock PHANTOM_LIGHT =
            register("phantom_light", new PhantomLightBlock(PhantomLightBlock.createProperties()));
    /** Light block of the UV floodlight; no item, not in the creative tab. */
    public static final UvLightBlock UV_LIGHT = register("uv_light", new UvLightBlock(UvLightBlock.createProperties()));

    public static final ElectricFloodlightBlock ELECTRIC_FLOODLIGHT =
            register("electric_floodlight", new ElectricFloodlightBlock(FloodlightBlock.createProperties()));
    public static final CarbonFloodlightBlock CARBON_FLOODLIGHT =
            register("carbon_floodlight", new CarbonFloodlightBlock(FloodlightBlock.createProperties()));
    public static final UvFloodlightBlock UV_FLOODLIGHT =
            register("uv_floodlight", new UvFloodlightBlock(FloodlightBlock.createProperties()));
    public static final FluorescentLightBlock SMALL_FLUORESCENT_LIGHT = register("small_fluorescent_light",
            new FluorescentLightBlock(PanelLightBlock.createPanelProperties(), true));
    public static final FluorescentLightBlock SQUARE_FLUORESCENT_LIGHT = register("square_fluorescent_light",
            new FluorescentLightBlock(PanelLightBlock.createPanelProperties(), false));
    public static final GrowLightBlock GROW_LIGHT =
            register("grow_light", new GrowLightBlock(PanelLightBlock.createPanelProperties()));

    /** All light source blocks (shared interaction, drops and tint), in creative tab order. */
    public static final List<FloodlightBlock> LIGHT_SOURCES = List.of(ELECTRIC_FLOODLIGHT, CARBON_FLOODLIGHT,
            UV_FLOODLIGHT, SMALL_FLUORESCENT_LIGHT, SQUARE_FLUORESCENT_LIGHT, GROW_LIGHT);

    private ModBlocks() {
    }

    private static <T extends Block> T register(String name, T block) {
        return Registry.register(BuiltInRegistries.BLOCK, OML.id(name), block);
    }

    /** Loads the class, registering all blocks. */
    public static void init() {
    }
}
