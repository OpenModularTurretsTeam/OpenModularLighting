package omtteam.openmodularlighting.block;

import net.minecraft.block.material.Material;
import omtteam.openmodularlighting.tileentity.FloodlightTileEntity;
import omtteam.openmodularlighting.tileentity.UvFloodlightTileEntity;

/**
 * UV floodlight (1.7.10 {@code BlockUVLight}): a floodlight block with a straight beam of UV light. No mode.
 */
public class UvFloodlightBlock extends FloodlightBlock {
    public UvFloodlightBlock() {
        super(Material.ROCK);
    }

    /** Can be disguised (OMLib's camouflage menu). */
    @Override
    public boolean supportsCamo() {
        return true;
    }

    @Override
    protected FloodlightTileEntity createLight() {
        return new UvFloodlightTileEntity();
    }
}
