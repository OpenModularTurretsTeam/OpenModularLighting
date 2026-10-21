package omtteam.openmodularlighting.core.beam;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class BeamModeTest {
    @Test
    void modesCycleInTheOldOrder() {
        assertEquals(BeamMode.NARROW_CONE, BeamMode.STRAIGHT.next());
        assertEquals(BeamMode.WIDE_CONE, BeamMode.NARROW_CONE.next());
        assertEquals(BeamMode.STRAIGHT, BeamMode.WIDE_CONE.next());
        assertEquals(GrowMode.BLACK_LIGHT, GrowMode.LIGHTING.next());
        assertEquals(GrowMode.LIGHTING, GrowMode.BLACK_LIGHT.next());
    }

    @Test
    void ordinalsMatchTheOldModeValues() {
        assertEquals(BeamMode.STRAIGHT, BeamMode.byOrdinal(0));
        assertEquals(BeamMode.NARROW_CONE, BeamMode.byOrdinal(1));
        assertEquals(BeamMode.WIDE_CONE, BeamMode.byOrdinal(2));
        assertEquals(BeamMode.STRAIGHT, BeamMode.byOrdinal(-1));
        assertEquals(BeamMode.STRAIGHT, BeamMode.byOrdinal(3));
        assertEquals(GrowMode.LIGHTING, GrowMode.byOrdinal(0));
        assertEquals(GrowMode.BLACK_LIGHT, GrowMode.byOrdinal(1));
        assertEquals(GrowMode.LIGHTING, GrowMode.byOrdinal(2));
        assertEquals(GrowMode.LIGHTING, GrowMode.byOrdinal(-1));
    }

    @Test
    void blackLightHasNoShape() {
        assertTrue(GrowMode.BLACK_LIGHT.shape().rays().isEmpty());
        assertEquals(1, GrowMode.LIGHTING.shape().rays().size());
    }

    @Test
    void rangesPerMode() {
        assertEquals(64, BeamMode.STRAIGHT.shape(64, 32).rays().get(0).steps().size());
        assertEquals(32, BeamMode.NARROW_CONE.shape(64, 32).rays().get(0).steps().size());
        assertEquals(8, BeamMode.WIDE_CONE.shape(64, 32).rays().get(0).steps().size());
        // 9 outer + 8 inner rays
        assertEquals(17, BeamMode.NARROW_CONE.shape(64, 32).rays().size());
    }
}
