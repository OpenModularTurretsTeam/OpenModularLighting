package omtteam.openmodularlighting.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;
import omtteam.openmodularlighting.registry.ModBlocks;

public class OMLBlockLootProvider extends FabricBlockLootTableProvider {
    public OMLBlockLootProvider(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generate() {
        // FloodlightBlock#getDrops adds the settings to the dropped item
        ModBlocks.LIGHT_SOURCES.forEach(this::dropSelf);
        dropSelf(ModBlocks.LIGHTPROOF_GLASS);
    }
}
