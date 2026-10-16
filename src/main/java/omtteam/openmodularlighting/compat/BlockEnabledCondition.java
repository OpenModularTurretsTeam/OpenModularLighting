package omtteam.openmodularlighting.compat;

import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.minecraft.resources.ResourceLocation;
import omtteam.openmodularlighting.OML;
import omtteam.openmodularlighting.config.OMLConfig;
//? if >=1.21 {
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditionType;
import net.minecraft.core.HolderLookup;
import org.jetbrains.annotations.Nullable;
//?} else {
/*import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.resource.conditions.v1.ConditionJsonProvider;
import net.minecraft.util.GsonHelper;

import java.util.ArrayList;
*///?}

import java.util.List;

/**
 * Resource condition {@code openmodularlighting:block_enabled}: the recipe loads only if at least one of its
 * {@code blocks} (registry paths) is enabled in the config (1.7.10: {@code RecipeHandler} skipped recipes of disabled
 * blocks). The condition API differs between 1.20.1 ({@code ConditionJsonProvider}) and 1.21 ({@code ResourceCondition}).
 * <p>
 * Recipe JSON: {@code "fabric:load_conditions": [{"condition": "openmodularlighting:block_enabled", "blocks": [...]}]}.
 */
public final class BlockEnabledCondition {
    public static final ResourceLocation ID = OML.id("block_enabled");
    private static final String BLOCKS = "blocks";

    private BlockEnabledCondition() {
    }

    /** Whether any of the blocks is enabled in the config in effect. */
    public static boolean anyEnabled(List<String> blocks) {
        return blocks.stream().anyMatch(OMLConfig.get().blocks::isEnabled);
    }

    //? if >=1.21 {
    private record Condition(List<String> blocks) implements ResourceCondition {
        static final MapCodec<Condition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.STRING.listOf().fieldOf(BLOCKS).forGetter(Condition::blocks)).apply(instance, Condition::new));
        static final ResourceConditionType<Condition> TYPE = ResourceConditionType.create(ID, CODEC);

        @Override
        public ResourceConditionType<?> getType() {
            return TYPE;
        }

        @Override
        public boolean test(@Nullable HolderLookup.Provider registries) {
            return anyEnabled(blocks);
        }
    }

    /** Registers the condition type; call from common init. */
    public static void register() {
        ResourceConditions.register(Condition.TYPE);
    }

    /** The condition for datagen ({@code FabricRecipeProvider#withConditions}). */
    public static ResourceCondition of(String... blocks) {
        return new Condition(List.of(blocks));
    }
    //?} else {
    /*/^* Registers the condition type; call from common init. ^/
    public static void register() {
        ResourceConditions.register(ID, json -> {
            JsonArray array = GsonHelper.getAsJsonArray(json, BLOCKS);
            List<String> blocks = new ArrayList<>();
            for (JsonElement element : array) {
                blocks.add(element.getAsString());
            }
            return anyEnabled(blocks);
        });
    }

    /^* The condition for datagen ({@code FabricRecipeProvider#withConditions}). ^/
    public static ConditionJsonProvider of(String... blocks) {
        return new ConditionJsonProvider() {
            @Override
            public ResourceLocation getConditionId() {
                return ID;
            }

            @Override
            public void writeParameters(JsonObject object) {
                JsonArray array = new JsonArray();
                for (String block : blocks) {
                    array.add(block);
                }
                object.add(BLOCKS, array);
            }
        };
    }
    *///?}
}
