package omtteam.openmodularlighting.core.light;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import omtteam.openmodularlighting.core.math.Pos;
import org.junit.jupiter.api.Test;

import java.util.List;

class ShapeDiffTest {
    private static Pos p(int x) {
        return new Pos(x, 0, 0);
    }

    @Test
    void keepsSharedPositionsAndKeepsOrder() {
        ShapeDiff diff = ShapeDiff.between(List.of(p(1), p(2), p(3), p(4)), List.of(p(6), p(2), p(5), p(4)));
        assertEquals(List.of(p(1), p(3)), diff.removed());
        assertEquals(List.of(p(6), p(5)), diff.added());
    }

    @Test
    void sameShapeIsEmpty() {
        assertTrue(ShapeDiff.between(List.of(p(1), p(2)), List.of(p(2), p(1))).isEmpty());
    }

    @Test
    void fromAndToNothing() {
        assertEquals(List.of(p(1)), ShapeDiff.between(List.of(), List.of(p(1))).added());
        assertEquals(List.of(p(1)), ShapeDiff.between(List.of(p(1)), List.of()).removed());
    }
}
