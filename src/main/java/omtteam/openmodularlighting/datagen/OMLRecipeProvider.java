package omtteam.openmodularlighting.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import omtteam.openmodularlighting.OML;
import omtteam.openmodularlighting.compat.BlockEnabledCondition;
import omtteam.openmodularlighting.platform.fabric.FabricBlockEnabledCondition;
import omtteam.openmodularlighting.compat.CommonTags;
import omtteam.openmodularlighting.registry.ModItems;
import org.jetbrains.annotations.Nullable;

//? if >=1.21 {
import net.minecraft.core.HolderLookup;
import net.minecraft.data.recipes.RecipeOutput;

import java.util.concurrent.CompletableFuture;
//?} else {
/*import net.minecraft.data.recipes.FinishedRecipe;

import java.util.function.Consumer;
*///?}

/**
 * Recipes ported from 1.7.10 {@code RecipeHandler}. Iron, gold, redstone and glass panes use the common {@code c:}
 * tags; the other ingredients are the vanilla items of 1.7.10. Every recipe carries a {@link BlockEnabledCondition}:
 * like in 1.7.10, it only loads if a block that needs it is enabled in the config.
 */
public class OMLRecipeProvider extends FabricRecipeProvider {
    /** Blocks that need the filament and the light bulb (1.7.10: electric, small and UV; the grow light needs a bulb too). */
    private static final String[] BULB_USERS = {"electric_floodlight", "small_fluorescent_light", "uv_floodlight", "grow_light", "cube_light"};
    private static final String CARBON = "carbon_floodlight";

    //? if >=1.21 {
    public OMLRecipeProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    public void buildRecipes(RecipeOutput output) {
        generate((builder, id, blocks) -> {
            RecipeOutput target = blocks == null ? output : withConditions(output, FabricBlockEnabledCondition.of(blocks));
            if (id == null) {
                builder.save(target);
            } else {
                builder.save(target, OML.id(id));
            }
        });
    }
    //?} else {
    /*public OMLRecipeProvider(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void buildRecipes(Consumer<FinishedRecipe> output) {
        generate((builder, id, blocks) -> {
            Consumer<FinishedRecipe> target = blocks == null ? output
                    : withConditions(output, FabricBlockEnabledCondition.of(blocks));
            if (id == null) {
                builder.save(target);
            } else {
                builder.save(target, OML.id(id));
            }
        });
    }
    *///?}

    /** Saves a recipe that loads if any of {@code blocks} is enabled (always, if {@code blocks} is null). */
    @FunctionalInterface
    private interface Saver {
        /** @param id recipe id, or null for the result's id */
        void saveAs(RecipeBuilder builder, @Nullable String id, String[] blocks);

        default void save(RecipeBuilder builder, String... blocks) {
            saveAs(builder, null, blocks);
        }

        /** For recipes of things that have no config switch. */
        default void saveAlways(RecipeBuilder builder) {
            saveAs(builder, null, null);
        }
    }

    private void generate(Saver save) {
        bulb(save);
        electricFloodlight(save);
        carbonFloodlight(save);
        uvFloodlight(save);
        fluorescentLights(save);
        cubeLight(save);
        growLight(save);
        lightproofGlass(save);
    }

    /** Raw filament → (smelting) glowing filament → light bulb. */
    private void bulb(Saver save) {
        save.save(ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.RAW_FILAMENT)
                .pattern(" L ")
                .pattern("RGR")
                .pattern(" L ")
                .define('L', Items.GLOWSTONE_DUST)
                .define('R', CommonTags.REDSTONE_DUSTS)
                .define('G', CommonTags.GOLD_INGOTS)
                .unlockedBy("has_glowstone_dust", has(Items.GLOWSTONE_DUST)), BULB_USERS);
        save.save(SimpleCookingRecipeBuilder.smelting(Ingredient.of(ModItems.RAW_FILAMENT), RecipeCategory.MISC,
                        ModItems.GLOWING_FILAMENT, 0.1F, 200)
                .unlockedBy("has_raw_filament", has(ModItems.RAW_FILAMENT)), BULB_USERS);
        save.save(ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.LIGHT_BULB)
                .pattern(" G ")
                .pattern("GFG")
                .pattern(" I ")
                .define('G', CommonTags.GLASS_PANES)
                .define('F', ModItems.GLOWING_FILAMENT)
                .define('I', CommonTags.IRON_INGOTS)
                .unlockedBy("has_glowing_filament", has(ModItems.GLOWING_FILAMENT)), BULB_USERS);
    }

    private void electricFloodlight(Saver save) {
        save.save(floodlight(ModItems.ELECTRIC_FLOODLIGHT, ModItems.LIGHT_BULB, Items.GLASS), "electric_floodlight");
    }

    /** Mantle + fuel dissolver + flint and steel → carbon lantern → carbon floodlight. */
    private void carbonFloodlight(Saver save) {
        String[] carbon = {CARBON};
        save.save(ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.CARBON_DISSOLVER)
                .pattern("GRG")
                .pattern("IGI")
                .define('G', CommonTags.GLASS_PANES)
                .define('R', CommonTags.REDSTONE_DUSTS)
                .define('I', CommonTags.IRON_INGOTS)
                .unlockedBy("has_redstone", has(CommonTags.REDSTONE_DUSTS)), carbon);
        save.save(ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.MANTLE)
                .pattern("SSS")
                .pattern("SRS")
                .pattern("SSS")
                .define('S', Items.STRING)
                .define('R', CommonTags.REDSTONE_DUSTS)
                .unlockedBy("has_string", has(Items.STRING)), carbon);
        save.save(ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.CARBON_LANTERN)
                .pattern(" G ")
                .pattern("GMG")
                .pattern("DFI")
                .define('G', CommonTags.GLASS_PANES)
                .define('M', ModItems.MANTLE)
                .define('D', ModItems.CARBON_DISSOLVER)
                .define('F', Items.FLINT_AND_STEEL)
                .define('I', CommonTags.IRON_INGOTS)
                .unlockedBy("has_mantle", has(ModItems.MANTLE)), carbon);
        save.save(floodlight(ModItems.CARBON_FLOODLIGHT, ModItems.CARBON_LANTERN, Items.GLASS), carbon);
    }

    /** UV floodlight: the floodlight frame with black stained glass. */
    private void uvFloodlight(Saver save) {
        save.save(floodlight(ModItems.UV_FLOODLIGHT, ModItems.LIGHT_BULB, Items.BLACK_STAINED_GLASS), "uv_floodlight");
    }

    /** Two small fluorescent lights per bulb; small and square convert into each other (1.7.10 metadata 0 ↔ 1). */
    private void fluorescentLights(Saver save) {
        String fluorescent = "small_fluorescent_light";
        save.save(ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ModItems.SMALL_FLUORESCENT_LIGHT, 2)
                .pattern(" G ")
                .pattern("GBG")
                .pattern("III")
                .define('G', Items.GLASS)
                .define('B', ModItems.LIGHT_BULB)
                .define('I', CommonTags.IRON_INGOTS)
                .unlockedBy("has_light_bulb", has(ModItems.LIGHT_BULB)), fluorescent);
        save.save(ShapelessRecipeBuilder.shapeless(RecipeCategory.REDSTONE, ModItems.SQUARE_FLUORESCENT_LIGHT)
                .requires(ModItems.SMALL_FLUORESCENT_LIGHT)
                .unlockedBy("has_small_fluorescent_light", has(ModItems.SMALL_FLUORESCENT_LIGHT)), fluorescent);
        save.saveAs(ShapelessRecipeBuilder.shapeless(RecipeCategory.REDSTONE, ModItems.SMALL_FLUORESCENT_LIGHT)
                        .requires(ModItems.SQUARE_FLUORESCENT_LIGHT)
                        .unlockedBy("has_square_fluorescent_light", has(ModItems.SQUARE_FLUORESCENT_LIGHT)),
                "small_fluorescent_light_from_square", new String[]{fluorescent});
    }

    /** Cube light: a bulb in a glass cage on iron. */
    private void cubeLight(Saver save) {
        save.save(ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ModItems.CUBE_LIGHT)
                .pattern("GGG")
                .pattern("GBG")
                .pattern("III")
                .define('G', Items.GLASS)
                .define('B', ModItems.LIGHT_BULB)
                .define('I', CommonTags.IRON_INGOTS)
                .unlockedBy("has_light_bulb", has(ModItems.LIGHT_BULB)), "cube_light");
    }

    /** Grow light: iron on top, the bulb between cobblestone, pink stained glass below. */
    private void growLight(Saver save) {
        save.save(ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ModItems.GROW_LIGHT)
                .pattern("III")
                .pattern("CBC")
                .pattern("GGG")
                .define('I', CommonTags.IRON_INGOTS)
                .define('C', Items.COBBLESTONE)
                .define('B', ModItems.LIGHT_BULB)
                .define('G', Items.PINK_STAINED_GLASS)
                .unlockedBy("has_light_bulb", has(ModItems.LIGHT_BULB)), "grow_light");
    }

    /** Black stained glass (the UV floodlight's glass) + coal. The glass has no config switch, so no condition. */
    private void lightproofGlass(Saver save) {
        save.saveAlways(ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, ModItems.LIGHTPROOF_GLASS)
                .requires(Items.BLACK_STAINED_GLASS)
                .requires(ItemTags.COALS)
                .unlockedBy("has_black_stained_glass", has(Items.BLACK_STAINED_GLASS)));
    }

    /** The common floodlight frame (electric, carbon, UV). */
    private static ShapedRecipeBuilder floodlight(ItemLike result, ItemLike lamp, ItemLike glass) {
        return ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, result)
                .pattern("CIC")
                .pattern("IBG")
                .pattern("CIC")
                .define('C', Items.COBBLESTONE)
                .define('I', CommonTags.IRON_INGOTS)
                .define('B', lamp)
                .define('G', glass)
                .unlockedBy("has_lamp", has(lamp));
    }
}
