package omtteam.openmodularlighting.menu;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import omtteam.openmodularlighting.blockentity.FloodlightBlockEntity;
import omtteam.openmodularlighting.camo.CamoBlock;
import omtteam.openmodularlighting.camo.CamoRules;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

/**
 * Floodlight GUI: the light's slot (battery or fuel) and the player inventory, laid out as in 1.7.10
 * ({@code ContainerElectricFloodlight}, 176 × 146). Values for the status line are synced as container data.
 * <p>
 * Lights that can be disguised ({@link CamoBlock}) also get a phantom slot for the disguise (as in OpenModularTurrets).
 * It never holds an item: clicking it with a block in the cursor sets that block as the disguise (the block stays in
 * the cursor), clicking it with an empty cursor removes the disguise, and shift-clicking a block that does not fit the
 * light's slot sets it as well. Only the server changes the disguise; the slot shows it through the normal slot sync.
 */
public abstract class FloodlightMenu extends AbstractContainerMenu {
    private static final double MAX_DISTANCE_SQR = 64.0;
    /** Index of the light's slot, after the 36 player slots. */
    private static final int LIGHT_SLOT = 36;
    /** Index of the disguise slot, after the light's slot; only on lights with {@link #hasCamoSlot()}. */
    public static final int CAMO_SLOT = 37;
    public static final int CAMO_X = 152;
    public static final int CAMO_Y = 36;

    private final BlockPos pos;
    @Nullable
    private final FloodlightBlockEntity light;
    private final Predicate<ItemStack> validItem;
    private final boolean camoSlot;
    /** What the disguise slot shows (filled by the server, synced to the client like any slot). */
    private final SimpleContainer camoDisplay = new SimpleContainer(1);
    protected final ContainerData data;

    protected FloodlightMenu(MenuType<?> type, int containerId, Inventory playerInventory, BlockPos pos,
                             @Nullable FloodlightBlockEntity light, Container slotContainer, ContainerData data,
                             Predicate<ItemStack> validItem) {
        super(type, containerId);
        this.pos = pos;
        this.light = light;
        this.validItem = validItem;
        this.data = data;
        // the block at pos: the same on both sides, so both create the same slots
        this.camoSlot = playerInventory.player.level().getBlockState(pos).getBlock() instanceof CamoBlock;
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
        if (camoSlot) {
            if (light != null) {
                camoDisplay.setItem(0, CamoRules.itemFor(light.getCamo()));
            }
            addSlot(new Slot(camoDisplay, 0, CAMO_X, CAMO_Y) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return false;
                }

                @Override
                public boolean mayPickup(Player player) {
                    return false;
                }
            });
        }
        addDataSlots(data);
    }

    /** Whether this light can be disguised: the GUI shows the disguise slot. */
    public boolean hasCamoSlot() {
        return camoSlot;
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (camoSlot && slotId == CAMO_SLOT) {
            if (clickType == ClickType.PICKUP && !player.level().isClientSide()) {
                setDisguiseFrom(player, getCarried());
            }
            return;
        }
        super.clicked(slotId, button, clickType, player);
    }

    /** An empty stack removes the disguise, a valid block sets it; anything else is refused with a message. */
    private void setDisguiseFrom(Player player, ItemStack stack) {
        if (light == null || light.isRemoved()) return;
        BlockState disguise = stack.isEmpty() ? null : CamoRules.disguiseFor(stack);
        if (!stack.isEmpty() && disguise == null) {
            player.displayClientMessage(Component.translatable("gui.openmodularlighting.camo_invalid"), false);
            return;
        }
        light.setCamo(disguise);
        camoDisplay.setItem(0, CamoRules.itemFor(light.getCamo()));
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
            if (!validItem.test(stack)) {
                // not for the light's slot: a block becomes the disguise (the stack stays where it is)
                if (camoSlot && !player.level().isClientSide() && CamoRules.disguiseFor(stack) != null) {
                    setDisguiseFrom(player, stack);
                }
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
