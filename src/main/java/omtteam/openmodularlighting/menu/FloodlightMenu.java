package omtteam.openmodularlighting.menu;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import omtteam.openmodularlighting.blockentity.FloodlightBlockEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

/**
 * Floodlight GUI: the light's slot (battery or fuel) and the player inventory, laid out as in 1.7.10
 * ({@code ContainerElectricFloodlight}, 176 × 146). Values for the status line are synced as container data.
 */
public abstract class FloodlightMenu extends AbstractContainerMenu {
    private static final double MAX_DISTANCE_SQR = 64.0;
    /** Index of the light's slot, after the 36 player slots. */
    private static final int LIGHT_SLOT = 36;

    private final BlockPos pos;
    @Nullable
    private final FloodlightBlockEntity light;
    private final Predicate<ItemStack> validItem;
    protected final ContainerData data;

    protected FloodlightMenu(MenuType<?> type, int containerId, Inventory playerInventory, BlockPos pos,
                             @Nullable FloodlightBlockEntity light, Container slotContainer, ContainerData data,
                             Predicate<ItemStack> validItem) {
        super(type, containerId);
        this.pos = pos;
        this.light = light;
        this.validItem = validItem;
        this.data = data;
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
                return validItem.test(stack);
            }
        });
        addDataSlots(data);
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
            if (!validItem.test(stack) || !moveItemStackTo(stack, LIGHT_SLOT, LIGHT_SLOT + 1, false)) {
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
