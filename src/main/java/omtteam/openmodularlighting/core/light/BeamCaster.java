package omtteam.openmodularlighting.core.light;

import omtteam.openmodularlighting.core.beam.BeamShape;
import omtteam.openmodularlighting.core.beam.Ray;
import omtteam.openmodularlighting.core.beam.Spacing;
import omtteam.openmodularlighting.core.math.Facing;
import omtteam.openmodularlighting.core.math.Pos;
import omtteam.openmodularlighting.core.math.Rotation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Computes which blocks a light reaches. Casting only reads the world; {@code main} then places or joins light
 * blocks at the result (usually through a {@link ShapeDiff} against the cached shape).
 */
public final class BeamCaster {
    private BeamCaster() {
    }

    /**
     * Casts every ray of {@code shape} from {@code origin} towards {@code facing}. Rays are followed over every
     * block; only the distances allowed by {@link BeamShape#spacing()} get a light.
     *
     * @return the lit positions in world space, without duplicates, in casting order
     */
    public static List<Pos> cast(BeamShape shape, Pos origin, Facing facing, LightType type, LightWorld world) {
        if (frontAborts(shape.frontRule(), world.cellAt(origin.offset(facing, 1)), type)) {
            return Collections.emptyList();
        }
        Set<Pos> lit = new LinkedHashSet<>();
        List<Ray> rays = shape.rays();
        // 1-based step at which each ray was blocked, 0 if it was not blocked (or not cast)
        int[] blockedAt = new int[rays.size()];
        for (int r = 0; r < rays.size(); r++) {
            Ray ray = rays.get(r);
            if (skipped(ray, blockedAt)) {
                continue;
            }
            List<Pos> steps = ray.steps();
            for (int s = 0; s < steps.size(); s++) {
                Pos pos = origin.offset(Rotation.rotate(steps.get(s), facing));
                LightType.Step step = type.step(world.cellAt(pos));
                if (step == LightType.Step.BLOCK) {
                    blockedAt[r] = s + 1;
                    break;
                }
                if (step == LightType.Step.LIGHT && emits(shape.spacing(), steps.get(s))) {
                    lit.add(pos);
                }
            }
        }
        return Collections.unmodifiableList(new ArrayList<>(lit));
    }

    /** The 1.7.10 cone rule: an inner ray is skipped if its outer ray was blocked before it got far enough. */
    private static boolean skipped(Ray ray, int[] blockedAt) {
        if (!ray.hasParent()) return false;
        int parentBlockedAt = blockedAt[ray.parent()];
        return parentBlockedAt != 0 && parentBlockedAt < ray.parentBlockedBefore();
    }

    /** Whether the step at {@code offset} (beam space) gets a light; the centre line is where y and z are 0. */
    private static boolean emits(Spacing spacing, Pos offset) {
        return spacing.emitsAt(offset.x(), offset.y() == 0 && offset.z() == 0);
    }

    private static boolean frontAborts(BeamShape.FrontRule rule, Cell front, LightType type) {
        switch (rule) {
            case ABORT_IF_OPAQUE:
                return front == Cell.OPAQUE;
            case ABORT_IF_BLOCKED:
                return type.step(front) == LightType.Step.BLOCK;
            default:
                return false;
        }
    }
}
