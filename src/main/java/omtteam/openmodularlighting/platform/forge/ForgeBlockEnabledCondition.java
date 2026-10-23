package omtteam.openmodularlighting.platform.forge;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.IConditionSerializer;
import omtteam.openmodularlighting.compat.BlockEnabledCondition;

import java.util.ArrayList;
import java.util.List;

/**
 * Forge registration of {@link BlockEnabledCondition} ({@code ICondition}, its {@link #SERIALIZER} registered under
 * {@link BlockEnabledCondition#ID} with {@code CraftingHelper.register}). Forge 1.20.1 reads it on recipes and
 * advancements.
 * <p>
 * Recipe JSON: {@code "conditions": [{"type": "openmodularlighting:block_enabled", "blocks": [...]}]}.
 */
public record ForgeBlockEnabledCondition(List<String> blocks) implements ICondition {
    public static final IConditionSerializer<ForgeBlockEnabledCondition> SERIALIZER = new IConditionSerializer<>() {
        @Override
        public void write(JsonObject json, ForgeBlockEnabledCondition condition) {
            JsonArray array = new JsonArray();
            condition.blocks().forEach(array::add);
            json.add(BlockEnabledCondition.BLOCKS, array);
        }

        @Override
        public ForgeBlockEnabledCondition read(JsonObject json) {
            List<String> blocks = new ArrayList<>();
            for (JsonElement element : GsonHelper.getAsJsonArray(json, BlockEnabledCondition.BLOCKS)) {
                blocks.add(element.getAsString());
            }
            return new ForgeBlockEnabledCondition(blocks);
        }

        @Override
        public ResourceLocation getID() {
            return BlockEnabledCondition.ID;
        }
    };

    @Override
    public ResourceLocation getID() {
        return BlockEnabledCondition.ID;
    }

    @Override
    public boolean test(IContext context) {
        return BlockEnabledCondition.anyEnabled(blocks);
    }
}
