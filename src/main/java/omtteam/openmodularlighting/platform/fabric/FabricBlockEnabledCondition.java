package omtteam.openmodularlighting.platform.fabric;

import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.minecraft.resources.ResourceLocation;
import omtteam.openmodularlighting.compat.BlockEnabledCondition;
//? if >=1.21 {
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditionType;
import org.jetbrains.annotations.Nullable;
//?} else {
/*import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.resource.conditions.v1.ConditionJsonProvider;
import net.minecraft.util.GsonHelper;

import java.util.ArrayList;
*///?}
//? if >=1.21.11 {
/*import net.minecraft.resources.RegistryOps.RegistryInfoLookup;
*///?} else if >=1.21 {
import net.minecraft.core.HolderLookup;
//?}

import java.util.List;

/**
 * Fabric registration of {@link BlockEnabledCondition} (resource conditions). The condition API differs between 1.20.1
 * ({@code ConditionJsonProvider}) and 1.21 ({@code ResourceCondition}, whose {@code test} takes a registry lookup
 * since 1.21.2; the condition does not need it).
 * <p>
 * Recipe JSON: {@code "fabric:load_conditions": [{"condition": "openmodularlighting:block_enabled", "blocks": [...]}]}.
 */
public final class FabricBlockEnabledCondition {
    private static final ResourceLocation ID = BlockEnabledCondition.ID;
    private static final String BLOCKS = BlockEnabledCondition.BLOCKS;

    private FabricBlockEnabledCondition() {
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

        //? if >=1.21.11 {
        /*@Override
        public boolean test(@Nullable RegistryInfoLookup registries) {
        *///?} else {
        @Override
        public boolean test(@Nullable HolderLookup.Provider registries) {
        //?}
            return BlockEnabledCondition.anyEnabled(blocks);
        }
    }

    /** Registers the condition type; called by the Fabric entrypoint. */
    public static void register() {
        ResourceConditions.register(Condition.TYPE);
    }

    /** The condition for datagen ({@code FabricRecipeProvider#withConditions}). */
    public static ResourceCondition of(String... blocks) {
        return new Condition(List.of(blocks));
    }
    //?} else {
    /*/^* Registers the condition type; called by the Fabric entrypoint. ^/
    public static void register() {
        ResourceConditions.register(ID, json -> {
            JsonArray array = GsonHelper.getAsJsonArray(json, BLOCKS);
            List<String> blocks = new ArrayList<>();
            for (JsonElement element : array) {
                blocks.add(element.getAsString());
            }
            return BlockEnabledCondition.anyEnabled(blocks);
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
