package omtteam.openmodularlighting.camo;

import net.minecraft.block.state.IBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;

/**
 * Marker for lights that can look like another block (taken over from OpenModularTurrets): the electric, carbon and UV
 * floodlights. Their GUI gets the disguise slot, and the client wraps their models so the disguise is drawn with the
 * lamp on top ({@code client.camo.CamoModel}). The block hands the disguise to the model as {@link #CAMO} in its
 * extended state.
 */
public interface CamoBlock {
    /** The disguise in the extended blockstate; absent (null) for the light's own look. */
    IUnlistedProperty<IBlockState> CAMO = new CamoProperty();

    /** Unlisted blockstate property holding a block state. */
    final class CamoProperty implements IUnlistedProperty<IBlockState> {
        private CamoProperty() {
        }

        @Override
        public String getName() {
            return "camo";
        }

        @Override
        public boolean isValid(IBlockState value) {
            return true;
        }

        @Override
        public Class<IBlockState> getType() {
            return IBlockState.class;
        }

        @Override
        public String valueToString(IBlockState value) {
            return value.toString();
        }
    }
}
