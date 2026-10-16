package omtteam.openmodularlighting.core.state;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;
import java.util.random.RandomGenerator;
import org.junit.jupiter.api.Test;

class RecheckTimerTest {
    @Test
    void delayAveragesTwentySeconds() {
        RandomGenerator random = new Random(1);
        long sum = 0;
        int samples = 20_000;
        for (int i = 0; i < samples; i++) {
            long delay = RecheckTimer.nextDelay(random);
            assertTrue(delay >= 200 && delay < 600, "delay " + delay);
            sum += delay;
        }
        double mean = (double) sum / samples;
        assertTrue(Math.abs(mean - 400) < 5, "mean " + mean);
    }

    @Test
    void firesAboutOncePerMeanInterval() {
        RandomGenerator random = new Random(2);
        RecheckTimer timer = new RecheckTimer();
        int ticks = 400 * 2_000;
        int fired = 0;
        int sinceLast = 0;
        for (int i = 0; i < ticks; i++) {
            sinceLast++;
            if (timer.tick(random)) {
                assertTrue(sinceLast >= 200 && sinceLast <= 601, "interval " + sinceLast);
                fired++;
                sinceLast = 0;
            }
        }
        assertTrue(Math.abs(fired - 2_000) < 60, "fired " + fired);
    }
}
