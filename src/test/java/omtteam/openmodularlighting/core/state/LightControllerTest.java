package omtteam.openmodularlighting.core.state;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import omtteam.openmodularlighting.core.state.LightController.Switch;
import omtteam.openmodularlighting.core.state.LightController.TickResult;
import org.junit.jupiter.api.Test;

class LightControllerTest {
    private static final int TIMEOUT = 40;

    /** Energy buffer paying {@code cost} per tick. */
    private static final class Buffer implements PowerSource {
        int energy;
        final int cost;

        Buffer(int energy, int cost) {
            this.energy = energy;
            this.cost = cost;
        }

        @Override
        public boolean canRun() {
            return energy >= cost;
        }

        @Override
        public void run() {
            energy -= cost;
        }
    }

    @Test
    void turnsOnWithRedstoneAndPaysEachTick() {
        LightController light = new LightController();
        Buffer buffer = new Buffer(100, 20);
        assertEquals(new TickResult(false, Switch.NONE, false), light.tick(false, buffer, TIMEOUT));
        assertEquals(new TickResult(false, Switch.ON, true), light.tick(true, buffer, TIMEOUT));
        assertEquals(new TickResult(false, Switch.NONE, true), light.tick(true, buffer, TIMEOUT));
        assertTrue(light.isOn());
        assertEquals(60, buffer.energy);
    }

    @Test
    void turnsOffWhenUnpoweredAndWaitsForTheTimeout() {
        LightController light = new LightController();
        Buffer buffer = new Buffer(10_000, 1);
        light.tick(true, buffer, TIMEOUT);
        assertEquals(Switch.OFF, light.tick(false, buffer, TIMEOUT).change());
        assertFalse(light.isOn());
        for (int i = 0; i < TIMEOUT; i++) {
            TickResult result = light.tick(true, buffer, TIMEOUT);
            assertEquals(Switch.NONE, result.change(), "tick " + i + " of the timeout");
            assertFalse(result.running());
        }
        assertEquals(Switch.ON, light.tick(true, buffer, TIMEOUT).change());
    }

    @Test
    void turnsOffWhenItCannotPay() {
        LightController light = new LightController();
        Buffer buffer = new Buffer(30, 20);
        assertEquals(Switch.ON, light.tick(true, buffer, TIMEOUT).change());
        assertEquals(Switch.OFF, light.tick(true, buffer, TIMEOUT).change());
        assertEquals(10, buffer.energy);
    }

    @Test
    void invertedLightsRunWithoutRedstone() {
        LightController light = new LightController();
        light.toggleInverted();
        Buffer buffer = new Buffer(100, 1);
        assertEquals(Switch.ON, light.tick(false, buffer, TIMEOUT).change());
        assertEquals(Switch.OFF, light.tick(true, buffer, TIMEOUT).change());
    }

    @Test
    void uvFloodlightHasNoTimeout() {
        LightController light = new LightController();
        Buffer buffer = new Buffer(100, 1);
        light.tick(true, buffer, 0);
        assertEquals(Switch.OFF, light.tick(false, buffer, 0).change());
        assertEquals(Switch.ON, light.tick(true, buffer, 0).change());
    }

    @Test
    void recastIsReportedOnceAndNotDelayedByTheTimeout() {
        LightController light = new LightController();
        Buffer buffer = new Buffer(100, 1);
        light.tick(true, buffer, TIMEOUT);
        light.tick(false, buffer, TIMEOUT); // off, timeout starts
        light.requestRecast();
        assertTrue(light.isRecastPending());
        assertTrue(light.tick(false, buffer, TIMEOUT).recast());
        assertFalse(light.tick(false, buffer, TIMEOUT).recast());
        assertFalse(light.isRecastPending());
    }

    @Test
    void loadRestoresState() {
        LightController light = new LightController();
        light.load(true, true, 5);
        assertTrue(light.isInverted());
        assertTrue(light.isOn());
        assertEquals(5, light.timeout());
        // still on after the timeout, as long as it can run
        Buffer buffer = new Buffer(100, 1);
        for (int i = 0; i < 5; i++) light.tick(false, buffer, TIMEOUT);
        assertEquals(new TickResult(false, Switch.NONE, true), light.tick(false, buffer, TIMEOUT));
    }

    @Test
    void fuelTimerRunsDownAndRejectsUselessFuel() {
        FuelTimer timer = new FuelTimer();
        assertTrue(timer.needsFuel());
        assertFalse(timer.refuel(0));
        assertTrue(timer.refuel(2));
        LightController light = new LightController();
        assertEquals(Switch.ON, light.tick(true, timer, TIMEOUT).change());
        light.tick(true, timer, TIMEOUT);
        assertTrue(timer.needsFuel());
        assertEquals(Switch.OFF, light.tick(true, timer, TIMEOUT).change());
    }
}
