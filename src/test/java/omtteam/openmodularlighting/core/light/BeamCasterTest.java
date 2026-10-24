package omtteam.openmodularlighting.core.light;

import static omtteam.openmodularlighting.TestCollections.listOf;
import static omtteam.openmodularlighting.TestCollections.setOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import omtteam.openmodularlighting.core.beam.BeamMode;
import omtteam.openmodularlighting.core.beam.BeamShape;
import omtteam.openmodularlighting.core.beam.Ray;
import omtteam.openmodularlighting.core.beam.Spacing;
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

    /**
     * The 1.7.10 positions, thinned like the mode shapes: a light only at the distances (in the facing direction) that
     * {@link Spacing} allows. The rays are the same, so the walls they stop at are the same.
     */
    private static Set<Pos> thinned(Set<Pos> legacy, Pos origin, Facing facing, Spacing spacing) {
        Set<Pos> result = new HashSet<>();
        for (Pos pos : legacy) {
            int distance = (pos.x() - origin.x()) * facing.dx() + (pos.y() - origin.y()) * facing.dy()
                    + (pos.z() - origin.z()) * facing.dz();
            boolean onAxis = pos.x() - origin.x() == distance * facing.dx()
                    && pos.y() - origin.y() == distance * facing.dy() && pos.z() - origin.z() == distance * facing.dz();
            if (spacing.emitsAt(distance, onAxis)) result.add(pos);
        }
        return result;
    }

    // ---- Same positions as the 1.7.10 loops (thinned for the floodlight modes) ----

    @Test
    void matchesLegacyLoopsInRandomWorlds() {
        for (int seed = 0; seed < 60; seed++) {
            TestWorld world = TestWorld.random(seed, (seed % 6) * 0.06);
            Pos origin = new Pos(seed * 7, seed * 3 - 50, -seed * 5);
            for (Facing facing : Facing.values()) {
                String where = "seed " + seed + ", " + facing;
                for (int range : RANGES) {
                    assertEquals(thinned(Legacy1710Beams.straightSource(origin, facing, range, world), origin, facing,
                                    Spacing.STRAIGHT),
                            castSet(BeamMode.STRAIGHT.shape(range, 1), origin, facing, world),
                            "straight " + range + ", " + where);
                    assertEquals(thinned(Legacy1710Beams.narrowConeSource(origin, facing, range, world), origin, facing,
                                    Spacing.CONE),
                            castSet(BeamMode.NARROW_CONE.shape(1, range), origin, facing, world),
                            "narrow cone " + range + ", " + where);
                    assertEquals(thinned(Legacy1710Beams.wideConeSource(origin, facing, range, world), origin, facing,
                                    Spacing.CONE),
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

    // ---- Spacing ----

    @Test
    void straightModeSkipsEverySecondBlockStartingWithTheSecond() {
        List<Pos> lit = cast(BeamMode.STRAIGHT.shape(8, 32), Facing.EAST, TestWorld.open());
        assertEquals(listOf(new Pos(1, 0, 0), new Pos(3, 0, 0), new Pos(5, 0, 0), new Pos(7, 0, 0)), lit);
    }

    @Test
    void conesKeepTheCentreLineOnOddDistancesAndTheRestOnEvenDistances() {
        for (BeamMode mode : new BeamMode[]{BeamMode.NARROW_CONE, BeamMode.WIDE_CONE}) {
            Set<Pos> lit = castSet(mode.shape(64, 64), Pos.ORIGIN, Facing.EAST, TestWorld.open());
            for (Pos pos : lit) {
                boolean onAxis = pos.y() == 0 && pos.z() == 0;
                assertEquals(onAxis ? 1 : 0, pos.x() % 2, mode + " lit at " + pos);
            }
            for (int x : new int[]{1, 3, 5, 7}) {
                assertTrue(lit.contains(new Pos(x, 0, 0)), mode + " centre line at " + x);
            }
            assertFalse(lit.contains(new Pos(2, 0, 0)) || lit.contains(new Pos(4, 0, 0)));
            // the cone is there from the start, not only from distance 5
            assertTrue(lit.contains(mode == BeamMode.NARROW_CONE ? new Pos(2, 1, 0) : new Pos(2, 2, 0)),
                    mode + " spreads in the first 5 blocks");
        }
    }

    @Test
    void skippedBlocksStillBlockTheRay() {
        TestWorld world = TestWorld.open().set(new Pos(4, 0, 0), Cell.OPAQUE);
        assertEquals(listOf(new Pos(1, 0, 0), new Pos(3, 0, 0)),
                cast(BeamMode.STRAIGHT.shape(10, 32), Facing.EAST, world));
        // a wall at a skipped distance still ends the cone ray
        TestWorld wall = TestWorld.open().set(new Pos(6, 0, 0), Cell.OPAQUE);
        Set<Pos> lit = castSet(BeamMode.NARROW_CONE.shape(64, 12), Pos.ORIGIN, Facing.EAST, wall);
        assertTrue(lit.contains(new Pos(5, 0, 0)));
        assertFalse(lit.contains(new Pos(7, 0, 0)));
    }

    @Test
    void spacingRules() {
        assertTrue(Spacing.ALL.emitsAt(1, false) && Spacing.ALL.emitsAt(2, true));
        assertTrue(Spacing.STRAIGHT.emitsAt(1, true) && !Spacing.STRAIGHT.emitsAt(2, true) && Spacing.STRAIGHT.emitsAt(3, true));
        assertTrue(Spacing.CONE.emitsAt(1, true) && !Spacing.CONE.emitsAt(2, true) && Spacing.CONE.emitsAt(3, true));
        assertTrue(!Spacing.CONE.emitsAt(1, false) && Spacing.CONE.emitsAt(2, false) && !Spacing.CONE.emitsAt(3, false));
        assertThrows(IllegalArgumentException.class, () -> new Spacing(0, 0, 0));
        // the smallest step is allowed; a step of 3 tells distance - phase apart from distance + phase
        assertTrue(new Spacing(1, 0, 0).emitsAt(5, false));
        Spacing third = new Spacing(3, 1, 2);
        assertTrue(third.emitsAt(4, false) && !third.emitsAt(2, false) && !third.emitsAt(3, false));
        assertTrue(third.emitsAt(5, true) && !third.emitsAt(1, true) && !third.emitsAt(4, true));
    }

    // ---- Shapes ----

    @Test
    void straightBeamInOrder() {
        List<Pos> lit = cast(BeamShape.straight(64), Facing.EAST, TestWorld.open());
        assertEquals(64, lit.size());
        for (int i = 0; i < 64; i++) {
            assertEquals(new Pos(i + 1, 0, 0), lit.get(i));
        }
        assertEquals(listOf(new Pos(0, 0, -1), new Pos(0, 0, -2)),
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
        assertEquals(setOf(new Pos(0, 1, 0), new Pos(1, 0, 0), new Pos(-1, 0, 0), new Pos(0, 0, 1), new Pos(0, 0, -1)),
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
        assertEquals(listOf(new Pos(1, 0, 0), new Pos(4, 0, 0), new Pos(5, 0, 0)),
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
        assertEquals(listOf(new Pos(1, 0, 0), new Pos(2, 0, 0), new Pos(4, 0, 0)),
                BeamCaster.cast(BeamShape.uv(8), Pos.ORIGIN, Facing.EAST, LightType.UV, world));
    }

    @Test
    void shapeRejectsRaysWithLaterParents() {
        Ray first = Ray.of(listOf(new Pos(1, 0, 0)));
        Ray child = new Ray(listOf(new Pos(2, 0, 0)), 1, 1);
        assertThrows(IllegalArgumentException.class, () -> new BeamShape(listOf(child, first), BeamShape.FrontRule.NONE));
        Ray ownParent = new Ray(listOf(new Pos(2, 0, 0)), 1, 1);
        assertThrows(IllegalArgumentException.class, () -> new BeamShape(listOf(first, ownParent), BeamShape.FrontRule.NONE));
        Ray firstAsParent = new Ray(listOf(new Pos(2, 0, 0)), 0, 1);
        assertEquals(2, new BeamShape(listOf(first, firstAsParent), BeamShape.FrontRule.NONE).rays().size());
    }

    @Test
    void cubeLightsAllSixNeighboursForEveryFacing() {
        Set<Pos> neighbours = new HashSet<>();
        for (Facing facing : Facing.values()) neighbours.add(Pos.ORIGIN.offset(facing, 1));
        for (Facing facing : Facing.values()) {
            assertEquals(neighbours, castSet(BeamShape.cube(), Pos.ORIGIN, facing, TestWorld.open()), facing.name());
        }
    }
}
