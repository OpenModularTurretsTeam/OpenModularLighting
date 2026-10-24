package omtteam.openmodularlighting.core.light;

import omtteam.openmodularlighting.core.math.Pos;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Difference between a light's cached shape and a newly cast one (mode change or re-cast). Positions in both are
 * left alone.
 * <p>
 * {@link #removed()}: positions only in the old shape, remove this source there. {@link #added()}: positions only in
 * the new shape, place or join a light block there.
 */
public final class ShapeDiff {
    private final List<Pos> removed;
    private final List<Pos> added;

    public ShapeDiff(List<Pos> removed, List<Pos> added) {
        this.removed = Collections.unmodifiableList(new ArrayList<>(removed));
        this.added = Collections.unmodifiableList(new ArrayList<>(added));
    }

    public static ShapeDiff between(Collection<Pos> before, Collection<Pos> after) {
        Set<Pos> beforeSet = new HashSet<>(before);
        Set<Pos> afterSet = new HashSet<>(after);
        Set<Pos> removed = new LinkedHashSet<>();
        for (Pos pos : before) {
            if (!afterSet.contains(pos)) removed.add(pos);
        }
        Set<Pos> added = new LinkedHashSet<>();
        for (Pos pos : after) {
            if (!beforeSet.contains(pos)) added.add(pos);
        }
        return new ShapeDiff(new ArrayList<>(removed), new ArrayList<>(added));
    }

    public List<Pos> removed() {
        return removed;
    }

    public List<Pos> added() {
        return added;
    }

    public boolean isEmpty() {
        return removed.isEmpty() && added.isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ShapeDiff)) return false;
        ShapeDiff other = (ShapeDiff) o;
        return removed.equals(other.removed) && added.equals(other.added);
    }

    @Override
    public int hashCode() {
        return removed.hashCode() * 31 + added.hashCode();
    }

    @Override
    public String toString() {
        return "ShapeDiff[removed=" + removed + ", added=" + added + "]";
    }
}
