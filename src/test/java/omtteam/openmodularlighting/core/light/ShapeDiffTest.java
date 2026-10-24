package omtteam.openmodularlighting.core.light;

import static omtteam.openmodularlighting.TestCollections.listOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import omtteam.openmodularlighting.core.math.Pos;
import org.junit.jupiter.api.Test;


class ShapeDiffTest {
    private static Pos p(int x) {
        return new Pos(x, 0, 0);
    }

    @Test
    void keepsSharedPositionsAndKeepsOrder() {
        ShapeDiff diff = ShapeDiff.between(listOf(p(1), p(2), p(3), p(4)), listOf(p(6), p(2), p(5), p(4)));
        assertEquals(listOf(p(1), p(3)), diff.removed());
        assertEquals(listOf(p(6), p(5)), diff.added());
    }

    @Test
    void sameShapeIsEmpty() {
        assertTrue(ShapeDiff.between(listOf(p(1), p(2)), listOf(p(2), p(1))).isEmpty());
    }

    @Test
    void fromAndToNothing() {
        assertEquals(listOf(p(1)), ShapeDiff.between(listOf(), listOf(p(1))).added());
        assertEquals(listOf(p(1)), ShapeDiff.between(listOf(p(1)), listOf()).removed());
        assertFalse(ShapeDiff.between(listOf(), listOf(p(1))).isEmpty());
        assertFalse(ShapeDiff.between(listOf(p(1)), listOf()).isEmpty());
    }
}
