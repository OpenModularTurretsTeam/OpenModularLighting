package omtteam.openmodularlighting.gametest;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Saves and loads block entities through vanilla's public methods (which call {@code writeData} / {@code readData} /
 * {@code writeClientData} of {@code CompatBlockEntity}). Test classes must not sit in the main mod's packages: NeoForge
 * loads the test mod as its own module, and two modules cannot share a package.
 */
public final class BlockEntityData {
    private BlockEntityData() {
    }

    public static CompoundTag save(BlockEntity blockEntity, HolderLookup.Provider registries) {
        return blockEntity.saveWithoutMetadata(registries);
    }

    public static void load(BlockEntity blockEntity, CompoundTag tag, HolderLookup.Provider registries) {
        blockEntity.loadWithComponents(tag, registries);
    }

    /** What the block entity sends to clients (its update tag). */
    public static CompoundTag clientData(BlockEntity blockEntity, HolderLookup.Provider registries) {
        return blockEntity.getUpdateTag(registries);
    }
}
