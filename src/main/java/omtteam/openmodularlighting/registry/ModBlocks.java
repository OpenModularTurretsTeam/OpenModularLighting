package omtteam.openmodularlighting.registry;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.TintedGlassBlock;
import net.minecraft.world.level.material.MapColor;
import omtteam.openmodularlighting.OML;
import omtteam.openmodularlighting.block.CarbonFloodlightBlock;
import omtteam.openmodularlighting.block.CubeLightBlock;
import omtteam.openmodularlighting.block.ElectricFloodlightBlock;
import omtteam.openmodularlighting.block.FloodlightBlock;
import omtteam.openmodularlighting.block.FluorescentLightBlock;
import omtteam.openmodularlighting.block.GrowLightBlock;
import omtteam.openmodularlighting.block.PanelLightBlock;
import omtteam.openmodularlighting.block.PhantomLightBlock;
import omtteam.openmodularlighting.block.UvFloodlightBlock;
import omtteam.openmodularlighting.block.UvLightBlock;
import omtteam.openmodularlighting.compat.VersionCompat;

import java.util.List;
import java.util.function.Function;

/**
 * All blocks of the mod. 1.7.10 ids are mapped in {@code docs/id-mapping.md}.
 */
public final class ModBlocks {
    /** Invisible light block of the floodlights; no item, not in the creative tab. */
    public static final PhantomLightBlock PHANTOM_LIGHT =
            register("phantom_light", PhantomLightBlock::new, PhantomLightBlock.createProperties());
    /** Light block of the UV floodlight; no item, not in the creative tab. */
    public static final UvLightBlock UV_LIGHT = register("uv_light", UvLightBlock::new, UvLightBlock.createProperties());

    public static final ElectricFloodlightBlock ELECTRIC_FLOODLIGHT =
            register("electric_floodlight", ElectricFloodlightBlock::new, FloodlightBlock.createProperties());
    public static final CarbonFloodlightBlock CARBON_FLOODLIGHT =
            register("carbon_floodlight", CarbonFloodlightBlock::new, FloodlightBlock.createProperties());
    public static final UvFloodlightBlock UV_FLOODLIGHT =
            register("uv_floodlight", UvFloodlightBlock::new, FloodlightBlock.createProperties());
    public static final FluorescentLightBlock SMALL_FLUORESCENT_LIGHT = register("small_fluorescent_light",
            properties -> new FluorescentLightBlock(properties, true), PanelLightBlock.createPanelProperties());
    public static final FluorescentLightBlock SQUARE_FLUORESCENT_LIGHT = register("square_fluorescent_light",
            properties -> new FluorescentLightBlock(properties, false), PanelLightBlock.createPanelProperties());
    public static final GrowLightBlock GROW_LIGHT =
            register("grow_light", GrowLightBlock::new, PanelLightBlock.createPanelProperties());
    public static final CubeLightBlock CUBE_LIGHT =
            register("cube_light", CubeLightBlock::new, FloodlightBlock.createProperties());

    /** Glass that can be seen through but lets no light through (like tinted glass), and stops floodlight beams. */
    public static final TintedGlassBlock LIGHTPROOF_GLASS = register("lightproof_glass", TintedGlassBlock::new,
            Block.Properties.of()
                    .mapColor(MapColor.COLOR_BLACK)
                    .strength(0.3F)
                    .sound(SoundType.GLASS)
                    .noOcclusion()
                    .isValidSpawn((state, level, pos, type) -> false)
                    .isRedstoneConductor((state, level, pos) -> false)
                    .isSuffocating((state, level, pos) -> false)
                    .isViewBlocking((state, level, pos) -> false));

    /** All light source blocks (shared interaction, drops and tint), in creative tab order. */
    public static final List<FloodlightBlock> LIGHT_SOURCES = List.of(ELECTRIC_FLOODLIGHT, CARBON_FLOODLIGHT,
            UV_FLOODLIGHT, SMALL_FLUORESCENT_LIGHT, SQUARE_FLUORESCENT_LIGHT, GROW_LIGHT, CUBE_LIGHT);

    private ModBlocks() {
    }

    private static <T extends Block> T register(String name, Function<Block.Properties, T> factory,
                                                Block.Properties properties) {
        ResourceLocation id = OML.id(name);
        return Registry.register(BuiltInRegistries.BLOCK, id, factory.apply(VersionCompat.blockProperties(properties, id)));
    }

    /** Loads the class, registering all blocks. */
    public static void init() {
    }
}
