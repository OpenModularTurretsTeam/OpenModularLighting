package omtteam.openmodularlighting.compat;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
//? if fabric {
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
//?}
//? if >=1.21 {
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.component.CustomData;
//?}

import java.util.function.Consumer;

/**
 * Hides differences between the supported Minecraft versions.
 * Version-conditional (Stonecutter) code should live here instead of in gameplay code.
 * <p>
 * Methods taking a {@link HolderLookup.Provider} need it on 1.21+; on 1.20.1 it is ignored.
 */
public final class VersionCompat {
    private VersionCompat() {
    }

    // ---- Resource locations ----

    public static ResourceLocation id(String namespace, String path) {
        //? if >=1.21 {
        return ResourceLocation.fromNamespaceAndPath(namespace, path);
        //?} else {
        /*return new ResourceLocation(namespace, path);
        *///?}
    }

    // ---- ItemStack NBT ----

    /**
     * Serializes a stack (empty stacks become an empty tag).
     */
    public static CompoundTag saveStack(ItemStack stack, HolderLookup.Provider registries) {
        //? if >=1.21 {
        Tag tag = stack.saveOptional(registries);
        return tag instanceof CompoundTag compound ? compound : new CompoundTag();
        //?} else {
        /*return stack.save(new CompoundTag());
        *///?}
    }

    /**
     * Deserializes a stack written by {@link #saveStack}; returns {@link ItemStack#EMPTY} on invalid data.
     */
    public static ItemStack loadStack(CompoundTag tag, HolderLookup.Provider registries) {
        //? if >=1.21 {
        return ItemStack.parseOptional(registries, tag);
        //?} else {
        /*return ItemStack.of(tag);
        *///?}
    }

    /**
     * Writes a slot list to {@code tag} under {@code "Items"} (vanilla container format).
     */
    public static void saveItems(CompoundTag tag, NonNullList<ItemStack> items, HolderLookup.Provider registries) {
        //? if >=1.21 {
        ContainerHelper.saveAllItems(tag, items, registries);
        //?} else {
        /*ContainerHelper.saveAllItems(tag, items);
        *///?}
    }

    /**
     * Reads a slot list written by {@link #saveItems}. {@code items} must already have the correct size.
     */
    public static void loadItems(CompoundTag tag, NonNullList<ItemStack> items, HolderLookup.Provider registries) {
        //? if >=1.21 {
        ContainerHelper.loadAllItems(tag, items, registries);
        //?} else {
        /*ContainerHelper.loadAllItems(tag, items);
        *///?}
    }

    // ---- Custom item data (NBT on 1.20.1, CUSTOM_DATA component on 1.21+) ----

    public static boolean hasCustomData(ItemStack stack) {
        //? if >=1.21 {
        return stack.has(DataComponents.CUSTOM_DATA);
        //?} else {
        /*return stack.hasTag();
        *///?}
    }

    /**
     * Returns a <b>copy</b> of the custom data of a stack (empty tag if none). Use {@link #setCustomData}
     * or {@link #updateCustomData} to change it.
     */
    public static CompoundTag getCustomData(ItemStack stack) {
        //? if >=1.21 {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        //?} else {
        /*CompoundTag tag = stack.getTag();
        return tag == null ? new CompoundTag() : tag.copy();
        *///?}
    }

    /**
     * Replaces the custom data of a stack. An empty tag removes it.
     */
    public static void setCustomData(ItemStack stack, CompoundTag tag) {
        //? if >=1.21 {
        CustomData.set(DataComponents.CUSTOM_DATA, stack, tag);
        //?} else {
        /*stack.setTag(tag.isEmpty() ? null : tag.copy());
        *///?}
    }

    /**
     * Modifies the custom data of a stack in place.
     */
    public static void updateCustomData(ItemStack stack, Consumer<CompoundTag> updater) {
        CompoundTag tag = getCustomData(stack);
        updater.accept(tag);
        setCustomData(stack, tag);
    }

    // ---- Blocks ----

    /** Whether bonemeal would work on this block (1.20.1 had an extra {@code isClient} parameter). */
    public static boolean isValidBonemealTarget(BonemealableBlock block, LevelReader level, BlockPos pos, BlockState state) {
        //? if >=1.21 {
        return block.isValidBonemealTarget(level, pos, state);
        //?} else {
        /*return block.isValidBonemealTarget(level, pos, state, false);
        *///?}
    }

    // ---- Block entity data on block items (BlockEntityTag on 1.20.1, BLOCK_ENTITY_DATA component on 1.21+) ----

    /**
     * Stores block entity data on a block item; vanilla loads it into the block entity when the block is placed.
     */
    public static void setBlockEntityData(ItemStack stack, BlockEntityType<?> type, CompoundTag tag) {
        BlockItem.setBlockEntityData(stack, type, tag);
    }

    public static boolean hasBlockEntityData(ItemStack stack) {
        //? if >=1.21 {
        return stack.has(DataComponents.BLOCK_ENTITY_DATA);
        //?} else {
        /*return BlockItem.getBlockEntityData(stack) != null;
        *///?}
    }

    /**
     * Builder of a mod creative tab. Fabric places mod tabs on its own tab pages ({@code FabricItemGroup}); NeoForge
     * sorts mod tabs itself.
     */
    public static CreativeModeTab.Builder creativeTabBuilder() {
        //? if fabric {
        return FabricItemGroup.builder();
        //?} else {
        /*return CreativeModeTab.builder();
        *///?}
    }
}
