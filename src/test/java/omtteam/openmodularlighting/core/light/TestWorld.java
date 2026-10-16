package omtteam.openmodularlighting.core.light;

import omtteam.openmodularlighting.core.math.Pos;

import java.util.HashMap;
import java.util.Map;

/**
 * A {@link LightWorld} for tests: explicitly set cells, everything else from a fallback (air by default).
 */
final class TestWorld implements LightWorld {
    private final Map<Pos, Cell> cells = new HashMap<>();
    private final LightWorld fallback;

    private TestWorld(LightWorld fallback) {
        this.fallback = fallback;
    }

    /** Air everywhere. */
    static TestWorld open() {
        return new TestWorld(pos -> Cell.AIR);
    }

    /**
     * A deterministic random world. {@code density} is the chance of an opaque block; transparent blocks, unloaded
     * chunks, UV and phantom lights are mixed in relative to it.
     */
    static TestWorld random(long seed, double density) {
        return new TestWorld(pos -> {
            double roll = unitHash(seed, pos);
            if ((roll -= density) < 0) return Cell.OPAQUE;
            if ((roll -= density / 2) < 0) return Cell.TRANSPARENT;
            if ((roll -= density / 10) < 0) return Cell.UNLOADED;
            if ((roll -= density / 10) < 0) return Cell.UV_LIGHT;
            if ((roll -= 0.1) < 0) return Cell.PHANTOM_LIGHT;
            return Cell.AIR;
        });
    }

    TestWorld set(Pos pos, Cell cell) {
        cells.put(pos, cell);
        return this;
    }

    @Override
    public Cell cellAt(Pos pos) {
        Cell cell = cells.get(pos);
        return cell != null ? cell : fallback.cellAt(pos);
    }

    /** A hash of seed and position in [0, 1). */
    private static double unitHash(long seed, Pos pos) {
        long h = seed * 0x9E3779B97F4A7C15L;
        h ^= pos.x() * 0xC2B2AE3D27D4EB4FL;
        h ^= pos.y() * 0x165667B19E3779F9L;
        h ^= pos.z() * 0x27D4EB2F165667C5L;
        h ^= h >>> 31;
        h *= 0x94D049BB133111EBL;
        h ^= h >>> 29;
        return (h >>> 11) * 0x1.0p-53;
    }
}
