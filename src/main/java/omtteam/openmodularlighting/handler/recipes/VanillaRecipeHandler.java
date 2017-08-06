package omtteam.openmodularlighting.handler.recipes;


import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.ShapedOreRecipe;
import omtteam.openmodularlighting.init.ModBlocks;
import omtteam.openmodularlighting.init.ModItems;


class VanillaRecipeHandler {
    public static void init() {
        // ModItems
        // Barrels
        GameRegistry.addRecipe(
                new ShapedOreRecipe(new ItemStack(ModItems.intermediateProductTiered, 1, 11), "AAA", " B ", "AAA", 'A', "ingotIron",
                        'B', new ItemStack(ModItems.intermediateProductTiered, 1, 10)));

        GameRegistry.addRecipe(
                new ShapedOreRecipe(new ItemStack(ModItems.intermediateProductTiered, 1, 12), "AAA", " B ", "AAA", 'A', "ingotGold",
                        'B', new ItemStack(ModItems.intermediateProductTiered, 1, 11)));

        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModItems.intermediateProductTiered, 1, 13), "CAC", " B ", "CAC", 'A',
                Items.DIAMOND, 'B', new ItemStack(ModItems.intermediateProductTiered, 1, 12),
                'C', Items.QUARTZ));

        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModItems.intermediateProductTiered, 1, 14), "AAA", "CBC", "AAA", 'A',
                Blocks.OBSIDIAN, 'B', new ItemStack(ModItems.intermediateProductTiered, 1, 13),
                'C', Items.GLOWSTONE_DUST));

        // Chambers
        GameRegistry.addRecipe(
                new ShapedOreRecipe(new ItemStack(ModItems.intermediateProductTiered, 1, 6), "AAA", " BC", "AAA", 'A', "ingotIron",
                        'B', new ItemStack(ModItems.intermediateProductTiered, 1, 5), 'C', Items.REDSTONE));

        GameRegistry.addRecipe(
                new ShapedOreRecipe(new ItemStack(ModItems.intermediateProductTiered, 1, 7), "AAA", " BC", "AAA", 'A', "ingotGold",
                        'B', new ItemStack(ModItems.intermediateProductTiered, 1, 6), 'C', RecipeHandler.transformer));

        GameRegistry.addRecipe(
                new ShapedOreRecipe(new ItemStack(ModItems.intermediateProductTiered, 1, 8), "DAD", " BC", "DAD", 'A',
                        Items.DIAMOND, 'B', new ItemStack(ModItems.intermediateProductTiered, 1, 7), 'C', RecipeHandler.transformer,
                        'D', Items.QUARTZ));

        GameRegistry.addRecipe(
                new ShapedOreRecipe(new ItemStack(ModItems.intermediateProductTiered, 1, 9), "ADA", " BC", "ADA", 'A',
                        Blocks.OBSIDIAN, 'B', new ItemStack(ModItems.intermediateProductTiered, 1, 8), 'C',
                        RecipeHandler.transformer, 'D', Items.QUARTZ));

        // Sensors
        GameRegistry.addRecipe(
                new ShapedOreRecipe(new ItemStack(ModItems.intermediateProductTiered, 1, 1), " A ", "ABA", " C ", 'A', "ingotIron",
                        'B', new ItemStack(ModItems.intermediateProductTiered, 1, 0), 'C', RecipeHandler.transformer));

        GameRegistry.addRecipe(
                new ShapedOreRecipe(new ItemStack(ModItems.intermediateProductTiered, 1, 2), " C ", "ABA", " C ", 'A', "ingotGold",
                        'B', new ItemStack(ModItems.intermediateProductTiered, 1, 1), 'C', RecipeHandler.transformer));

        GameRegistry.addRecipe(
                new ShapedOreRecipe(new ItemStack(ModItems.intermediateProductTiered, 1, 3), "EDE", "CBC", "EDE", 'A', "ingotGold",
                        'B', new ItemStack(ModItems.intermediateProductTiered, 1, 2), 'C', RecipeHandler.transformer, 'D',
                        Items.DIAMOND, 'E', Items.QUARTZ));

        GameRegistry.addRecipe(
                new ShapedOreRecipe(new ItemStack(ModItems.intermediateProductTiered, 1, 4), "EDE", "CBC", "EDE", 'A', "ingotGold",
                        'B', new ItemStack(ModItems.intermediateProductTiered, 1, 3), 'C', RecipeHandler.transformer, 'D',
                        Items.GLOWSTONE_DUST, 'E', Blocks.OBSIDIAN));

        // Bases
        GameRegistry.addRecipe(
                new ShapedOreRecipe(new ItemStack(ModBlocks.lightingBase, 1, 1), "ABA", "DCD", "ADA", 'A', "ingotIron",
                        'B', new ItemStack(ModBlocks.lightingBase, 1, 0), 'C', new ItemStack(ModItems.intermediateProductTiered, 1, 1), 'D', RecipeHandler.transformer));

        GameRegistry.addRecipe(
                new ShapedOreRecipe(new ItemStack(ModBlocks.lightingBase, 1, 2), "ABA", "DCD", "ADA", 'A', "ingotGold",
                        'B', new ItemStack(ModBlocks.lightingBase, 1, 1), 'C', new ItemStack(ModItems.intermediateProductTiered, 1, 2), 'D', RecipeHandler.transformer));

        GameRegistry.addRecipe(
                new ShapedOreRecipe(new ItemStack(ModBlocks.lightingBase, 1, 3), "ABA", "DCD", "ADA", 'A',
                        Items.DIAMOND, 'B', new ItemStack(ModBlocks.lightingBase, 1, 2), 'C',
                        new ItemStack(ModItems.intermediateProductTiered, 1, 3), 'D', RecipeHandler.transformer));

        GameRegistry.addRecipe(
                new ShapedOreRecipe(new ItemStack(ModBlocks.lightingBase, 1, 4), "ABA", "DCD", "ADA", 'A',
                        Blocks.OBSIDIAN, 'B', new ItemStack(ModBlocks.lightingBase, 1, 3), 'C',
                        new ItemStack(ModItems.intermediateProductTiered, 1, 4), 'D', RecipeHandler.transformer));


    }
}
