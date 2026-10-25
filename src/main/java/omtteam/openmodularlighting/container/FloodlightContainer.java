package omtteam.openmodularlighting.container;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.items.SlotItemHandler;
import omtteam.openmodularlighting.block.CarbonFloodlightBlock;
import omtteam.openmodularlighting.block.ElectricFloodlightBlock;
import omtteam.openmodularlighting.camo.CamoBlock;
import omtteam.openmodularlighting.camo.CamoRules;
import omtteam.openmodularlighting.tileentity.FloodlightTileEntity;

import java.util.function.LongSupplier;

/**
 * Floodlight GUI: the light's slot (battery or fuel) and the player inventory, laid out as in 1.7.10
 * ({@code ContainerElectricFloodlight}, 176 × 146). Values for the status line are synced as window properties.
 * <p>
 * Lights that can be disguised ({@link CamoBlock}) also get a phantom slot for the disguise (as in OpenModularTurrets).
 * It never holds an item: clicking it with a block in the cursor sets that block as the disguise (the block stays in
 * the cursor), clicking it with an empty cursor removes the disguise, and shift-clicking a block that does not fit the
 * light's slot sets it as well. Only the server changes the disguise; the slot shows it through the normal slot sync.
 */
public abstract class FloodlightContainer extends Container {
    private static final double MAX_DISTANCE_SQR = 64.0;
    /** Index of the light's slot, after the 36 player slots. */
    private static final int LIGHT_SLOT = 36;
    /** Index of the disguise slot, after the light's slot; only on lights with {@link #hasCamoSlot()}. */
    public static final int CAMO_SLOT = 37;
    public static final int CAMO_X = 152;
    public static final int CAMO_Y = 36;
    /** Window properties are sent as shorts: each long value goes as two 16-bit halves. */
    private static final int HALF_BITS = 16;
    private static final int HALF_MASK = 0xFFFF;

    private final BlockPos pos;
    private final FloodlightTileEntity light;
    private final boolean camoSlot;
    private final boolean timeoutHint;
    /** What the disguise slot shows (filled by the server, synced to the client like any slot). */
    private final InventoryBasic camoDisplay = new InventoryBasic("camo", false, 1);
    /** The synced values (read on the server only, clamped to {@code 0..Integer.MAX_VALUE}). */
    private final LongSupplier[] values;
    /** Last sent (server) or received (client) halves of {@link #values}. */
    private final int[] halves;

    /**
     * @param light  the light's block entity (on the client the client's copy, kept in sync by the slots)
     * @param values the values shown in the status line, see {@link #value}
     */
    protected FloodlightContainer(InventoryPlayer playerInventory, FloodlightTileEntity light, LongSupplier... values) {
        this.pos = light.getPos();
        this.light = light;
        this.values = values;
        this.halves = new int[values.length * 2];
        // the block at pos: the same on both sides, so both create the same slots
        Block block = light.getWorld().getBlockState(pos).getBlock();
        this.camoSlot = block instanceof CamoBlock;
        this.timeoutHint = block instanceof ElectricFloodlightBlock || block instanceof CarbonFloodlightBlock;
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 9; x++) {
                addSlotToContainer(new Slot(playerInventory, 9 + x + y * 9, 8 + x * 18, 58 + y * 18));
            }
        }
        for (int x = 0; x < 9; x++) {
            addSlotToContainer(new Slot(playerInventory, x, 8 + x * 18, 116));
        }
        addSlotToContainer(new SlotItemHandler(light.getInventory(), 0, 26, 22));
        if (camoSlot) {
            camoDisplay.setInventorySlotContents(0, CamoRules.itemFor(light.getCamo()));
            addSlotToContainer(new Slot(camoDisplay, 0, CAMO_X, CAMO_Y) {
                @Override
                public boolean isItemValid(ItemStack stack) {
                    return false;
                }

                @Override
                public boolean canTakeStack(EntityPlayer player) {
                    return false;
                }
            });
        }
    }

    /** What the light's slot accepts (battery or fuel). */
    protected boolean isValidItem(ItemStack stack) {
        return light.isValidItem(stack);
    }

    /** Whether this light can be disguised: the GUI shows the disguise slot. */
    public boolean hasCamoSlot() {
        return camoSlot;
    }

    /** Whether the GUI explains the timeout after turning off (the electric and carbon floodlights). */
    public boolean hasTimeoutHint() {
        return timeoutHint;
    }

    public BlockPos getPos() {
        return pos;
    }

    /** Synced value number {@code index} (client side: as last received). */
    protected long value(int index) {
        return (halves[index * 2] & HALF_MASK) | ((long) (halves[index * 2 + 1] & HALF_MASK) << HALF_BITS);
    }

    private int currentHalf(int half) {
        long value = Math.max(0, Math.min(Integer.MAX_VALUE, values[half / 2].getAsLong()));
        return (int) ((half % 2 == 0 ? value : value >> HALF_BITS) & HALF_MASK);
    }

    @Override
    public void addListener(IContainerListener listener) {
        super.addListener(listener);
        for (int half = 0; half < halves.length; half++) {
            halves[half] = currentHalf(half);
            listener.sendWindowProperty(this, half, halves[half]);
        }
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        for (int half = 0; half < halves.length; half++) {
            int current = currentHalf(half);
            if (current != halves[half]) {
                halves[half] = current;
                for (IContainerListener listener : listeners) {
                    listener.sendWindowProperty(this, half, current);
                }
            }
        }
    }

    @Override
    public void updateProgressBar(int id, int data) {
        if (id >= 0 && id < halves.length) {
            halves[id] = data & HALF_MASK;
        }
    }

    @Override
    public ItemStack slotClick(int slotId, int dragType, ClickType clickType, EntityPlayer player) {
        if (camoSlot && slotId == CAMO_SLOT) {
            if (clickType == ClickType.PICKUP && !player.world.isRemote) {
                setDisguiseFrom(player, player.inventory.getItemStack());
            }
            return ItemStack.EMPTY;
        }
        return super.slotClick(slotId, dragType, clickType, player);
    }

    /** An empty stack removes the disguise, a valid block sets it; anything else is refused with a message. */
    private void setDisguiseFrom(EntityPlayer player, ItemStack stack) {
        if (light.isInvalid()) return;
        IBlockState disguise = stack.isEmpty() ? null : CamoRules.disguiseFor(stack);
        if (!stack.isEmpty() && disguise == null) {
            player.sendStatusMessage(new TextComponentTranslation("gui.openmodularlighting.camo_invalid"), false);
            return;
        }
        light.setCamo(disguise);
        camoDisplay.setInventorySlotContents(0, CamoRules.itemFor(light.getCamo()));
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        Slot slot = inventorySlots.get(index);
        if (slot == null || !slot.getHasStack()) return ItemStack.EMPTY;
        ItemStack stack = slot.getStack();
        ItemStack original = stack.copy();
        if (index < LIGHT_SLOT) {
            if (!isValidItem(stack)) {
                quickSetDisguise(player, stack); // the stack stays where it is
                return ItemStack.EMPTY;
            }
            if (!mergeItemStack(stack, LIGHT_SLOT, LIGHT_SLOT + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (!mergeItemStack(stack, 0, LIGHT_SLOT, false)) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) {
            slot.putStack(ItemStack.EMPTY);
        } else {
            slot.onSlotChanged();
        }
        return stack.getCount() == original.getCount() ? ItemStack.EMPTY : original;
    }

    /** Shift-click of a block that does not fit the light's slot: it becomes the disguise. */
    private void quickSetDisguise(EntityPlayer player, ItemStack stack) {
        if (camoSlot && !player.world.isRemote && CamoRules.disguiseFor(stack) != null) {
            setDisguiseFrom(player, stack);
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return !light.isInvalid()
                && player.getDistanceSq(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) < MAX_DISTANCE_SQR;
    }
}
