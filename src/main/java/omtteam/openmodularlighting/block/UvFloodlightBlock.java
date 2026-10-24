package omtteam.openmodularlighting.block;

import net.minecraft.block.material.Material;
import omtteam.openmodularlighting.blockentity.FloodlightBlockEntity;
import omtteam.openmodularlighting.blockentity.UvFloodlightBlockEntity;
import omtteam.openmodularlighting.camo.CamoBlock;

/**
 * UV floodlight (1.7.10 {@code BlockUVLight}): a floodlight block with a straight beam of UV light. No mode.
 */
public class UvFloodlightBlock extends FloodlightBlock implements CamoBlock {
    public UvFloodlightBlock() {
        super(Material.ROCK);
    }

    @Override
    protected FloodlightBlockEntity createLight() {
        return new UvFloodlightBlockEntity();
    }
}
