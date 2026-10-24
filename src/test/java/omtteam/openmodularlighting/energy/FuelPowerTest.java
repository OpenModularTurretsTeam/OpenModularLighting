package omtteam.openmodularlighting.energy;

import net.minecraft.nbt.NBTTagCompound;
import omtteam.openmodularlighting.core.beam.BeamMode;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FuelPowerTest {
    private final AtomicReference<BeamMode> mode = new AtomicReference<>(BeamMode.STRAIGHT);
    private final FuelPower power = new FuelPower(mode::get);

    private static NBTTagCompound saved(int ticks) {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger("time_remaining", ticks);
        return tag;
    }

    @Test
    void anEmptyLightCannotRun() {
        assertFalse(power.canRun());
        assertFalse(power.hasStored());
        assertEquals(0, power.runTicks());
    }

    @Test
    void conesUseTheBurnTimeTwiceAsFast() {
        power.read(saved(101));
        assertEquals(101, power.runTicks());
        mode.set(BeamMode.NARROW_CONE);
        assertEquals(51, power.runTicks(), "rounded up");

        power.run();

        NBTTagCompound tag = new NBTTagCompound();
        power.write(tag);
        assertEquals(99, tag.getInteger("time_remaining"));
    }

    @Test
    void savesTheBurnTime() {
        power.read(saved(400));
        NBTTagCompound tag = new NBTTagCompound();
        power.write(tag);

        FuelPower loaded = new FuelPower(mode::get);
        loaded.read(tag);
        assertTrue(loaded.canRun());
        assertEquals(400, loaded.runTicks());
    }

    @Test
    void aNegativeSavedTimeLoadsAsEmpty() {
        power.read(saved(-20));
        assertFalse(power.hasStored());
    }
}
