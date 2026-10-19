package omtteam.openmodularlighting.client.camo;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import omtteam.openmodularlighting.OML;
import omtteam.openmodularlighting.camo.CamoBlock;
import omtteam.openmodularlighting.compat.VersionCompat;

/**
 * Which models show a disguise: those of the {@link CamoBlock}s (as in OpenModularTurrets). The loader client
 * entrypoints wrap them in their camouflage model ({@code FabricCamoModel}, {@code NeoForgeCamoModel}); the block
 * entity hands the disguise to the model through {@code CompatBlockEntity#renderData}.
 * <p>
 * A disguised light shows the disguise's model and, of its own model, only the lamp: the quads with
 * {@link #LAMP_TINT_INDEX} (a cut-out overlay on the front face, see {@code models/block/floodlight.json}).
 */
public final class CamoModels {
    /** Tint index that marks the lamp quads of a light's model; they get the dye colour like the rest of the light. */
    public static final int LAMP_TINT_INDEX = 1;

    private CamoModels() {
    }

    /** All blocks that can be disguised (after registration). */
    public static Block[] camoBlocks() {
        return BuiltInRegistries.BLOCK.stream().filter(block -> block instanceof CamoBlock).toArray(Block[]::new);
    }

    /**
     * Whether the model with this id belongs to a block that can be disguised: block state and item models share the
     * block's registry path (only namespace and path of the id are compared).
     */
    public static boolean isCamoModel(ResourceLocation id) {
        if (!id.getNamespace().equals(OML.MOD_ID)) return false;
        // a plain id: on 1.20.1 model ids can be ModelResourceLocations, which never equal a registry key
        return BuiltInRegistries.BLOCK.getOptional(VersionCompat.id(id.getNamespace(), id.getPath()))
                .map(block -> block instanceof CamoBlock).orElse(false);
    }
}
