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
import omtteam.omlib.container.WindowValues;
import omtteam.openmodularlighting.block.CarbonFloodlightBlock;
import omtteam.openmodularlighting.block.ElectricFloodlightBlock;
import omtteam.openmodularlighting.block.FloodlightBlock;
import omtteam.openmodularlighting.tileentity.FloodlightTileEntity;

import java.util.function.IntSupplier;
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

    private final BlockPos pos;
    private final FloodlightTileEntity light;
    private final boolean camoButton;
    private final boolean timeoutHint;
    /** The values of the status line as window properties (OMLib), clamped to {@code 0..Integer.MAX_VALUE}. */
    private final WindowValues values;

    /**
     * @param light  the light's block entity (on the client the client's copy, kept in sync by the slots)
     * @param values the values shown in the status line, see {@link #value}
     */
    protected FloodlightContainer(InventoryPlayer playerInventory, FloodlightTileEntity light, LongSupplier... values) {
        this.pos = light.getPos();
        this.light = light;
        IntSupplier[] clamped = new IntSupplier[values.length];
        for (int i = 0; i < values.length; i++) {
            LongSupplier value = values[i];
            clamped[i] = () -> (int) Math.max(0, Math.min(Integer.MAX_VALUE, value.getAsLong()));
        }
        this.values = new WindowValues(clamped);
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
        return values.get(index);
    }

    @Override
    public void addListener(IContainerListener listener) {
        super.addListener(listener);
        values.addListener(this, listener);
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        values.detectAndSendChanges(this, listeners);
    }

    @Override
    public void updateProgressBar(int id, int data) {
        values.updateProgressBar(id, data);
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
