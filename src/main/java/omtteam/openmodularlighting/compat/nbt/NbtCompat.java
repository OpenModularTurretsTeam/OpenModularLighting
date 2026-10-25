package omtteam.openmodularlighting.compat.nbt;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Reads NBT the same way on every supported version: 1.21.11 returns {@code Optional}s, the older versions defaults.
 * A missing key or a wrong type reads as the fallback (or empty) everywhere. Read NBT only through these methods.
 * <p>
 * Depends on Minecraft only, so any package may use it (it is outside the package cycle around {@code compat}).
 */
public final class NbtCompat {
    private NbtCompat() {
    }

    public static int getInt(CompoundTag tag, String key, int fallback) {
        //? if >=1.21.11 {
        /*return tag.getIntOr(key, fallback);
        *///?} else {
        return tag.contains(key, Tag.TAG_ANY_NUMERIC) ? tag.getInt(key) : fallback;
        //?}
    }

    public static long getLong(CompoundTag tag, String key, long fallback) {
        //? if >=1.21.11 {
        /*return tag.getLongOr(key, fallback);
        *///?} else {
        return tag.contains(key, Tag.TAG_ANY_NUMERIC) ? tag.getLong(key) : fallback;
        //?}
    }

    public static boolean getBoolean(CompoundTag tag, String key, boolean fallback) {
        //? if >=1.21.11 {
        /*return tag.getBooleanOr(key, fallback);
        *///?} else {
        return tag.contains(key, Tag.TAG_ANY_NUMERIC) ? tag.getBoolean(key) : fallback;
        //?}
    }

    public static String getString(CompoundTag tag, String key, String fallback) {
        //? if >=1.21.11 {
        /*return tag.getStringOr(key, fallback);
        *///?} else {
        return tag.contains(key, Tag.TAG_STRING) ? tag.getString(key) : fallback;
        //?}
    }

    /** The int array under {@code key}, empty if there is none. */
    public static int[] getIntArray(CompoundTag tag, String key) {
        //? if >=1.21.11 {
        /*return tag.getIntArray(key).orElseGet(() -> new int[0]);
        *///?} else {
        return tag.getIntArray(key);
        //?}
    }

    /** The long array under {@code key}, empty if there is none. */
    public static long[] getLongArray(CompoundTag tag, String key) {
        //? if >=1.21.11 {
        /*return tag.getLongArray(key).orElseGet(() -> new long[0]);
        *///?} else {
        return tag.getLongArray(key);
        //?}
    }

    /** The compound under {@code key}, empty if there is none. */
    public static CompoundTag getCompound(CompoundTag tag, String key) {
        //? if >=1.21.11 {
        /*return tag.getCompoundOrEmpty(key);
        *///?} else {
        return tag.getCompound(key);
        //?}
    }

    /** A block state written by {@link NbtUtils#writeBlockState}; air if it is invalid. */
    public static BlockState readBlockState(CompoundTag tag) {
        //? if >=1.21.11 {
        /*return NbtUtils.readBlockState(BuiltInRegistries.BLOCK, tag);
        *///?} else {
        return NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), tag);
        //?}
    }
}
