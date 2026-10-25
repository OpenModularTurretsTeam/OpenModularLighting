package omtteam.openmodularlighting.tileentity;

import net.minecraft.nbt.NBTTagCompound;
import omtteam.openmodularlighting.config.OMLConfig;
import omtteam.openmodularlighting.core.beam.BeamShape;
import omtteam.openmodularlighting.energy.ElectricPower;
import omtteam.openmodularlighting.energy.LightPower;

/**
 * Small and square fluorescent lights (1.7.10 {@code TileEntitySmallFloodlight}): light the block in front and the 4
 * blocks beside them; {@code energyUsageSmallFloodlight} per tick, energy only from the back. No timeout after
 * turning off.
 */
public class FluorescentLightTileEntity extends PanelLightTileEntity {
    private static final int[] SHAPE_KEY = {0};
    private static final String ROTATED = "rotated";

    private final ElectricPower power = new ElectricPower(
            () -> OMLConfig.get().general.energyUsageSmallFloodlight, this::markDirty);

    /** The small light turned by 90° (1.7.10 {@code rotationState}); not kept on the dropped item. */
    private boolean rotated;

    public boolean isRotated() {
        return rotated;
    }

    /** Turns the small light's strip by 90°; the client redraws it, the beam does not change. */
    public void toggleRotated() {
        rotated = !rotated;
        syncToClient();
    }

    @Override
    protected void writeData(NBTTagCompound tag) {
        super.writeData(tag);
        tag.setBoolean(ROTATED, rotated);
    }

    @Override
    protected void readData(NBTTagCompound tag) {
        boolean wasRotated = rotated;
        super.readData(tag);
        rotated = tag.getBoolean(ROTATED);
        if (wasRotated != rotated) {
            remesh();
        }
    }

    @Override
    protected void writeClientData(NBTTagCompound tag) {
        super.writeClientData(tag);
        tag.setBoolean(ROTATED, rotated);
    }

    @Override
    protected LightPower power() {
        return power;
    }

    @Override
    protected BeamShape currentShape() {
        return BeamShape.fluorescent();
    }

    @Override
    protected int[] shapeKey() {
        return SHAPE_KEY;
    }

    @Override
    protected int timeoutTicks() {
        return 0;
    }
}
