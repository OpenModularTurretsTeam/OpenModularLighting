package omtteam.openmodularlighting.core.light;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import omtteam.openmodularlighting.core.beam.BeamMode;
import omtteam.openmodularlighting.core.beam.BeamShape;
import omtteam.openmodularlighting.core.beam.Ray;
import omtteam.openmodularlighting.core.math.Facing;
import omtteam.openmodularlighting.core.math.Pos;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

class BeamCasterTest {
    private static final int[] RANGES = {1, 2, 3, 4, 5, 7, 8, 9, 12, 15, 16, 17, 31, 32, 33, 64};

    private static List<Pos> cast(BeamShape shape, Facing facing, LightWorld world) {
        return BeamCaster.cast(shape, Pos.ORIGIN, facing, LightType.PHANTOM, world);
    }

    private static Set<Pos> castSet(BeamShape shape, Pos origin, Facing facing, LightWorld world) {
        List<Pos> lit = BeamCaster.cast(shape, origin, facing, LightType.PHANTOM, world);
        Set<Pos> set = new HashSet<>(lit);
        assertEquals(lit.size(), set.size(), "cast result has duplicates");
        return set;
    }

    // ---- Same positions as the 1.7.10 loops ----

    @Test
    void matchesLegacyLoopsInRandomWorlds() {
        for (int seed = 0; seed < 60; seed++) {
            TestWorld world = TestWorld.random(seed, (seed % 6) * 0.06);
            Pos origin = new Pos(seed * 7, seed * 3 - 50, -seed * 5);
            for (Facing facing : Facing.values()) {
                String where = "seed " + seed + ", " + facing;
                for (int range : RANGES) {
                    assertEquals(Legacy1710Beams.straightSource(origin, facing, range, world),
                            castSet(BeamMode.STRAIGHT.shape(range, 1), origin, facing, world),
                            "straight " + range + ", " + where);
                    assertEquals(Legacy1710Beams.narrowConeSource(origin, facing, range, world),
                            castSet(BeamMode.NARROW_CONE.shape(1, range), origin, facing, world),
                            "narrow cone " + range + ", " + where);
                    assertEquals(Legacy1710Beams.wideConeSource(origin, facing, range, world),
                            castSet(BeamMode.WIDE_CONE.shape(1, range), origin, facing, world),
                            "wide cone " + range + ", " + where);
                }
                assertEquals(Legacy1710Beams.smallSource(origin, facing, world),
                        castSet(BeamShape.fluorescent(), origin, facing, world), "fluorescent, " + where);
                assertEquals(Legacy1710Beams.growSource(origin, facing, world),
                        castSet(BeamShape.grow(), origin, facing, world), "grow, " + where);
            }
        }
    }

    @Test
    void matchesLegacyLoopsInOpenWorld() {
        TestWorld world = TestWorld.open();
        for (Facing facing : Facing.values()) {
            for (int range : RANGES) {
                assertEquals(Legacy1710Beams.narrowConeSource(Pos.ORIGIN, facing, range, world),
                        castSet(BeamShape.narrowCone(range), Pos.ORIGIN, facing, world));
                assertEquals(Legacy1710Beams.wideConeSource(Pos.ORIGIN, facing, range, world),
                        castSet(BeamShape.wideCone(range / 4), Pos.ORIGIN, facing, world));
            }
        }
    }

    // ---- Shapes ----

    @Test
    void straightBeamInOrder() {
        List<Pos> lit = cast(BeamShape.straight(64), Facing.EAST, TestWorld.open());
        assertEquals(64, lit.size());
        for (int i = 0; i < 64; i++) {
            assertEquals(new Pos(i + 1, 0, 0), lit.get(i));
        }
        assertEquals(List.of(new Pos(0, 0, -1), new Pos(0, 0, -2)),
                cast(BeamShape.straight(2), Facing.NORTH, TestWorld.open()));
    }

    @Test
    void conesSpreadAsIn1710() {
        Set<Pos> narrow = new HashSet<>(cast(BeamShape.narrowCone(32), Facing.EAST, TestWorld.open()));
        assertTrue(narrow.contains(new Pos(32, 16, 0)), "outer ray spreads i/2");
        assertTrue(narrow.contains(new Pos(32, -16, -16)), "diagonal outer ray");
        assertTrue(narrow.contains(new Pos(32, 8, 8)), "inner ray spreads i/4");
        assertFalse(narrow.contains(new Pos(33, 0, 0)), "range 32");

        Set<Pos> wide = new HashSet<>(cast(BeamMode.WIDE_CONE.shape(64, 32), Facing.EAST, TestWorld.open()));
        assertTrue(wide.contains(new Pos(8, 8, 8)), "wide cone reaches range / 4 and spreads i");
        assertTrue(wide.contains(new Pos(8, 4, 0)), "inner ray spreads i/2");
        assertFalse(wide.contains(new Pos(9, 0, 0)));
    }

    @Test
    void fluorescentLightsFrontAndSides() {
        assertEquals(Set.of(new Pos(0, 1, 0), new Pos(1, 0, 0), new Pos(-1, 0, 0), new Pos(0, 0, 1), new Pos(0, 0, -1)),
                new HashSet<>(cast(BeamShape.fluorescent(), Facing.UP, TestWorld.open())));
    }

    @Test
    void emptyShapeLightsNothing() {
        assertTrue(cast(BeamShape.NONE, Facing.EAST, TestWorld.open()).isEmpty());
    }

    // ---- Blocking rules ----

    @Test
    void raysStopAtOpaqueAndUnloadedAndPassTransparentAndUvWithoutLight() {
        TestWorld world = TestWorld.open()
                .set(new Pos(2, 0, 0), Cell.TRANSPARENT)
                .set(new Pos(3, 0, 0), Cell.UV_LIGHT)
                .set(new Pos(4, 0, 0), Cell.PHANTOM_LIGHT)
                .set(new Pos(6, 0, 0), Cell.OPAQUE);
        assertEquals(List.of(new Pos(1, 0, 0), new Pos(4, 0, 0), new Pos(5, 0, 0)),
                cast(BeamShape.straight(10), Facing.EAST, world));

        world.set(new Pos(6, 0, 0), Cell.UNLOADED);
        assertEquals(3, cast(BeamShape.straight(10), Facing.EAST, world).size());
    }

    @Test
    void innerConeRayNeedsItsOuterRayToReachDistance8() {
        // (8, 2, 0) is only on inner ray 9, whose parent is outer ray 0: (i, i/2, 0)
        Pos innerOnly = new Pos(8, 2, 0);
        TestWorld blockedEarly = TestWorld.open().set(new Pos(3, 1, 0), Cell.OPAQUE);
        assertFalse(cast(BeamShape.narrowCone(32), Facing.EAST, blockedEarly).contains(innerOnly));

        TestWorld blockedLate = TestWorld.open().set(new Pos(9, 4, 0), Cell.OPAQUE);
        assertTrue(cast(BeamShape.narrowCone(32), Facing.EAST, blockedLate).contains(innerOnly));
    }

    @Test
    void narrowConeAbortsIfTheFrontIsBlocked() {
        Pos front = new Pos(1, 0, 0);
        assertTrue(cast(BeamShape.narrowCone(32), Facing.EAST, TestWorld.open().set(front, Cell.OPAQUE)).isEmpty());
        assertTrue(cast(BeamShape.narrowCone(32), Facing.EAST, TestWorld.open().set(front, Cell.UNLOADED)).isEmpty());
        assertFalse(cast(BeamShape.narrowCone(32), Facing.EAST, TestWorld.open().set(front, Cell.TRANSPARENT)).isEmpty());
    }

    @Test
    void wideConeAbortsOnlyIfTheFrontIsOpaque() {
        Pos front = new Pos(1, 0, 0);
        assertTrue(cast(BeamShape.wideCone(8), Facing.EAST, TestWorld.open().set(front, Cell.OPAQUE)).isEmpty());
        // 1.7.10 read an unloaded front block as air: only the centre ray stops there
        List<Pos> lit = cast(BeamShape.wideCone(8), Facing.EAST, TestWorld.open().set(front, Cell.UNLOADED));
        assertFalse(lit.isEmpty());
        assertFalse(lit.contains(front));
    }

    @Test
    void uvBeamTakesOverPhantomAndUvLights() {
        TestWorld world = TestWorld.open()
                .set(new Pos(1, 0, 0), Cell.PHANTOM_LIGHT)
                .set(new Pos(2, 0, 0), Cell.UV_LIGHT)
                .set(new Pos(3, 0, 0), Cell.TRANSPARENT)
                .set(new Pos(5, 0, 0), Cell.OPAQUE);
        assertEquals(List.of(new Pos(1, 0, 0), new Pos(2, 0, 0), new Pos(4, 0, 0)),
                BeamCaster.cast(BeamShape.uv(8), Pos.ORIGIN, Facing.EAST, LightType.UV, world));
    }

    @Test
    void shapeRejectsRaysWithLaterParents() {
        Ray first = Ray.of(List.of(new Pos(1, 0, 0)));
        Ray child = new Ray(List.of(new Pos(2, 0, 0)), 1, 1);
        assertThrows(IllegalArgumentException.class, () -> new BeamShape(List.of(child, first), BeamShape.FrontRule.NONE));
    }
}
