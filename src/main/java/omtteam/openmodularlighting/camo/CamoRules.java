package omtteam.openmodularlighting.camo;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumBlockRenderType;

import javax.annotation.Nullable;

/**
 * Which blocks may be used as a disguise, and the item form of one. Only full, opaque blocks (stone, planks, wool...):
 * a floodlight hides the faces of its neighbours like any solid block, so a see-through disguise (glass, leaves) would
 * show holes into the world behind it.
 */
public final class CamoRules {
    private CamoRules() {
    }

    /** A full, opaque block that is drawn from a normal model and has no block entity of its own. */
    @SuppressWarnings("deprecation") // the state methods are the ones to call
    public static boolean isValidDisguise(IBlockState state) {
        Block block = state.getBlock();
        if (state.getMaterial() == Material.AIR || block.hasTileEntity(state)) return false;
        if (state.getRenderType() != EnumBlockRenderType.MODEL) return false;
        return state.isOpaqueCube() && state.isFullCube();
    }

    /**
     * The disguise a held stack stands for: the item's block in the state of the item's metadata (wool colour, plank
     * type), if it is a valid one.
     */
    @Nullable
    @SuppressWarnings("deprecation") // getStateFromMeta is how 1.12 maps item damage to a state
    public static IBlockState disguiseFor(ItemStack stack) {
        if (stack.isEmpty() || !(stack.getItem() instanceof ItemBlock)) return null;
        ItemBlock item = (ItemBlock) stack.getItem();
        IBlockState state = item.getBlock().getStateFromMeta(item.getMetadata(stack.getMetadata()));
        return isValidDisguise(state) ? state : null;
    }

    /** The item shown in the disguise slot of the GUI. */
    public static ItemStack itemFor(@Nullable IBlockState camo) {
        if (camo == null) return ItemStack.EMPTY;
        Item item = Item.getItemFromBlock(camo.getBlock());
        return item == Items.AIR ? ItemStack.EMPTY
                : new ItemStack(item, 1, camo.getBlock().damageDropped(camo));
    }
}
