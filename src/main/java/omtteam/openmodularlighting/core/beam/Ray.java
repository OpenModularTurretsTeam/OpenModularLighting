package omtteam.openmodularlighting.core.beam;

import omtteam.openmodularlighting.core.math.Pos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * One ray of a beam: offsets in beam space (see {@link omtteam.openmodularlighting.core.math.Rotation}), nearest
 * first. A ray is cast step by step and stops at the first blocking block.
 * <p>
 * {@code steps}: the offsets, nearest first. {@code parent}: index of the ray (in the same shape) this ray depends on,
 * or {@link #NO_PARENT}. {@code parentBlockedBefore}: this ray is skipped if its parent was blocked at a step number
 * (1-based) below this value. This is the 1.7.10 cone rule: an inner ray is only cast if its outer ray reached far
 * enough.
 */
public final class Ray {
    public static final int NO_PARENT = -1;

    private final List<Pos> steps;
    private final int parent;
    private final int parentBlockedBefore;

    public Ray(List<Pos> steps, int parent, int parentBlockedBefore) {
        this.steps = Collections.unmodifiableList(new ArrayList<>(steps));
        this.parent = parent;
        this.parentBlockedBefore = parentBlockedBefore;
    }

    /** A ray without a parent. */
    public static Ray of(List<Pos> steps) {
        return new Ray(steps, NO_PARENT, 0);
    }

    public List<Pos> steps() {
        return steps;
    }

    public int parent() {
        return parent;
    }

    public int parentBlockedBefore() {
        return parentBlockedBefore;
    }

    public boolean hasParent() {
        return parent != NO_PARENT;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Ray)) return false;
        Ray other = (Ray) o;
        return parent == other.parent && parentBlockedBefore == other.parentBlockedBefore && steps.equals(other.steps);
    }

    @Override
    public int hashCode() {
        return Objects.hash(steps, parent, parentBlockedBefore);
    }

    @Override
    public String toString() {
        return "Ray[steps=" + steps + ", parent=" + parent + ", parentBlockedBefore=" + parentBlockedBefore + "]";
    }
}
