package omtteam.openmodularlighting.registry;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;
import omtteam.openmodularlighting.compat.BlockEnabledCondition;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Recipes that 1.12.2 cannot load from JSON: smelting. The crafting recipes are JSON files in
 * {@code assets/openmodularlighting/recipes}, with the {@code block_enabled} condition ({@link BlockEnabledCondition}).
 */
public final class ModRecipes {
    /** The blocks that need light bulbs: filaments and bulbs are only craftable if one of them is enabled. */
    public static final List<String> BULB_USERS = Collections.unmodifiableList(Arrays.asList("electric_floodlight",
            "small_fluorescent_light", "uv_floodlight", "grow_light", "cube_light"));
    private static final float FILAMENT_XP = 0.1F;

    private ModRecipes() {
    }

    /** Raw filament smelts into glowing filament (if a block that needs bulbs is enabled). */
    public static void registerSmelting() {
        if (BlockEnabledCondition.anyEnabled(BULB_USERS)) {
            GameRegistry.addSmelting(ModItems.RAW_FILAMENT, new ItemStack(ModItems.GLOWING_FILAMENT), FILAMENT_XP);
        }
    }
}
