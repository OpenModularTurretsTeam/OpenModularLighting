package omtteam.openmodularlighting.core.beam;

import omtteam.openmodularlighting.core.math.Pos;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * The rays of a light, in beam space (forward = +X). Ports of the 1.7.10 shapes in
 * {@code TileEntityMetaFloodlight}, {@code TileEntitySmallFloodlight}, {@code TileEntityGrowLight} and
 * {@code TileEntityUVLight}, including their integer division.
 */
public final class BeamShape {
    /** No light at all (e.g. the grow light in black-light mode). */
    public static final BeamShape NONE = new BeamShape(Collections.emptyList(), FrontRule.NONE);

    /**
     * Sideways directions of the cone rays in 1.7.10 order ({@code j} = 0..7: +y, -y, +z, -z and the four diagonals),
     * followed by the centre ray ({@code j} = 8, which has no case in the old switch).
     */
    private static final int[][] SIDEWAYS = {
            {1, 0}, {-1, 0}, {0, 1}, {0, -1}, {1, 1}, {1, -1}, {-1, 1}, {-1, -1}, {0, 0}
    };
    /** Number of outer cone rays that have an inner ray (all but the centre ray). */
    private static final int INNER_RAYS = 8;

    public enum FrontRule {
        NONE,
        /** Cast nothing if the block in front is opaque (1.7.10 wide cone). */
        ABORT_IF_OPAQUE,
        /** Cast nothing if the block in front blocks light: opaque or unloaded (1.7.10 narrow cone). */
        ABORT_IF_BLOCKED
    }

    public BeamShape(List<Ray> rays, FrontRule frontRule) {
        this(rays, frontRule, Spacing.ALL);
    }

    private final List<Ray> rays;
    private final FrontRule frontRule;
    private final Spacing spacing;

    /**
     * @param rays      the rays; a ray's parent always comes before it
     * @param frontRule whether a blocked block directly in front cancels the whole beam
     * @param spacing   which distances get a light block (the rays are followed over all of them)
     */
    public BeamShape(List<Ray> rays, FrontRule frontRule, Spacing spacing) {
        this.rays = Collections.unmodifiableList(new ArrayList<>(rays));
        this.frontRule = Objects.requireNonNull(frontRule);
        this.spacing = Objects.requireNonNull(spacing);
        for (int i = 0; i < this.rays.size(); i++) {
            Ray ray = this.rays.get(i);
            if (ray.hasParent() && (ray.parent() < 0 || ray.parent() >= i)) {
                throw new IllegalArgumentException("Ray " + i + " must have an earlier parent, got " + ray.parent());
            }
        }
    }

    public List<Ray> rays() {
        return rays;
    }

    public FrontRule frontRule() {
        return frontRule;
    }

    public Spacing spacing() {
        return spacing;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BeamShape)) return false;
        BeamShape other = (BeamShape) o;
        return rays.equals(other.rays) && frontRule == other.frontRule && spacing.equals(other.spacing);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rays, frontRule, spacing);
    }

    @Override
    public String toString() {
        return "BeamShape[rays=" + rays + ", frontRule=" + frontRule + ", spacing=" + spacing + "]";
    }

    /** The same rays with light blocks only at some distances. */
    public BeamShape withSpacing(Spacing spacing) {
        return new BeamShape(rays, frontRule, spacing);
    }

    /** A single ray straight forward. */
    public static BeamShape straight(int range) {
        List<Pos> steps = new ArrayList<>();
        for (int i = 1; i <= range; i++) {
            steps.add(new Pos(i, 0, 0));
        }
        return new BeamShape(Collections.singletonList(Ray.of(steps)), FrontRule.NONE);
    }

    /**
     * Narrow cone ({@code narrowConeSource}): 9 outer rays (8 spreading {@code i/2} sideways at distance {@code i},
     * plus the centre ray) and 8 inner rays from distance 8 spreading {@code i/4}. An inner ray is skipped if its
     * outer ray was blocked before distance 8.
     */
    public static BeamShape narrowCone(int range) {
        return cone(range, 2, 8, 4, FrontRule.ABORT_IF_BLOCKED);
    }

    /**
     * Wide cone ({@code wideConeSource}) with the given length (1.7.10: cone range / 4): 9 outer rays spreading
     * {@code i} sideways, and 8 inner rays from distance 4 spreading {@code i/2}. An inner ray is skipped if its outer
     * ray was blocked before distance 4.
     */
    public static BeamShape wideCone(int length) {
        return cone(length, 1, 4, 2, FrontRule.ABORT_IF_OPAQUE);
    }

    /**
     * @param outerDivisor sideways spread of the outer rays is {@code i / outerDivisor}
     * @param innerStart   distance where the inner rays start, and the parent threshold
     * @param innerDivisor sideways spread of the inner rays is {@code i / innerDivisor}
     */
    private static BeamShape cone(int range, int outerDivisor, int innerStart, int innerDivisor, FrontRule frontRule) {
        List<Ray> rays = new ArrayList<>();
        for (int[] side : SIDEWAYS) {
            List<Pos> steps = new ArrayList<>();
            for (int i = 1; i <= range; i++) {
                int spread = i / outerDivisor;
                steps.add(new Pos(i, side[0] * spread, side[1] * spread));
            }
            rays.add(Ray.of(steps));
        }
        for (int j = 0; j < INNER_RAYS; j++) {
            List<Pos> steps = new ArrayList<>();
            for (int i = innerStart; i <= range; i++) {
                int spread = i / innerDivisor;
                steps.add(new Pos(i, SIDEWAYS[j][0] * spread, SIDEWAYS[j][1] * spread));
            }
            rays.add(new Ray(steps, j, innerStart));
        }
        return new BeamShape(rays, frontRule);
    }

    /** Fluorescent lights ({@code smallSource}): the block in front and the 4 blocks beside the light, each on its own. */
    public static BeamShape fluorescent() {
        return new BeamShape(Arrays.asList(
                single(new Pos(1, 0, 0)),
                single(new Pos(0, 1, 0)),
                single(new Pos(0, -1, 0)),
                single(new Pos(0, 0, 1)),
                single(new Pos(0, 0, -1))), FrontRule.NONE);
    }

    /** Cube light: the 6 blocks touching it, each on its own (the same set for every facing). */
    public static BeamShape cube() {
        return new BeamShape(Arrays.asList(
                single(new Pos(1, 0, 0)),
                single(new Pos(-1, 0, 0)),
                single(new Pos(0, 1, 0)),
                single(new Pos(0, -1, 0)),
                single(new Pos(0, 0, 1)),
                single(new Pos(0, 0, -1))), FrontRule.NONE);
    }

    private static Ray single(Pos step) {
        return Ray.of(Collections.singletonList(step));
    }

    /** Grow light in lighting mode ({@code growSource}): the block in front. */
    public static BeamShape grow() {
        return straight(1);
    }

    /** UV floodlight ({@code UVSource}): a single ray straight forward. */
    public static BeamShape uv(int range) {
        return straight(range);
    }
}
