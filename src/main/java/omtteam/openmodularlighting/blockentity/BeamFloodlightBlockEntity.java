package omtteam.openmodularlighting.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import omtteam.omlib.compat.nbt.NbtCompat;
import omtteam.openmodularlighting.config.ConfigData;
import omtteam.openmodularlighting.config.OMLConfig;
import omtteam.openmodularlighting.core.beam.BeamMode;
import omtteam.openmodularlighting.core.beam.BeamShape;

/**
 * The electric and carbon floodlights: straight, narrow cone and wide cone ({@link BeamMode}).
 */
public abstract class BeamFloodlightBlockEntity extends FloodlightBlockEntity {
    private static final String MODE = "mode";

    private BeamMode mode = BeamMode.STRAIGHT;

    protected BeamFloodlightBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

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
        return switch (mode) {
            case STRAIGHT -> "gui.openmodularlighting.straight";
            case NARROW_CONE -> "gui.openmodularlighting.narrow_cone";
            case WIDE_CONE -> "gui.openmodularlighting.wide_cone";
        };
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
    protected void writeSettings(CompoundTag tag) {
        super.writeSettings(tag);
        tag.putInt(MODE, mode.ordinal());
    }

    @Override
    protected void readSettings(CompoundTag tag) {
        mode = BeamMode.byOrdinal(NbtCompat.getInt(tag, MODE, 0));
        super.readSettings(tag);
    }
}
