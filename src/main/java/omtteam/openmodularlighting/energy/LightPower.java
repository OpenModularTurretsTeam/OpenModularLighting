package omtteam.openmodularlighting.energy;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.items.ItemStackHandler;
import omtteam.openmodularlighting.core.state.PowerSource;

/**
 * What a light runs on, together with its one item slot: {@link ElectricPower} (battery slot) or {@link FuelPower}
 * (fuel slot).
 */
public interface LightPower extends PowerSource {
    /** Whether the slot accepts this item. */
    boolean isValidItem(ItemStack stack);

    /** Charges from the battery or burns the next fuel item; runs every tick before the light logic. */
    void prepare(ItemStackHandler inventory);

    /** Whether there is energy or burn time worth keeping on the dropped item. */
    boolean hasStored();

    void read(NBTTagCompound tag);

    void write(NBTTagCompound tag);
}
