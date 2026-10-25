package omtteam.openmodularlighting.compat;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
//? if fabric {
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
//?}
//? if fabric && >=1.21.11 {
/*import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
*///?}
//? if >=1.21 {
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomData;
//?}
//? if >=1.21.11 {
/*import com.mojang.serialization.MapCodec;
import net.minecraft.nbt.NbtOps;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
*///?}

import java.util.function.BiFunction;
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

    //? if >=1.21.11 {
    /*/^* A whole compound as a map codec: 1.21.11 stores data through ValueOutput, our code still reads and writes NBT. ^/
    static final MapCodec<CompoundTag> WHOLE_TAG = MapCodec.assumeMapUnsafe(CompoundTag.CODEC);
    *///?}

    // ---- Resource locations ----

    public static ResourceLocation id(String namespace, String path) {
        //? if >=1.21 {
        return ResourceLocation.fromNamespaceAndPath(namespace, path);
        //?} else {
        /*return new ResourceLocation(namespace, path);
        *///?}
    }

    // ---- Registration (1.21.11 needs the registry id in the properties before a block or item is created) ----

    /** Properties of the block that will be registered as {@code id}. */
    public static BlockBehaviour.Properties blockProperties(BlockBehaviour.Properties properties, ResourceLocation id) {
        //? if >=1.21.11 {
        /*return properties.setId(ResourceKey.create(Registries.BLOCK, id));
        *///?} else {
        return properties;
        //?}
    }

    /** Properties of the item that will be registered as {@code id}. */
    public static Item.Properties itemProperties(Item.Properties properties, ResourceLocation id) {
        //? if >=1.21.11 {
        /*return properties.setId(ResourceKey.create(Registries.ITEM, id));
        *///?} else {
        return properties;
        //?}
    }

    /**
     * Properties of the block item that will be registered as {@code id}. It keeps the block's name
     * ({@code block.<mod>.<id>}); 1.21.11 would otherwise name it like an item.
     */
    public static Item.Properties blockItemProperties(Item.Properties properties, ResourceLocation id) {
        //? if >=1.21.11 {
        /*return itemProperties(properties, id).useBlockDescriptionPrefix();
        *///?} else {
        return itemProperties(properties, id);
        //?}
    }

    /**
     * A block entity type for these blocks (1.21.11 removed {@code BlockEntityType.Builder}; vanilla's factory interface
     * is only accessible on NeoForge there, Fabric has its own builder).
     */
    public static <T extends BlockEntity> BlockEntityType<T> blockEntityType(BiFunction<BlockPos, BlockState, T> factory,
                                                                             Block... blocks) {
        //? if fabric && >=1.21.11 {
        /*return FabricBlockEntityTypeBuilder.create(factory::apply, blocks).build();
        *///?} else if >=1.21.11 {
        /*return new BlockEntityType<>(factory::apply, blocks);
        *///?} else {
        return BlockEntityType.Builder.of(factory::apply, blocks).build(null);
        //?}
    }

    // ---- ItemStack NBT ----

    /**
     * Serializes a stack (empty stacks become an empty tag).
     */
    public static CompoundTag saveStack(ItemStack stack, HolderLookup.Provider registries) {
        //? if >=1.21.11 {
        /*if (stack.isEmpty()) return new CompoundTag();
        Tag tag = ItemStack.CODEC.encodeStart(registries.createSerializationContext(NbtOps.INSTANCE), stack).getOrThrow();
        return tag instanceof CompoundTag compound ? compound : new CompoundTag();
        *///?} else if >=1.21 {
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
        //? if >=1.21.11 {
        /*return ItemStack.CODEC.parse(registries.createSerializationContext(NbtOps.INSTANCE), tag).result()
                .orElse(ItemStack.EMPTY);
        *///?} else if >=1.21 {
        return ItemStack.parseOptional(registries, tag);
        //?} else {
        /*return ItemStack.of(tag);
        *///?}
    }

    /**
     * Writes a slot list to {@code tag} under {@code "Items"} (vanilla container format).
     */
    public static void saveItems(CompoundTag tag, NonNullList<ItemStack> items, HolderLookup.Provider registries) {
        //? if >=1.21.11 {
        /*TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, registries);
        ContainerHelper.saveAllItems(output, items);
        tag.merge(output.buildResult());
        *///?} else if >=1.21 {
        ContainerHelper.saveAllItems(tag, items, registries);
        //?} else {
        /*ContainerHelper.saveAllItems(tag, items);
        *///?}
    }

    /**
     * Reads a slot list written by {@link #saveItems}. {@code items} must already have the correct size.
     */
    public static void loadItems(CompoundTag tag, NonNullList<ItemStack> items, HolderLookup.Provider registries) {
        //? if >=1.21.11 {
        /*ContainerHelper.loadAllItems(TagValueInput.create(ProblemReporter.DISCARDING, registries, tag), items);
        *///?} else if >=1.21 {
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
        //? if >=1.21.11 {
        /*TagValueOutput output = TagValueOutput.createWithoutContext(ProblemReporter.DISCARDING);
        output.store(WHOLE_TAG, tag);
        BlockItem.setBlockEntityData(stack, type, output);
        *///?} else {
        BlockItem.setBlockEntityData(stack, type, tag);
        //?}
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
