package omtteam.openmodularlighting.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import omtteam.openmodularlighting.block.FluorescentLightBlock;
import omtteam.openmodularlighting.registry.ModBlocks;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class OMLBlockTagProvider extends FabricTagProvider.BlockTagProvider {
    public OMLBlockTagProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void addTags(HolderLookup.Provider registries) {
        // Light blocks behave like air: players, trees and other blocks may replace them (CLAUDE.md §6.3)
        for (Block light : List.of(ModBlocks.PHANTOM_LIGHT, ModBlocks.UV_LIGHT)) {
            getOrCreateTagBuilder(BlockTags.REPLACEABLE).add(light);
            getOrCreateTagBuilder(BlockTags.REPLACEABLE_BY_TREES).add(light);
        }
        // 1.7.10: Material.rock -> pickaxe; setHarvestLevel("pickaxe", 1) -> stone pickaxe (not set on the
        // fluorescent lights)
        ModBlocks.LIGHT_SOURCES.forEach(getOrCreateTagBuilder(BlockTags.MINEABLE_WITH_PICKAXE)::add);
        ModBlocks.LIGHT_SOURCES.stream().filter(block -> !(block instanceof FluorescentLightBlock))
                .forEach(getOrCreateTagBuilder(BlockTags.NEEDS_STONE_TOOL)::add);
    }
}
