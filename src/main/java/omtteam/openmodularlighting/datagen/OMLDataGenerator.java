package omtteam.openmodularlighting.datagen;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import omtteam.openmodularlighting.registry.ModDamageTypes;

/**
 * Datagen entrypoint. Generates the version-specific {@code data/} files (recipes, loot tables, tags, …)
 * into {@code versions/<v>/src/main/generated}.
 */
public class OMLDataGenerator implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator generator) {
        FabricDataGenerator.Pack pack = generator.createPack();
        pack.addProvider(OMLBlockTagProvider::new);
        pack.addProvider(OMLBlockLootProvider::new);
        pack.addProvider(OMLRecipeProvider::new);
        pack.addProvider(OMLDamageTypeProvider::new);
    }

    @Override
    public void buildRegistry(RegistrySetBuilder builder) {
        builder.add(Registries.DAMAGE_TYPE, context -> context.register(ModDamageTypes.UV_LIGHT, ModDamageTypes.uvLight()));
    }
}
