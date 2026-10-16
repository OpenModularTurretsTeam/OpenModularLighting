package omtteam.openmodularlighting.gametest;

import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.crafting.RecipeManager;
import omtteam.openmodularlighting.OML;
import omtteam.openmodularlighting.compat.BlockEnabledCondition;
import omtteam.openmodularlighting.config.ConfigData;
import omtteam.openmodularlighting.config.OMLConfig;

import java.util.List;

/**
 * The generated recipes load (their {@code block_enabled} condition is registered and passes with the default
 * config), and the condition follows the config.
 */
public class RecipeGameTest implements FabricGameTest {
    private static final List<String> RECIPES = List.of("raw_filament", "glowing_filament", "light_bulb",
            "electric_floodlight", "carbon_dissolver", "mantle", "carbon_lantern", "carbon_floodlight", "uv_floodlight",
            "small_fluorescent_light", "square_fluorescent_light", "small_fluorescent_light_from_square", "grow_light");

    @GameTest(template = EMPTY_STRUCTURE)
    public void allRecipesLoadWithTheDefaultConfig(GameTestHelper helper) {
        RecipeManager recipes = helper.getLevel().getRecipeManager();
        for (String recipe : RECIPES) {
            helper.assertTrue(recipes.byKey(OML.id(recipe)).isPresent(), "recipe " + recipe + " loaded");
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
