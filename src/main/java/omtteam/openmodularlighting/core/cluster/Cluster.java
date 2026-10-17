package omtteam.openmodularlighting.core.cluster;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * A group of connected {@link ClusterNode}s. Clusters are built incrementally: {@link #join} merges, {@link #leave}
 * only marks the cluster dirty, and the (possible) split happens in {@link #refresh()}, which the members call from
 * their tick. So breaking a whole wall of blocks in one tick costs one flood fill, not one per block.
 * <p>
 * A cluster never grows beyond {@link #MAX_SIZE} members; nodes that would not fit stay in a cluster of their own.
 * Clusters are not saved and carry no data themselves, they only say who is connected to whom.
 *
 * @param <N> the node type
 */
public final class Cluster<N extends ClusterNode<N>> {
    public static final int MAX_SIZE = 256;

    /** Insertion order, so that splits are deterministic. */
    private final Set<N> members = new LinkedHashSet<>();
    private boolean dirty;

    private Cluster() {
    }

    /** The members; do not change the nodes' connections while iterating. */
    public Set<N> members() {
        return Collections.unmodifiableSet(members);
    }

    public int size() {
        return members.size();
    }

    /** Whether {@link #refresh()} has a split to look for. */
    public boolean isDirty() {
        return dirty;
    }

    /** Puts {@code node} into a new cluster and merges that with the clusters of its neighbours as far as they fit. */
    public static <N extends ClusterNode<N>> void join(N node) {
        leave(node);
        Cluster<N> own = new Cluster<>();
        own.members.add(node);
        node.setCluster(own);
        for (N neighbour : node.neighbours()) {
            Cluster<N> other = neighbour.cluster();
            Cluster<N> current = node.cluster();
            if (other != null && other != current && current.size() + other.size() <= MAX_SIZE) {
                merge(current, other);
            }
        }
    }

    /** Takes {@code node} out of its cluster. The rest of that cluster may have fallen apart: see {@link #refresh()}. */
    public static <N extends ClusterNode<N>> void leave(N node) {
        Cluster<N> cluster = node.cluster();
        if (cluster == null) return;
        cluster.members.remove(node);
        cluster.dirty = !cluster.members.isEmpty();
        node.setCluster(null);
    }

    /** Moves the smaller cluster into the larger one. */
    private static <N extends ClusterNode<N>> void merge(Cluster<N> a, Cluster<N> b) {
        Cluster<N> large = a.size() >= b.size() ? a : b;
        Cluster<N> small = large == a ? b : a;
        for (N member : small.members) {
            large.members.add(member);
            member.setCluster(large);
        }
        large.dirty |= small.dirty;
        small.members.clear();
    }

    /**
     * If members left since the last call: finds the connected parts again (flood fill over the members only). The
     * first part stays in this cluster, every other part gets a new one. Do nothing if the cluster is not dirty.
     */
    public void refresh() {
        if (!dirty) return;
        dirty = false;
        Set<N> unvisited = new LinkedHashSet<>(members);
        List<List<N>> parts = new ArrayList<>();
        while (!unvisited.isEmpty()) {
            N start = unvisited.iterator().next();
            unvisited.remove(start);
            List<N> part = new ArrayList<>();
            Deque<N> queue = new ArrayDeque<>();
            queue.add(start);
            while (!queue.isEmpty()) {
                N node = queue.poll();
                part.add(node);
                for (N neighbour : node.neighbours()) {
                    if (unvisited.remove(neighbour)) {
                        queue.add(neighbour);
                    }
                }
            }
            parts.add(part);
        }
        if (parts.size() <= 1) return;
        members.clear();
        members.addAll(parts.get(0));
        for (List<N> part : parts.subList(1, parts.size())) {
            Cluster<N> split = new Cluster<>();
            split.members.addAll(part);
            for (N node : part) {
                node.setCluster(split);
            }
        }
    }
}
