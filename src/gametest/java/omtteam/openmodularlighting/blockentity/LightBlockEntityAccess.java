package omtteam.openmodularlighting.blockentity;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

/**
 * Lets game tests call the version-independent save/load methods of {@link LightBlockEntity}.
 */
public final class LightBlockEntityAccess {
    private LightBlockEntityAccess() {
    }

    public static CompoundTag save(LightBlockEntity light, HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        light.writeData(tag, registries);
        return tag;
    }

    public static void load(LightBlockEntity light, CompoundTag tag, HolderLookup.Provider registries) {
        light.readData(tag, registries);
    }

    public static CompoundTag clientData(LightBlockEntity light, HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        light.writeClientData(tag, registries);
        return tag;
    }
}
