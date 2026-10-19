package omtteam.openmodularlighting.camo;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * The disguise of a light and its NBT form (as in OpenModularTurrets).
 */
public final class CamoData {
    private static final String KEY = "camo";

    @Nullable
    private BlockState camo;

    @Nullable
    public BlockState get() {
        return camo;
    }

    /** @return whether the disguise changed */
    public boolean set(@Nullable BlockState state) {
        if (camo == state) return false;
        camo = state;
        return true;
    }

    public void write(CompoundTag tag) {
        if (camo != null) {
            tag.put(KEY, NbtUtils.writeBlockState(camo));
        }
    }

    /**
     * Reads what {@link #write} wrote; no disguise if the tag has none, or if it is no longer valid (the block was
     * removed, or the rules changed).
     *
     * @return whether the disguise changed
     */
    public boolean read(CompoundTag tag) {
        BlockState state = null;
        if (tag.contains(KEY, Tag.TAG_COMPOUND)) {
            BlockState read = NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), tag.getCompound(KEY));
            state = CamoRules.isValidDisguise(read) ? read : null;
        }
        return set(state);
    }
}
