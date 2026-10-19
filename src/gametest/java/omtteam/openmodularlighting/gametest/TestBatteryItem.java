package omtteam.openmodularlighting.gametest;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import omtteam.openmodularlighting.compat.VersionCompat;

/**
 * A battery for battery-slot tests: 100 000 E, any rate. The energy is kept in the item's custom data; each loader
 * exposes it through its energy API ({@code platform/<loader>}).
 */
public class TestBatteryItem extends Item {
    public static final long CAPACITY = 100_000;
    private static final String ENERGY = "energy";

    public TestBatteryItem(Properties properties) {
        super(properties);
    }

    public long getStoredEnergy(ItemStack stack) {
        return VersionCompat.getCustomData(stack).getLong(ENERGY);
    }

    public void setStoredEnergy(ItemStack stack, long energy) {
        VersionCompat.updateCustomData(stack, tag -> tag.putLong(ENERGY, Math.max(0, Math.min(CAPACITY, energy))));
    }
}
