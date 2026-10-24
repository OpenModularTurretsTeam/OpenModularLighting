package omtteam.openmodularlighting.registry;

import net.minecraft.block.Block;
import omtteam.openmodularlighting.OML;
import omtteam.openmodularlighting.block.CarbonFloodlightBlock;
import omtteam.openmodularlighting.block.CubeLightBlock;
import omtteam.openmodularlighting.block.ElectricFloodlightBlock;
import omtteam.openmodularlighting.block.FloodlightBlock;
import omtteam.openmodularlighting.block.FluorescentLightBlock;
import omtteam.openmodularlighting.block.GrowLightBlock;
import omtteam.openmodularlighting.block.LightproofGlassBlock;
import omtteam.openmodularlighting.block.PhantomLightBlock;
import omtteam.openmodularlighting.block.UvFloodlightBlock;
import omtteam.openmodularlighting.block.UvLightBlock;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * All blocks of the mod; registered in {@link Registration}. 1.7.10 ids are mapped in {@code docs/id-mapping.md}.
 */
public final class ModBlocks {
    /** Invisible light block of the floodlights; no item, not in the creative tab. */
    public static final PhantomLightBlock PHANTOM_LIGHT = named("phantom_light", new PhantomLightBlock());
    /** Light block of the UV floodlight; no item, not in the creative tab. */
    public static final UvLightBlock UV_LIGHT = named("uv_light", new UvLightBlock());

    public static final ElectricFloodlightBlock ELECTRIC_FLOODLIGHT =
            named("electric_floodlight", new ElectricFloodlightBlock());
    public static final CarbonFloodlightBlock CARBON_FLOODLIGHT = named("carbon_floodlight", new CarbonFloodlightBlock());
    public static final UvFloodlightBlock UV_FLOODLIGHT = named("uv_floodlight", new UvFloodlightBlock());
    public static final FluorescentLightBlock SMALL_FLUORESCENT_LIGHT =
            named("small_fluorescent_light", new FluorescentLightBlock(true));
    public static final FluorescentLightBlock SQUARE_FLUORESCENT_LIGHT =
            named("square_fluorescent_light", new FluorescentLightBlock(false));
    public static final GrowLightBlock GROW_LIGHT = named("grow_light", new GrowLightBlock());
    public static final CubeLightBlock CUBE_LIGHT = named("cube_light", new CubeLightBlock());

    /** Glass that can be seen through but lets no light through, and stops floodlight beams. */
    public static final LightproofGlassBlock LIGHTPROOF_GLASS = named("lightproof_glass", new LightproofGlassBlock());

    /** All light source blocks (shared interaction, drops and tint), in creative tab order. */
    public static final List<FloodlightBlock> LIGHT_SOURCES = Collections.unmodifiableList(Arrays.asList(
            ELECTRIC_FLOODLIGHT, CARBON_FLOODLIGHT, UV_FLOODLIGHT, SMALL_FLUORESCENT_LIGHT, SQUARE_FLUORESCENT_LIGHT,
            GROW_LIGHT, CUBE_LIGHT));

    /** Every block, in registration order. */
    public static final List<Block> ALL = Collections.unmodifiableList(Arrays.asList(PHANTOM_LIGHT, UV_LIGHT,
            ELECTRIC_FLOODLIGHT, CARBON_FLOODLIGHT, UV_FLOODLIGHT, SMALL_FLUORESCENT_LIGHT, SQUARE_FLUORESCENT_LIGHT,
            GROW_LIGHT, CUBE_LIGHT, LIGHTPROOF_GLASS));

    private ModBlocks() {
    }

    /** Registry name {@code openmodularlighting:<name>}, translation key {@code tile.openmodularlighting.<name>.name}. */
    private static <T extends Block> T named(String name, T block) {
        block.setRegistryName(OML.id(name));
        block.setUnlocalizedName(OML.MOD_ID + "." + name);
        return block;
    }
}
