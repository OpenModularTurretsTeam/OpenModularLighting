package omtteam.openmodularlighting.camo;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import omtteam.openmodularlighting.compat.block.BlockCompat;
import org.jetbrains.annotations.Nullable;

/**
 * Which blocks may be used as a disguise, and the item form of one. Only full, opaque blocks (stone, planks, wool...):
 * a floodlight hides the faces of its neighbours like any solid block, so a see-through disguise (glass, leaves) would
 * show holes into the world behind it.
 */
public final class CamoRules {
    private CamoRules() {
    }

    /** A full, opaque block that is drawn from a normal model and has no block entity of its own. */
    public static boolean isValidDisguise(BlockState state) {
        if (state.isAir() || state.getBlock() instanceof EntityBlock) return false;
        if (state.getRenderShape() != RenderShape.MODEL) return false;
        return BlockCompat.isSolidRender(state, EmptyBlockGetter.INSTANCE, BlockPos.ZERO);
    }

    /** The disguise a held stack stands for (the item's block in its default state), if it is a valid one. */
    @Nullable
    public static BlockState disguiseFor(ItemStack stack) {
        if (!(stack.getItem() instanceof BlockItem item)) return null;
        BlockState state = item.getBlock().defaultBlockState();
        return isValidDisguise(state) ? state : null;
    }

    /** The item shown in the disguise slot of the GUI. */
    public static ItemStack itemFor(@Nullable BlockState camo) {
        return camo == null ? ItemStack.EMPTY : new ItemStack(camo.getBlock());
    }
}
