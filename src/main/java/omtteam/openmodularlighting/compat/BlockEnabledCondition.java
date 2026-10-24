package omtteam.openmodularlighting.compat;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.util.JsonUtils;
import net.minecraftforge.common.crafting.IConditionFactory;
import net.minecraftforge.common.crafting.JsonContext;
import omtteam.openmodularlighting.config.OMLConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

/**
 * Recipe condition {@code openmodularlighting:block_enabled} (registered in {@code recipes/_factories.json}): the
 * recipe loads only if at least one of its {@code blocks} (registry paths) is enabled in the config (1.7.10:
 * {@code RecipeHandler} skipped recipes of disabled blocks). Recipes load once at start-up, from the local config.
 */
public final class BlockEnabledCondition implements IConditionFactory {
    /** JSON field with the registry paths of the blocks. */
    public static final String BLOCKS = "blocks";

    /** Whether any of the blocks is enabled in the config in effect. */
    public static boolean anyEnabled(List<String> blocks) {
        for (String block : blocks) {
            if (OMLConfig.get().blocks.isEnabled(block)) return true;
        }
        return false;
    }

    @Override
    public BooleanSupplier parse(JsonContext context, JsonObject json) {
        List<String> blocks = new ArrayList<>();
        for (JsonElement block : JsonUtils.getJsonArray(json, BLOCKS)) {
            blocks.add(block.getAsString());
        }
        return () -> anyEnabled(blocks);
    }
}
