package omtteam.openmodularlighting.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import omtteam.openmodularlighting.block.FluorescentLightBlock;
import omtteam.openmodularlighting.registry.ModBlocks;

import java.util.List;
import java.util.concurrent.CompletableFuture;
//? if >=1.21.11 {
/*import net.minecraft.data.tags.TagAppender;
*///?}

public class OMLBlockTagProvider extends FabricTagProvider.BlockTagProvider {
    public OMLBlockTagProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void addTags(HolderLookup.Provider registries) {
        // Light blocks behave like air: players, trees and other blocks may replace them (CLAUDE.md §6.3)
        for (Block light : List.of(ModBlocks.PHANTOM_LIGHT, ModBlocks.UV_LIGHT)) {
            tagBuilder(BlockTags.REPLACEABLE).add(light);
            tagBuilder(BlockTags.REPLACEABLE_BY_TREES).add(light);
        }
        // 1.7.10: Material.rock -> pickaxe; setHarvestLevel("pickaxe", 1) -> stone pickaxe (not set on the
        // fluorescent lights)
        ModBlocks.LIGHT_SOURCES.forEach(tagBuilder(BlockTags.MINEABLE_WITH_PICKAXE)::add);
        ModBlocks.LIGHT_SOURCES.stream().filter(block -> !(block instanceof FluorescentLightBlock))
                .forEach(tagBuilder(BlockTags.NEEDS_STONE_TOOL)::add);
    }

    /** The builder that adds blocks by value (renamed in the Fabric API for 1.21.5). */
    //? if >=1.21.11 {
    /*private TagAppender<Block, Block> tagBuilder(TagKey<Block> tag) {
        return valueLookupBuilder(tag);
    }
    *///?} else {
    private FabricTagBuilder tagBuilder(TagKey<Block> tag) {
        return getOrCreateTagBuilder(tag);
    }
    //?}
}
