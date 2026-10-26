package omtteam.openmodularlighting.gametest;

//? if neoforge && <1.21.11 {
/*import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
*///?}
//? if forge {
/*import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
*///?}
//? if (neoforge && <1.21.11) || forge {
/*import omtteam.openmodularlighting.OML;
*///?}
//? if <1.21.11 {
import net.minecraft.gametest.framework.GameTest;
//?}
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.crafting.RecipeManager;
//? if >=1.21.11 {
/*import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
*///?}
import omtteam.openmodularlighting.OML;
import omtteam.openmodularlighting.compat.BlockEnabledCondition;
import omtteam.openmodularlighting.config.ConfigData;
import omtteam.openmodularlighting.config.OMLConfig;

import java.util.List;

/**
 * The generated recipes load (their {@code block_enabled} condition is registered and passes with the default
 * config), and the condition follows the config.
 */
//? if (neoforge && <1.21.11) || forge {
/*@GameTestHolder(OML.MOD_ID)
@PrefixGameTestTemplate(false)
*///?}
public class RecipeGameTest implements OMLGameTests {
    private static final List<String> RECIPES = List.of("raw_filament", "glowing_filament", "light_bulb",
            "electric_floodlight", "carbon_dissolver", "mantle", "carbon_lantern", "carbon_floodlight", "uv_floodlight",
            "small_fluorescent_light", "square_fluorescent_light", "small_fluorescent_light_from_square", "grow_light");

    @GameTest(template = EMPTY_STRUCTURE)
    public void allRecipesLoadWithTheDefaultConfig(GameTestHelper helper) {
        RecipeManager recipes = helper.getLevel().getServer().getRecipeManager();
        for (String recipe : RECIPES) {
            //? if >=1.21.11 {
            /*boolean loaded = recipes.byKey(ResourceKey.create(Registries.RECIPE, OML.id(recipe))).isPresent();
            *///?} else {
            boolean loaded = recipes.byKey(OML.id(recipe)).isPresent();
            //?}
            helper.assertTrue(loaded, "recipe " + recipe + " loaded");
        }
        helper.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void conditionFollowsTheConfig(GameTestHelper helper) {
        ConfigData.Blocks blocks = OMLConfig.get().blocks;
        boolean electric = blocks.electricFloodlightEnabled;
        boolean small = blocks.smallElectricFloodlightEnabled;
        boolean uv = blocks.uvFloodlightEnabled;
        try {
            List<String> bulbUsers = List.of("electric_floodlight", "small_fluorescent_light", "uv_floodlight");
            helper.assertTrue(BlockEnabledCondition.anyEnabled(bulbUsers), "enabled by default");
            blocks.electricFloodlightEnabled = false;
            blocks.uvFloodlightEnabled = false;
            helper.assertTrue(BlockEnabledCondition.anyEnabled(bulbUsers), "one enabled block is enough");
            blocks.smallElectricFloodlightEnabled = false;
            helper.assertTrue(!BlockEnabledCondition.anyEnabled(bulbUsers), "all disabled");
        } finally {
            blocks.electricFloodlightEnabled = electric;
            blocks.smallElectricFloodlightEnabled = small;
            blocks.uvFloodlightEnabled = uv;
        }
        helper.succeed();
    }
}
