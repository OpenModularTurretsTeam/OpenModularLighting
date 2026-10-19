package omtteam.openmodularlighting.platform.neoforge;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.neoforged.neoforge.common.conditions.ICondition;
import omtteam.openmodularlighting.compat.BlockEnabledCondition;

import java.util.List;

/**
 * NeoForge registration of {@link BlockEnabledCondition} ({@code ICondition}, registered under
 * {@link BlockEnabledCondition#ID} in {@code NeoForgeRegistries.CONDITION_SERIALIZERS}).
 * <p>
 * Recipe JSON: {@code "neoforge:conditions": [{"type": "openmodularlighting:block_enabled", "blocks": [...]}]}.
 */
public record NeoForgeBlockEnabledCondition(List<String> blocks) implements ICondition {
    public static final MapCodec<NeoForgeBlockEnabledCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.listOf().fieldOf(BlockEnabledCondition.BLOCKS).forGetter(NeoForgeBlockEnabledCondition::blocks))
            .apply(instance, NeoForgeBlockEnabledCondition::new));

    @Override
    public boolean test(IContext context) {
        return BlockEnabledCondition.anyEnabled(blocks);
    }

    @Override
    public MapCodec<? extends ICondition> codec() {
        return CODEC;
    }
}
