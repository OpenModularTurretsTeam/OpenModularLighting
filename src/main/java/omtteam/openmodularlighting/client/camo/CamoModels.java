package omtteam.openmodularlighting.client.camo;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import omtteam.openmodularlighting.OML;
import omtteam.openmodularlighting.camo.CamoBlock;

/**
 * Which models show a disguise: the block models of the {@link CamoBlock}s (as in OpenModularTurrets). The client
 * wraps them in {@link CamoModel}; the block hands the disguise to the model in its extended state.
 * <p>
 * A disguised light shows the disguise's model and, of its own model, only the lamp: the quads with
 * {@link #LAMP_TINT_INDEX} (a cut-out overlay on the front face, see {@code models/block/floodlight.json}).
 */
public final class CamoModels {
    /** Tint index that marks the lamp quads of a light's model; they get the dye colour like the rest of the light. */
    public static final int LAMP_TINT_INDEX = 1;
    private static final String INVENTORY = "inventory";

    private CamoModels() {
    }

    /** Whether the model with this id is a block model (not the item model) of a block that can be disguised. */
    public static boolean isCamoModel(ModelResourceLocation id) {
        if (!id.getNamespace().equals(OML.MOD_ID) || INVENTORY.equals(id.getVariant())) return false;
        Block block = ForgeRegistries.BLOCKS.getValue(OML.id(id.getPath()));
        return block instanceof CamoBlock;
    }
}
