package omtteam.openmodularlighting.handler.recipes;


import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.ShapedOreRecipe;
import omtteam.openmodularlighting.compatibility.ModCompatibility;
import omtteam.openmodularlighting.handler.ConfigHandler;
import omtteam.openmodularlighting.init.ModBlocks;
import omtteam.openmodularlighting.init.ModItems;

public class RecipeHandler {

    private static ItemStack addonFakeDrops;

    private static ItemStack upgradeEfficiency;


    private static ItemStack memoryCard;
    public static ItemStack transformer;


    @SuppressWarnings("StatementWithEmptyBody")
    public static void initRecipes() {


        addonFakeDrops = new ItemStack(ModItems.addonMetaItem, 1, 0);
        upgradeEfficiency = new ItemStack(ModItems.upgradeMetaItem, 1, 0);
        memoryCard = new ItemStack(ModItems.usableMetaItem, 1, 0);
        transformer = new ItemStack(ModItems.intermediateProductRegular, 1, 0);


        boolean recipesDone = false;
        // Recipes

        if (ModCompatibility.EnderIOLoaded && ConfigHandler.recipes.equals("enderio")) {
            EnderIORecipeHandler.init();
            recipesDone = true;
        } else if (ModCompatibility.MekanismLoaded && ConfigHandler.recipes.equals("mekanism")) {
            MekanismRecipeHandler.init();
            recipesDone = true;
        } else if (ConfigHandler.recipes.equals("vanilla")) {
            VanillaRecipeHandler.init();
            recipesDone = true;
        } else if (ConfigHandler.recipes.equals("auto")) {
            if (ModCompatibility.EnderIOLoaded) {
                EnderIORecipeHandler.init();
            } else if (ModCompatibility.MekanismLoaded) {
                MekanismRecipeHandler.init();
            } else {
                VanillaRecipeHandler.init();
            }
            recipesDone = true;
        }

        // Only do vanilla if setting was invalid (recipes chosen but mod not available)
        if (!recipesDone) {
            VanillaRecipeHandler.init();
        }

        //RECIPES THAT DON'T CHANGE BASED ON MODS LOADED:
        //Tier 1 static recipes (Because they shouldn't use expensive mod items, only redstone, cobblestone and planks)
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModItems.intermediateProductTiered, 1, 10), "AAA", " B ", "AAA", 'A',
                Blocks.COBBLESTONE, 'B', "plankWood"));

        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModItems.intermediateProductTiered, 1, 5), "AAA", " BC", "AAA", 'A',
                Blocks.COBBLESTONE, 'B', "plankWood", 'C',
                Items.REDSTONE));

        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModItems.intermediateProductTiered, 1, 0), " A ", "ABA", " A ", 'A',
                Items.REDSTONE, 'B', "plankWood"));

        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModBlocks.lightingBase, 1, 0), "ABA", "BCB", "ABA", 'A',
                Blocks.COBBLESTONE, 'B', "plankWood", 'C',
                new ItemStack(ModItems.intermediateProductTiered, 1, 0)));

        GameRegistry.addRecipe(
                new ShapedOreRecipe(addonFakeDrops, "CAC", "ABD", "CAC", 'A', Blocks.LAPIS_BLOCK,
                        'B', Items.ENDER_EYE, 'C', Items.QUARTZ, 'D',
                        transformer));

        // Integration


        // Upgrades
        GameRegistry.addRecipe(
                new ShapedOreRecipe(upgradeEfficiency, " A ", "ABA", " C ", 'A',
                        Items.QUARTZ, 'B', Items.ENDER_EYE, 'C',
                        transformer));


        //Other
        GameRegistry.addRecipe(
                new ShapedOreRecipe(transformer, " A ", "BBB", " C ", 'A', "ingotGold", 'B',
                        Items.REDSTONE, 'C', Items.IRON_INGOT));

        GameRegistry.addRecipe(
                new ShapedOreRecipe(memoryCard, "BAB", "CEC", "FDF", 'A', "ingotGold", 'B',
                        Items.REDSTONE, 'C', Items.IRON_INGOT, 'D', RecipeHandler.transformer, 'E', Items.PAPER,
                        'F', new ItemStack(Items.DYE, 1, 4)));

    }
}
