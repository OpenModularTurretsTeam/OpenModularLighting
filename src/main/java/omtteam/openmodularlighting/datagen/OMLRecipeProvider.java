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

//? if >=1.21.11 {
/*import net.minecraft.data.recipes.RecipeProvider;
*///?}
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
 * <p>
 * The recipes themselves are in {@link Recipes}, the same on every version; only the way into it differs.
 */
public class OMLRecipeProvider extends FabricRecipeProvider {
    /** Blocks that need the filament and the light bulb (1.7.10: electric, small and UV; the grow light needs a bulb too). */
    private static final String[] BULB_USERS = {"electric_floodlight", "small_fluorescent_light", "uv_floodlight", "grow_light", "cube_light"};
    private static final String CARBON = "carbon_floodlight";

    //? if >=1.21 {
    public OMLRecipeProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }
    //?} else {
    /*public OMLRecipeProvider(FabricDataOutput output) {
        super(output);
    }
    *///?}

    //? if >=1.21.11 {
    /*@Override
    protected RecipeProvider createRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
        return new Recipes(registries, output);
    }

    @Override
    public String getName() {
        return "Recipes";
    }
    *///?} else if >=1.21 {
    @Override
    public void buildRecipes(RecipeOutput output) {
        new Recipes(output).generate();
    }
    //?} else {
    /*@Override
    public void buildRecipes(Consumer<FinishedRecipe> output) {
        new Recipes(output).generate();
    }
    *///?}

    // From 1.21.2 the recipe builders need the item registry, so the vanilla provider hands them out (shaped, shapeless,
    // has). Before, Recipes is a plain class: it inherits the static has from this provider and gets shaped and
    // shapeless as stand-ins below.
    //? if >=1.21.11 {
    /*private final class Recipes extends RecipeProvider {
        Recipes(HolderLookup.Provider registries, RecipeOutput output) {
            super(registries, output);
        }

        @Override
        public void buildRecipes() {
            generate();
        }
    *///?} else if >=1.21 {
    private final class Recipes {
        private final RecipeOutput output;

        Recipes(RecipeOutput output) {
            this.output = output;
        }
    //?} else {
    /*private final class Recipes {
        private final Consumer<FinishedRecipe> output;

        Recipes(Consumer<FinishedRecipe> output) {
            this.output = output;
        }
    *///?}
    //? if <1.21.11 {

        private ShapedRecipeBuilder shaped(RecipeCategory category, ItemLike result) {
            return ShapedRecipeBuilder.shaped(category, result);
        }

        private ShapedRecipeBuilder shaped(RecipeCategory category, ItemLike result, int count) {
            return ShapedRecipeBuilder.shaped(category, result, count);
        }

        private ShapelessRecipeBuilder shapeless(RecipeCategory category, ItemLike result) {
            return ShapelessRecipeBuilder.shapeless(category, result);
        }
    //?}

        /** Saves a recipe that loads if any of {@code blocks} is enabled. */
        private void save(RecipeBuilder builder, String... blocks) {
            builder.save(withConditions(output, FabricBlockEnabledCondition.of(blocks)));
        }

        /** Like {@link #save}, under {@code id} instead of the result's id. */
        private void saveAs(RecipeBuilder builder, String id, String... blocks) {
            builder.save(withConditions(output, FabricBlockEnabledCondition.of(blocks)), OML.id(id).toString());
        }

        /** For recipes of things that have no config switch. */
        private void saveAlways(RecipeBuilder builder) {
            builder.save(output);
        }

        void generate() {
            bulb();
            electricFloodlight();
            carbonFloodlight();
            uvFloodlight();
            fluorescentLights();
            cubeLight();
            growLight();
            lightproofGlass();
        }

        /** Raw filament → (smelting) glowing filament → light bulb. */
        private void bulb() {
            save(shaped(RecipeCategory.MISC, ModItems.RAW_FILAMENT)
                    .pattern(" L ")
                    .pattern("RGR")
                    .pattern(" L ")
                    .define('L', Items.GLOWSTONE_DUST)
                    .define('R', CommonTags.REDSTONE_DUSTS)
                    .define('G', CommonTags.GOLD_INGOTS)
                    .unlockedBy("has_glowstone_dust", has(Items.GLOWSTONE_DUST)), BULB_USERS);
            save(SimpleCookingRecipeBuilder.smelting(Ingredient.of(ModItems.RAW_FILAMENT), RecipeCategory.MISC,
                            ModItems.GLOWING_FILAMENT, 0.1F, 200)
                    .unlockedBy("has_raw_filament", has(ModItems.RAW_FILAMENT)), BULB_USERS);
            save(shaped(RecipeCategory.MISC, ModItems.LIGHT_BULB)
                    .pattern(" G ")
                    .pattern("GFG")
                    .pattern(" I ")
                    .define('G', CommonTags.GLASS_PANES)
                    .define('F', ModItems.GLOWING_FILAMENT)
                    .define('I', CommonTags.IRON_INGOTS)
                    .unlockedBy("has_glowing_filament", has(ModItems.GLOWING_FILAMENT)), BULB_USERS);
        }

        private void electricFloodlight() {
            save(floodlight(ModItems.ELECTRIC_FLOODLIGHT, ModItems.LIGHT_BULB, Items.GLASS), "electric_floodlight");
        }

        /** Mantle + fuel dissolver + flint and steel → carbon lantern → carbon floodlight. */
        private void carbonFloodlight() {
            String[] carbon = {CARBON};
            save(shaped(RecipeCategory.MISC, ModItems.CARBON_DISSOLVER)
                    .pattern("GRG")
                    .pattern("IGI")
                    .define('G', CommonTags.GLASS_PANES)
                    .define('R', CommonTags.REDSTONE_DUSTS)
                    .define('I', CommonTags.IRON_INGOTS)
                    .unlockedBy("has_redstone", has(CommonTags.REDSTONE_DUSTS)), carbon);
            save(shaped(RecipeCategory.MISC, ModItems.MANTLE)
                    .pattern("SSS")
                    .pattern("SRS")
                    .pattern("SSS")
                    .define('S', Items.STRING)
                    .define('R', CommonTags.REDSTONE_DUSTS)
                    .unlockedBy("has_string", has(Items.STRING)), carbon);
            save(shaped(RecipeCategory.MISC, ModItems.CARBON_LANTERN)
                    .pattern(" G ")
                    .pattern("GMG")
                    .pattern("DFI")
                    .define('G', CommonTags.GLASS_PANES)
                    .define('M', ModItems.MANTLE)
                    .define('D', ModItems.CARBON_DISSOLVER)
                    .define('F', Items.FLINT_AND_STEEL)
                    .define('I', CommonTags.IRON_INGOTS)
                    .unlockedBy("has_mantle", has(ModItems.MANTLE)), carbon);
            save(floodlight(ModItems.CARBON_FLOODLIGHT, ModItems.CARBON_LANTERN, Items.GLASS), carbon);
        }

        /** UV floodlight: the floodlight frame with black stained glass. */
        private void uvFloodlight() {
            save(floodlight(ModItems.UV_FLOODLIGHT, ModItems.LIGHT_BULB, Items.BLACK_STAINED_GLASS), "uv_floodlight");
        }

        /** Two small fluorescent lights per bulb; small and square convert into each other (1.7.10 metadata 0 ↔ 1). */
        private void fluorescentLights() {
            String fluorescent = "small_fluorescent_light";
            save(shaped(RecipeCategory.REDSTONE, ModItems.SMALL_FLUORESCENT_LIGHT, 2)
                    .pattern(" G ")
                    .pattern("GBG")
                    .pattern("III")
                    .define('G', Items.GLASS)
                    .define('B', ModItems.LIGHT_BULB)
                    .define('I', CommonTags.IRON_INGOTS)
                    .unlockedBy("has_light_bulb", has(ModItems.LIGHT_BULB)), fluorescent);
            save(shapeless(RecipeCategory.REDSTONE, ModItems.SQUARE_FLUORESCENT_LIGHT)
                    .requires(ModItems.SMALL_FLUORESCENT_LIGHT)
                    .unlockedBy("has_small_fluorescent_light", has(ModItems.SMALL_FLUORESCENT_LIGHT)), fluorescent);
            saveAs(shapeless(RecipeCategory.REDSTONE, ModItems.SMALL_FLUORESCENT_LIGHT)
                            .requires(ModItems.SQUARE_FLUORESCENT_LIGHT)
                            .unlockedBy("has_square_fluorescent_light", has(ModItems.SQUARE_FLUORESCENT_LIGHT)),
                    "small_fluorescent_light_from_square", fluorescent);
        }

        /** Cube light: a bulb in a glass cage on iron. */
        private void cubeLight() {
            save(shaped(RecipeCategory.REDSTONE, ModItems.CUBE_LIGHT)
                    .pattern("GGG")
                    .pattern("GBG")
                    .pattern("III")
                    .define('G', Items.GLASS)
                    .define('B', ModItems.LIGHT_BULB)
                    .define('I', CommonTags.IRON_INGOTS)
                    .unlockedBy("has_light_bulb", has(ModItems.LIGHT_BULB)), "cube_light");
        }

        /** Grow light: iron on top, the bulb between cobblestone, pink stained glass below. */
        private void growLight() {
            save(shaped(RecipeCategory.REDSTONE, ModItems.GROW_LIGHT)
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
        private void lightproofGlass() {
            saveAlways(shapeless(RecipeCategory.BUILDING_BLOCKS, ModItems.LIGHTPROOF_GLASS)
                    .requires(Items.BLACK_STAINED_GLASS)
                    .requires(ItemTags.COALS)
                    .unlockedBy("has_black_stained_glass", has(Items.BLACK_STAINED_GLASS)));
        }

        /** The common floodlight frame (electric, carbon, UV). */
        private ShapedRecipeBuilder floodlight(ItemLike result, ItemLike lamp, ItemLike glass) {
            return shaped(RecipeCategory.REDSTONE, result)
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
}
