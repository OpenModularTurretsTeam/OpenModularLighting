package omtteam.openmodularlighting.tileentity;

import net.minecraft.nbt.NBTTagCompound;
import omtteam.openmodularlighting.config.ConfigData;
import omtteam.openmodularlighting.config.OMLConfig;
import omtteam.openmodularlighting.core.beam.BeamMode;
import omtteam.openmodularlighting.core.beam.BeamShape;

/**
 * The electric and carbon floodlights: straight, narrow cone and wide cone ({@link BeamMode}).
 */
public abstract class BeamFloodlightTileEntity extends FloodlightTileEntity {
    private static final String MODE = "mode";

    private BeamMode mode = BeamMode.STRAIGHT;

    public BeamMode getMode() {
        return mode;
    }

    /** Changes the mode and re-casts at once (1.7.10 {@code changeMode}). */
    public void setMode(BeamMode mode) {
        if (this.mode == mode) return;
        this.mode = mode;
        shapeChanged();
    }

    public void cycleMode() {
        setMode(mode.next());
    }

    @Override
    public String modeKey() {
        switch (mode) {
            case NARROW_CONE:
                return "gui.openmodularlighting.narrow_cone";
            case WIDE_CONE:
                return "gui.openmodularlighting.wide_cone";
            default:
                return "gui.openmodularlighting.straight";
        }
    }

    @Override
    protected BeamShape currentShape() {
        ConfigData.General config = OMLConfig.get().general;
        return mode.shape(config.rangeStraightFloodlight, config.rangeConeFloodlight);
    }

    @Override
    protected int[] shapeKey() {
        ConfigData.General config = OMLConfig.get().general;
        return new int[]{mode.ordinal(), config.rangeStraightFloodlight, config.rangeConeFloodlight};
    }

    @Override
    public boolean hasCustomSettings() {
        return super.hasCustomSettings() || mode != BeamMode.STRAIGHT;
    }

    @Override
    protected void writeSettings(NBTTagCompound tag) {
        super.writeSettings(tag);
        tag.setInteger(MODE, mode.ordinal());
    }

    @Override
    protected void readSettings(NBTTagCompound tag) {
        mode = BeamMode.byOrdinal(tag.getInteger(MODE));
        super.readSettings(tag);
    }
}
