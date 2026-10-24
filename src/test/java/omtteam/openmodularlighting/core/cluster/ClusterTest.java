package omtteam.openmodularlighting.core.cluster;

import static omtteam.openmodularlighting.TestCollections.setOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class ClusterTest {
    /** A node on an integer line/grid; neighbours are the live nodes one step away in x or y. */
    private static final class Node implements ClusterNode<Node> {
        final Map<Long, Node> grid;
        final int x;
        final int y;
        Cluster<Node> cluster;

        Node(Map<Long, Node> grid, int x, int y) {
            this.grid = grid;
            this.x = x;
            this.y = y;
        }

        @Override
        public Cluster<Node> cluster() {
            return cluster;
        }

        @Override
        public void setCluster(Cluster<Node> cluster) {
            this.cluster = cluster;
        }

        @Override
        public Collection<Node> neighbours() {
            List<Node> result = new ArrayList<>();
            for (int[] d : new int[][]{{1, 0}, {-1, 0}, {0, 1}, {0, -1}}) {
                Node node = grid.get(key(x + d[0], y + d[1]));
                if (node != null) result.add(node);
            }
            return result;
        }
    }

    private final Map<Long, Node> grid = new HashMap<>();

    private static long key(int x, int y) {
        return ((long) x << 32) | (y & 0xffffffffL);
    }

    private Node place(int x, int y) {
        Node node = new Node(grid, x, y);
        grid.put(key(x, y), node);
        Cluster.join(node);
        return node;
    }

    private void remove(Node node) {
        grid.remove(key(node.x, node.y));
        Cluster.leave(node);
    }

    @Test
    void aLoneNodeIsItsOwnCluster() {
        Node a = place(0, 0);
        assertEquals(1, a.cluster().size());
    }

    @Test
    void adjacentNodesShareACluster() {
        Node a = place(0, 0);
        Node b = place(1, 0);
        assertSame(a.cluster(), b.cluster());
        assertEquals(2, a.cluster().size());
    }

    @Test
    void diagonalNodesDoNotConnect() {
        Node a = place(0, 0);
        Node b = place(1, 1);
        assertNotSame(a.cluster(), b.cluster());
    }

    @Test
    void aBridgingNodeMergesTwoClusters() {
        Node a = place(0, 0);
        Node c = place(2, 0);
        assertNotSame(a.cluster(), c.cluster());
        Node b = place(1, 0);
        assertSame(a.cluster(), c.cluster());
        assertSame(a.cluster(), b.cluster());
        assertEquals(3, a.cluster().size());
    }

    @Test
    void removingTheMiddleSplitsOnRefresh() {
        Node a = place(0, 0);
        Node b = place(1, 0);
        Node c = place(2, 0);
        Cluster<Node> cluster = a.cluster();
        remove(b);
        assertNull(b.cluster());
        assertTrue(cluster.isDirty());
        cluster.refresh();
        assertNotSame(a.cluster(), c.cluster());
        assertEquals(1, a.cluster().size());
        assertEquals(1, c.cluster().size());
        assertFalse(a.cluster().isDirty());
        assertFalse(c.cluster().isDirty());
    }

    @Test
    void removingAnEndKeepsTheRestTogether() {
        Node a = place(0, 0);
        Node b = place(1, 0);
        Node c = place(2, 0);
        remove(c);
        a.cluster().refresh();
        assertSame(a.cluster(), b.cluster());
        assertEquals(2, a.cluster().size());
    }

    @Test
    void aLoopSurvivesRemovingOneNode() {
        Node a = place(0, 0);
        place(1, 0);
        place(1, 1);
        Node d = place(0, 1);
        remove(d);
        a.cluster().refresh();
        assertEquals(3, a.cluster().size());
    }

    @Test
    void severalRemovalsNeedOnlyOneRefresh() {
        Node[] row = new Node[7];
        for (int i = 0; i < row.length; i++) row[i] = place(i, 0);
        Cluster<Node> cluster = row[0].cluster();
        remove(row[1]);
        remove(row[3]);
        remove(row[5]);
        cluster.refresh();
        assertEquals(4, java.util.stream.Stream.of(row[0], row[2], row[4], row[6])
                .map(Node::cluster).distinct().count());
        assertEquals(1, row[6].cluster().size());
    }

    @Test
    void aSplitOffPartCanMergeAgain() {
        Node a = place(0, 0);
        Node b = place(1, 0);
        Node c = place(2, 0);
        remove(b);
        a.cluster().refresh();
        Node again = place(1, 0);
        assertSame(a.cluster(), c.cluster());
        assertSame(a.cluster(), again.cluster());
    }

    @Test
    void joiningAgainDoesNotDuplicate() {
        Node a = place(0, 0);
        Node b = place(1, 0);
        Cluster.join(b);
        assertSame(a.cluster(), b.cluster());
        assertEquals(2, a.cluster().size());
    }

    @Test
    void joiningAgainLeavesTheOldCluster() {
        Node a = place(0, 0);
        Node b = place(1, 0);
        Cluster<Node> old = a.cluster();
        grid.remove(key(0, 0)); // b no longer sees a
        Cluster.join(b);
        assertNotSame(old, b.cluster());
        assertEquals(setOf(a), old.members());
        assertEquals(setOf(b), b.cluster().members());
    }

    @Test
    void mergingKeepsTheLargerClusterAndEmptiesTheOther() {
        Node a = place(0, 0);
        place(1, 0);
        place(2, 0);
        Cluster<Node> large = a.cluster();
        Node d = place(4, 0);
        Cluster<Node> single = d.cluster();
        Node bridge = place(3, 0);
        assertSame(large, a.cluster());
        assertSame(large, d.cluster());
        assertSame(large, bridge.cluster());
        assertEquals(5, large.size());
        assertEquals(0, single.size());
    }

    @Test
    void aDirtyClusterMergedIntoACleanOneStillSplits() {
        for (int x = 5; x <= 8; x++) place(x, 0);
        Node d1 = place(10, 0);
        Node d2 = place(11, 0);
        Node d3 = place(12, 0);
        remove(d2);
        // the bridge first joins the dirty {d1, d3}, then that moves into the larger clean row 5..8
        Node bridge = place(9, 0);
        Cluster<Node> merged = bridge.cluster();
        assertSame(merged, d3.cluster());
        assertTrue(merged.isDirty());
        merged.refresh();
        assertSame(merged, d1.cluster());
        assertNotSame(merged, d3.cluster());
        assertEquals(6, merged.size());
        assertEquals(1, d3.cluster().size());
    }

    @Test
    void clustersStopGrowingAtTheMaximum() {
        Node[] row = new Node[Cluster.MAX_SIZE + 1];
        for (int i = 0; i < row.length; i++) row[i] = place(i, 0);
        assertEquals(Cluster.MAX_SIZE, row[0].cluster().size());
        assertEquals(1, row[Cluster.MAX_SIZE].cluster().size());
    }

    @Test
    void refreshOnACleanClusterDoesNothing() {
        Node a = place(0, 0);
        Node b = place(1, 0);
        Cluster<Node> cluster = a.cluster();
        cluster.refresh();
        assertSame(cluster, b.cluster());
    }
}
