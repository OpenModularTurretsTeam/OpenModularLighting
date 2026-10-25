package omtteam.openmodularlighting.core.light;

import omtteam.openmodularlighting.core.math.Pos;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Function;

/**
 * The lights that reach one phantom or UV light block, and which of them are on. The block is lit while at least one
 * source is on, and is removed when it has no sources left.
 * <p>
 * Every change returns an {@link Effect} telling {@code main} what to do with the block, so the blockstate is only
 * touched when the result actually changes.
 */
public final class LightSources {
    private final Set<Pos> sources = new LinkedHashSet<>();
    private final Set<Pos> activeSources = new LinkedHashSet<>();
    /** Counts real changes, so {@code main} only saves the block entity when something changed. */
    private long changes;

    /** What has to happen to the light block after a change. */
    public enum Effect {
        NONE,
        /** Set {@code LIT = true}. */
        TURN_ON,
        /** Set {@code LIT = false}. */
        TURN_OFF,
        /** No sources left: remove the block (set it to air). */
        REMOVE
    }

    /** State of a source as seen from a light block, used by {@link #refresh}. */
    public enum SourceStatus {
        ON,
        OFF,
        /** The source no longer exists, or no longer covers this block. */
        GONE,
        /** Not known (e.g. the source's chunk is not loaded): keep the stored state. */
        UNKNOWN
    }

    /** Adds a source (or updates its on/off state if it is already known). */
    public Effect add(Pos source, boolean on) {
        boolean wasLit = isLit();
        if (sources.add(source)) changes++;
        setActiveInternal(source, on);
        return effect(wasLit);
    }

    public Effect remove(Pos source) {
        boolean wasLit = isLit();
        if (sources.remove(source)) changes++;
        activeSources.remove(source);
        return effect(wasLit);
    }

    /** Switches a known source on or off. Unknown sources are ignored. */
    public Effect setActive(Pos source, boolean on) {
        boolean wasLit = isLit();
        if (sources.contains(source)) {
            setActiveInternal(source, on);
        }
        return effect(wasLit);
    }

    /**
     * Re-checks every source (random-tick cleanup): drops {@link SourceStatus#GONE} sources and updates the on/off
     * state of the others. {@link SourceStatus#UNKNOWN} sources are kept as they are.
     */
    public Effect refresh(Function<Pos, SourceStatus> status) {
        boolean wasLit = isLit();
        for (Pos source : Set.copyOf(sources)) {
            switch (status.apply(source)) {
                case ON -> setActiveInternal(source, true);
                case OFF -> setActiveInternal(source, false);
                case GONE -> {
                    sources.remove(source);
                    activeSources.remove(source);
                    changes++;
                }
                case UNKNOWN -> {
                }
            }
        }
        return effect(wasLit);
    }

    public boolean isLit() {
        return !activeSources.isEmpty();
    }

    public boolean isEmpty() {
        return sources.isEmpty();
    }

    /**
     * Grows with every change of the sources or of their on/off state (not with calls that change nothing); compare
     * two values to see whether anything changed in between.
     */
    public long changeCount() {
        return changes;
    }

    public Set<Pos> sources() {
        return Collections.unmodifiableSet(sources);
    }

    public Set<Pos> activeSources() {
        return Collections.unmodifiableSet(activeSources);
    }

    private void setActiveInternal(Pos source, boolean on) {
        if (on ? activeSources.add(source) : activeSources.remove(source)) {
            changes++;
        }
    }

    private Effect effect(boolean wasLit) {
        if (sources.isEmpty()) return Effect.REMOVE;
        boolean lit = isLit();
        if (lit == wasLit) return Effect.NONE;
        return lit ? Effect.TURN_ON : Effect.TURN_OFF;
    }
}
