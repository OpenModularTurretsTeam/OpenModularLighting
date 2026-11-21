package omtteam.openmodularlighting.platform.fabric;

import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.minecraft.resources.ResourceLocation;
import omtteam.openmodularlighting.compat.BlockEnabledCondition;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditionType;
import org.jetbrains.annotations.Nullable;
import net.minecraft.core.HolderLookup;

import java.util.List;

/**
 * Fabric registration of {@link BlockEnabledCondition} (resource conditions: a {@code ResourceCondition}, whose
 * {@code test} takes a registry lookup; the condition does not need it).
 * <p>
 * Recipe JSON: {@code "fabric:load_conditions": [{"condition": "openmodularlighting:block_enabled", "blocks": [...]}]}.
 */
public final class FabricBlockEnabledCondition {
    private static final ResourceLocation ID = BlockEnabledCondition.ID;
    private static final String BLOCKS = BlockEnabledCondition.BLOCKS;

    private FabricBlockEnabledCondition() {
    }

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
}
