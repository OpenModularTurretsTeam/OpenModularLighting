package omtteam.openmodularlighting.core.light;

import omtteam.openmodularlighting.core.math.Pos;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Difference between a light's cached shape and a newly cast one (mode change or re-cast). Positions in both are
 * left alone.
 *
 * @param removed positions only in the old shape: remove this source there
 * @param added   positions only in the new shape: place or join a light block there
 */
public record ShapeDiff(List<Pos> removed, List<Pos> added) {
    public ShapeDiff {
        removed = List.copyOf(removed);
        added = List.copyOf(added);
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
        return new ShapeDiff(List.copyOf(removed), List.copyOf(added));
    }

    public boolean isEmpty() {
        return removed.isEmpty() && added.isEmpty();
    }
}
