package omtteam.openmodularlighting.core.cluster;

import java.util.Collection;

/**
 * A member of a {@link Cluster}: something that can be connected to its neighbours, such as a block entity. The node
 * stores the reference to its cluster itself, so no registry is needed.
 *
 * @param <N> the implementing type
 */
public interface ClusterNode<N extends ClusterNode<N>> {
    /** The cluster this node is in, or null if it has not joined (yet) or has left. */
    Cluster<N> cluster();

    void setCluster(Cluster<N> cluster);

    /**
     * The nodes this one connects to right now: adjacent, compatible, loaded and not removed. May include nodes that
     * have no cluster yet. Called with the world as it is now, so it must not rely on cached state.
     */
    Collection<N> neighbours();
}
