package omtteam.openmodularlighting.core.beam;

import omtteam.openmodularlighting.core.math.Pos;

import java.util.List;

/**
 * One ray of a beam: offsets in beam space (see {@link omtteam.openmodularlighting.core.math.Rotation}), nearest
 * first. A ray is cast step by step and stops at the first blocking block.
 *
 * @param steps               the offsets, nearest first
 * @param parent              index of the ray (in the same shape) this ray depends on, or {@link #NO_PARENT}
 * @param parentBlockedBefore this ray is skipped if its parent was blocked at a step number (1-based) below this
 *                            value. This is the 1.7.10 cone rule: an inner ray is only cast if its outer ray reached
 *                            far enough.
 */
public record Ray(List<Pos> steps, int parent, int parentBlockedBefore) {
    public static final int NO_PARENT = -1;

    public Ray {
        steps = List.copyOf(steps);
    }

    /** A ray without a parent. */
    public static Ray of(List<Pos> steps) {
        return new Ray(steps, NO_PARENT, 0);
    }

    public boolean hasParent() {
        return parent != NO_PARENT;
    }
}
