package omtteam.openmodularlighting.core.beam;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class SpacingTest {
    @Test
    void keepsItsValues() {
        Spacing spacing = new Spacing(3, 1, 2);
        assertEquals(3, spacing.step());
        assertEquals(1, spacing.phase());
        assertEquals(2, spacing.axisPhase());
    }

    @Test
    void stepMustBePositive() {
        assertThrows(IllegalArgumentException.class, () -> new Spacing(0, 0, 0));
    }
}
