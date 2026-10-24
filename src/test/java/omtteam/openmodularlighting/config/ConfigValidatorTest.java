package omtteam.openmodularlighting.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfigValidatorTest {
    @Test
    void valuesInsideTheRangeStay() {
        ConfigValidator validator = new ConfigValidator();
        assertEquals(1, validator.range("a", 1, 1, 64), "min is allowed");
        assertEquals(30, validator.range("a", 30, 1, 64));
        assertEquals(64, validator.range("a", 64, 1, 64), "max is allowed");
        assertEquals(0D, validator.range("b", 0D, 0D, 1D));
        assertEquals(0.25D, validator.range("b", 0.25D, 0D, 1D));
        assertEquals(1D, validator.range("b", 1D, 0D, 1D));
        assertFalse(validator.changed());
    }

    @Test
    void intsOutsideAreClamped() {
        ConfigValidator low = new ConfigValidator();
        assertEquals(1, low.range("a", 0, 1, 64));
        assertTrue(low.changed());
        ConfigValidator high = new ConfigValidator();
        assertEquals(64, high.range("a", 65, 1, 64));
        assertTrue(high.changed());
    }

    @Test
    void doublesOutsideAreClamped() {
        ConfigValidator low = new ConfigValidator();
        assertEquals(0D, low.range("b", -0.5D, 0D, 1D));
        assertTrue(low.changed());
        ConfigValidator high = new ConfigValidator();
        assertEquals(1D, high.range("b", 1.5D, 0D, 1D));
        assertTrue(high.changed());
    }

    @Test
    void notANumberBecomesTheMinimum() {
        ConfigValidator validator = new ConfigValidator();
        assertEquals(0.5D, validator.range("b", Double.NaN, 0.5D, 1D));
        assertTrue(validator.changed());
    }
}
