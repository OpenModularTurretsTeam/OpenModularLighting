package omtteam.openmodularlighting.energy;

import net.minecraft.nbt.NBTTagCompound;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ElectricPowerTest {
    private final AtomicInteger cost = new AtomicInteger(10);
    private final AtomicInteger changes = new AtomicInteger();
    private final ElectricPower power = new ElectricPower(cost::get, changes::incrementAndGet);

    @Test
    void runsWhileItCanPayTheCost() {
        power.store().generate(15);
        assertTrue(power.canRun());
        power.run();
        assertEquals(5, power.store().getAmount());
        assertFalse(power.canRun());
        assertTrue(power.hasStored());
    }

    @Test
    void theCostIsReadEveryTick() {
        power.store().generate(15);
        cost.set(20);
        assertFalse(power.canRun());
        assertEquals(20, power.cost());
    }

    @Test
    void onlyEnergyComingInMarksTheLightChanged() {
        assertEquals(50_000, power.store().insert(100_000, false), "the whole buffer can be filled in one tick");
        assertEquals(1, changes.get());
        power.run();
        assertEquals(1, changes.get(), "paying its own cost every tick does not");
        assertEquals(0, power.store().extract(100, false), "nothing can be extracted");
    }

    @Test
    void savesTheStoredEnergy() {
        power.store().generate(1234);
        NBTTagCompound tag = new NBTTagCompound();
        power.write(tag);

        ElectricPower loaded = new ElectricPower(cost::get, () -> {
        });
        loaded.read(tag);
        assertEquals(1234, loaded.store().getAmount());
    }

    @Test
    void loadingClampsToTheBuffer() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setLong("energy", 80_000);
        power.read(tag);
        assertEquals(ElectricPower.CAPACITY, power.store().getAmount());

        tag.setLong("energy", -5);
        power.read(tag);
        assertEquals(0, power.store().getAmount());
        assertEquals(0, changes.get(), "loading is no change");
    }
}
