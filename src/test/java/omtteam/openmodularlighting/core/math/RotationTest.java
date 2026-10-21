package omtteam.openmodularlighting.core.math;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

class RotationTest {
    @Test
    void forwardPointsAlongTheFacing() {
        for (Facing facing : Facing.values()) {
            assertEquals(new Pos(facing.dx() * 3, facing.dy() * 3, facing.dz() * 3),
                    Rotation.rotate(new Pos(3, 0, 0), facing), facing.name());
            assertEquals(Pos.ORIGIN.offset(facing, 3), Rotation.rotate(new Pos(3, 0, 0), facing));
        }
    }

    @Test
    void portOfMathUtilRotate() {
        assertEquals(new Pos(2, -1, 3), Rotation.rotate(new Pos(1, 2, 3), Facing.DOWN));
        assertEquals(new Pos(-2, 1, 3), Rotation.rotate(new Pos(1, 2, 3), Facing.UP));
        assertEquals(new Pos(3, 2, -1), Rotation.rotate(new Pos(1, 2, 3), Facing.NORTH));
        assertEquals(new Pos(-3, 2, 1), Rotation.rotate(new Pos(1, 2, 3), Facing.SOUTH));
        assertEquals(new Pos(-1, 2, 3), Rotation.rotate(new Pos(1, 2, 3), Facing.WEST));
        assertEquals(new Pos(1, 2, 3), Rotation.rotate(new Pos(1, 2, 3), Facing.EAST));
    }

    @Test
    void rotationIsABijection() {
        for (Facing facing : Facing.values()) {
            Set<Pos> seen = new HashSet<>();
            for (int a = -3; a <= 3; a++) {
                for (int b = -3; b <= 3; b++) {
                    for (int c = -3; c <= 3; c++) {
                        seen.add(Rotation.rotate(new Pos(a, b, c), facing));
                    }
                }
            }
            assertEquals(7 * 7 * 7, seen.size(), facing.name());
        }
    }

    @Test
    void facingOppositeAndOrdinals() {
        assertEquals(Facing.UP, Facing.DOWN.opposite());
        assertEquals(Facing.SOUTH, Facing.NORTH.opposite());
        assertEquals(Facing.WEST, Facing.EAST.opposite());
        for (Facing facing : Facing.values()) {
            assertEquals(facing, facing.opposite().opposite());
            assertEquals(facing, Facing.byOrdinal(facing.ordinal()));
        }
        assertEquals(Facing.SOUTH, Facing.byOrdinal(42));
        assertEquals(Facing.SOUTH, Facing.byOrdinal(Facing.values().length));
        assertEquals(Facing.SOUTH, Facing.byOrdinal(-1));
    }
}
