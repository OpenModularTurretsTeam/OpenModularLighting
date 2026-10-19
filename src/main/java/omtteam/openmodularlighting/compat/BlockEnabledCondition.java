package omtteam.openmodularlighting.compat;

import net.minecraft.resources.ResourceLocation;
import omtteam.openmodularlighting.OML;
import omtteam.openmodularlighting.config.OMLConfig;

import java.util.List;

/**
 * Resource condition {@code openmodularlighting:block_enabled}: the recipe loads only if at least one of its
 * {@code blocks} (registry paths) is enabled in the config (1.7.10: {@code RecipeHandler} skipped recipes of disabled
 * blocks). Each loader registers it with its own condition API ({@code platform/<loader>}).
 */
public final class BlockEnabledCondition {
    public static final ResourceLocation ID = OML.id("block_enabled");
    /** JSON field with the registry paths of the blocks. */
    public static final String BLOCKS = "blocks";

    private BlockEnabledCondition() {
    }

    /** Whether any of the blocks is enabled in the config in effect. */
    public static boolean anyEnabled(List<String> blocks) {
        return blocks.stream().anyMatch(OMLConfig.get().blocks::isEnabled);
    }
}
