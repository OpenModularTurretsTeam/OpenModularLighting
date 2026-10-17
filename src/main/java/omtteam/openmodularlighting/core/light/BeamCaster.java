package omtteam.openmodularlighting.core.light;

import omtteam.openmodularlighting.core.beam.BeamShape;
import omtteam.openmodularlighting.core.beam.Ray;
import omtteam.openmodularlighting.core.math.Facing;
import omtteam.openmodularlighting.core.math.Pos;
import omtteam.openmodularlighting.core.math.Rotation;

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
            return List.of();
        }
        Set<Pos> lit = new LinkedHashSet<>();
        List<Ray> rays = shape.rays();
        // 1-based step at which each ray was blocked, 0 if it was not blocked (or not cast)
        int[] blockedAt = new int[rays.size()];
        for (int r = 0; r < rays.size(); r++) {
            Ray ray = rays.get(r);
            if (ray.hasParent()) {
                int parentBlockedAt = blockedAt[ray.parent()];
                if (parentBlockedAt != 0 && parentBlockedAt < ray.parentBlockedBefore()) {
                    continue;
                }
            }
            List<Pos> steps = ray.steps();
            for (int s = 0; s < steps.size(); s++) {
                Pos pos = origin.offset(Rotation.rotate(steps.get(s), facing));
                LightType.Step step = type.step(world.cellAt(pos));
                if (step == LightType.Step.BLOCK) {
                    blockedAt[r] = s + 1;
                    break;
                }
                if (step == LightType.Step.LIGHT && shape.spacing().emitsAt(steps.get(s).x(), steps.get(s).y() == 0 && steps.get(s).z() == 0)) {
                    lit.add(pos);
                }
            }
        }
        return List.copyOf(lit);
    }

    private static boolean frontAborts(BeamShape.FrontRule rule, Cell front, LightType type) {
        return switch (rule) {
            case NONE -> false;
            case ABORT_IF_OPAQUE -> front == Cell.OPAQUE;
            case ABORT_IF_BLOCKED -> type.step(front) == LightType.Step.BLOCK;
        };
    }
}
