package omtteam.openmodularlighting.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricDynamicRegistryProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

/**
 * Writes the damage types ({@code data/openmodularlighting/damage_type/*.json}), which are registered in
 * {@link OMLDataGenerator#buildRegistry}.
 */
public class OMLDamageTypeProvider extends FabricDynamicRegistryProvider {
    public OMLDamageTypeProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void configure(HolderLookup.Provider registries, Entries entries) {
        entries.addAll(registries.lookupOrThrow(Registries.DAMAGE_TYPE));
    }

    @Override
    public @NotNull String getName() {
        return "Damage types";
    }
}
