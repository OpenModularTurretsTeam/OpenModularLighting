package omtteam.openmodularlighting.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;
import omtteam.openmodularlighting.registry.ModBlocks;
import net.minecraft.core.HolderLookup;

import java.util.concurrent.CompletableFuture;

public class OMLBlockLootProvider extends FabricBlockLootTableProvider {
    public OMLBlockLootProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    public void generate() {
        // FloodlightBlock#getDrops adds the settings to the dropped item
        ModBlocks.LIGHT_SOURCES.forEach(this::dropSelf);
        dropSelf(ModBlocks.LIGHTPROOF_GLASS);
    }
}
