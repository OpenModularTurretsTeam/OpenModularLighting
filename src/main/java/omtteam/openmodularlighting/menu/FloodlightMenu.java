package omtteam.openmodularlighting.menu;

import omtteam.omlib.menu.CamoMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import omtteam.openmodularlighting.block.CarbonFloodlightBlock;
import omtteam.openmodularlighting.block.ElectricFloodlightBlock;
import omtteam.openmodularlighting.blockentity.FloodlightBlockEntity;
import omtteam.omlib.camo.CamoBlock;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Floodlight GUI: the light's slot (battery or fuel) and the player inventory, laid out as in 1.7.10
 * ({@code ContainerElectricFloodlight}, 176 × 146). Values for the status line are synced as container data.
 * <p>
 * Lights that can be disguised ({@link CamoBlock}) get a button for the camouflage menu ({@code CamoMenu}) in the screen.
 */
public abstract class FloodlightMenu extends AbstractContainerMenu {
    private static final double MAX_DISTANCE_SQR = 64.0;
    /** Index of the light's slot, after the 36 player slots. */
    private static final int LIGHT_SLOT = 36;

    private final BlockPos pos;
    @Nullable
    private final FloodlightBlockEntity light;
    private final boolean camo;
    private final boolean timeoutHint;
    protected final ContainerData data;

    protected FloodlightMenu(MenuType<?> type, int containerId, Inventory playerInventory, BlockPos pos,
                             @Nullable FloodlightBlockEntity light, Container slotContainer, ContainerData data) {
        super(type, containerId);
        this.pos = pos;
        this.light = light;
        this.data = data;
        // the block at pos: the same on both sides, so both create the same slots
        Block block = playerInventory.player.level().getBlockState(pos).getBlock();
        this.camo = block instanceof CamoBlock;
        this.timeoutHint = block instanceof ElectricFloodlightBlock || block instanceof CarbonFloodlightBlock;
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 9; x++) {
                addSlot(new Slot(playerInventory, 9 + x + y * 9, 8 + x * 18, 58 + y * 18));
            }
        }
        for (int x = 0; x < 9; x++) {
            addSlot(new Slot(playerInventory, x, 8 + x * 18, 116));
        }
        addSlot(new Slot(slotContainer, 0, 26, 22) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return isValidItem(stack);
            }
        });
        addDataSlots(data);
    }

    /** What the light's slot accepts (battery or fuel). */
    protected abstract boolean isValidItem(ItemStack stack);

    /** Whether this light can be disguised: the GUI shows the button for the camouflage menu. */
    public boolean supportsCamo() {
        return camo;
    }

    /** Whether the GUI explains the timeout after turning off (the electric and carbon floodlights). */
    public boolean hasTimeoutHint() {
        return timeoutHint;
    }

    public BlockPos getPos() {
        return pos;
    }

    @Override
    public @NotNull ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        if (index < LIGHT_SLOT) {
            if (!isValidItem(stack)) {
                return ItemStack.EMPTY;
            }
            if (!moveItemStackTo(stack, LIGHT_SLOT, LIGHT_SLOT + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(stack, 0, LIGHT_SLOT, false)) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return stack.getCount() == original.getCount() ? ItemStack.EMPTY : original;
    }

    @Override
    public boolean stillValid(Player player) {
        if (light == null) return true; // client side
        return !light.isRemoved() && player.distanceToSqr(Vec3.atCenterOf(pos)) < MAX_DISTANCE_SQR;
    }
}
