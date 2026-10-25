package omtteam.openmodularlighting.container;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.SlotItemHandler;
import omtteam.openmodularlighting.block.CarbonFloodlightBlock;
import omtteam.openmodularlighting.block.ElectricFloodlightBlock;
import omtteam.openmodularlighting.block.FloodlightBlock;
import omtteam.openmodularlighting.tileentity.FloodlightTileEntity;

import java.util.function.LongSupplier;

/**
 * Floodlight GUI: the light's slot (battery or fuel) and the player inventory, laid out as in 1.7.10
 * ({@code ContainerElectricFloodlight}, 176 × 146). Values for the status line are synced as window properties.
 * <p>
 * Lights that can be disguised ({@link FloodlightBlock#supportsCamo()}) get a button in the GUI that opens OMLib's
 * camouflage menu.
 */
public abstract class FloodlightContainer extends Container {
    private static final double MAX_DISTANCE_SQR = 64.0;
    /** Index of the light's slot, after the 36 player slots. */
    private static final int LIGHT_SLOT = 36;
    /** Window properties are sent as shorts: each long value goes as two 16-bit halves. */
    private static final int HALF_BITS = 16;
    private static final int HALF_MASK = 0xFFFF;

    private final BlockPos pos;
    private final FloodlightTileEntity light;
    private final boolean camoButton;
    private final boolean timeoutHint;
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
        this.camoButton = block instanceof FloodlightBlock && ((FloodlightBlock) block).supportsCamo();
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
    }

    /** What the light's slot accepts (battery or fuel). */
    protected boolean isValidItem(ItemStack stack) {
        return light.isValidItem(stack);
    }

    /** Whether this light can be disguised: the GUI shows the camouflage button. */
    public boolean hasCamoButton() {
        return camoButton;
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
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        Slot slot = inventorySlots.get(index);
        if (slot == null || !slot.getHasStack()) return ItemStack.EMPTY;
        ItemStack stack = slot.getStack();
        ItemStack original = stack.copy();
        if (index < LIGHT_SLOT) {
            if (!isValidItem(stack) || !mergeItemStack(stack, LIGHT_SLOT, LIGHT_SLOT + 1, false)) {
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

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return !light.isInvalid()
                && player.getDistanceSq(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) < MAX_DISTANCE_SQR;
    }
}
