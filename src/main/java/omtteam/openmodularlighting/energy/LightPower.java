package omtteam.openmodularlighting.energy;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import omtteam.openmodularlighting.core.state.PowerSource;

/**
 * What a light runs on, together with its one item slot: {@link ElectricPower} (battery slot) or {@link FuelPower}
 * (fuel slot).
 */
public interface LightPower extends PowerSource {
    /** Whether the slot accepts this item. */
    boolean isValidItem(ItemStack stack);

    /** Charges from the battery or burns the next fuel item; runs every tick before the light logic. */
    void prepare(SimpleContainer inventory);

    /** Whether there is energy or burn time worth keeping on the dropped item. */
    boolean hasStored();

    void read(CompoundTag tag);

    void write(CompoundTag tag);
}
