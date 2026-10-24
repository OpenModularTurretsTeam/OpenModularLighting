package omtteam.openmodularlighting.camo;

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;

/**
 * The disguise of a light and its NBT form (as in OpenModularTurrets).
 */
public final class CamoData {
    private static final String KEY = "camo";

    @Nullable
    private IBlockState camo;

    @Nullable
    public IBlockState get() {
        return camo;
    }

    /** @return whether the disguise changed */
    public boolean set(@Nullable IBlockState state) {
        if (camo == state) return false;
        camo = state;
        return true;
    }

    public void write(NBTTagCompound tag) {
        if (camo != null) {
            tag.setTag(KEY, NBTUtil.writeBlockState(new NBTTagCompound(), camo));
        }
    }

    /**
     * Reads what {@link #write} wrote; no disguise if the tag has none, or if it is no longer valid (the block was
     * removed, or the rules changed).
     *
     * @return whether the disguise changed
     */
    public boolean read(NBTTagCompound tag) {
        IBlockState state = null;
        if (tag.hasKey(KEY, Constants.NBT.TAG_COMPOUND)) {
            IBlockState read = NBTUtil.readBlockState(tag.getCompoundTag(KEY));
            state = CamoRules.isValidDisguise(read) ? read : null;
        }
        return set(state);
    }
}
