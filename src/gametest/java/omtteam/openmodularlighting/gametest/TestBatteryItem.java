package omtteam.openmodularlighting.gametest;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import team.reborn.energy.api.base.SimpleEnergyItem;

/**
 * A battery for battery-slot tests: 100 000 E, any rate.
 */
public class TestBatteryItem extends Item implements SimpleEnergyItem {
    public static final long CAPACITY = 100_000;

    public TestBatteryItem(Properties properties) {
        super(properties);
    }

    @Override
    public long getEnergyCapacity(ItemStack stack) {
        return CAPACITY;
    }

    @Override
    public long getEnergyMaxInput(ItemStack stack) {
        return CAPACITY;
    }

    @Override
    public long getEnergyMaxOutput(ItemStack stack) {
        return CAPACITY;
    }
}
