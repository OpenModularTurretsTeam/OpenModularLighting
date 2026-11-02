package omtteam.openmodularlighting.tileentity;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Rotation;
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

    /**
     * Structures turn the light: a quarter turn of a light on the floor or ceiling turns its strip too (on a wall it
     * turns with the wall through the facing). Kept here, as the strip's state lives in the tile entity on 1.12.
     */
    @Override
    public void rotate(Rotation rotation) {
        boolean quarter = rotation == Rotation.CLOCKWISE_90 || rotation == Rotation.COUNTERCLOCKWISE_90;
        if (quarter && world != null && facing().getAxis() == EnumFacing.Axis.Y) {
            rotated = !rotated;
            markDirty();
        }
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
