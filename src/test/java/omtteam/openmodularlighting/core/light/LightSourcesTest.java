package omtteam.openmodularlighting.core.light;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import omtteam.openmodularlighting.core.light.LightSources.Effect;
import omtteam.openmodularlighting.core.light.LightSources.SourceStatus;
import omtteam.openmodularlighting.core.math.Pos;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

class LightSourcesTest {
    private static final Pos A = new Pos(1, 2, 3);
    private static final Pos B = new Pos(4, 5, 6);
    private static final Pos C = new Pos(7, 8, 9);

    @Test
    void litWhileAnySourceIsOn() {
        LightSources sources = new LightSources();
        assertEquals(Effect.NONE, sources.add(A, false));
        assertFalse(sources.isLit());
        assertEquals(Effect.TURN_ON, sources.add(B, true));
        assertEquals(Effect.NONE, sources.setActive(A, true));
        assertEquals(Effect.NONE, sources.setActive(B, false));
        assertEquals(Effect.TURN_OFF, sources.setActive(A, false));
        assertFalse(sources.isLit());
    }

    @Test
    void removedWhenNoSourcesAreLeft() {
        LightSources sources = new LightSources();
        sources.add(A, true);
        sources.add(B, false);
        assertEquals(Effect.TURN_OFF, sources.remove(A));
        assertEquals(Effect.REMOVE, sources.remove(B));
        assertTrue(sources.isEmpty());
    }

    @Test
    void addingAKnownSourceUpdatesItsState() {
        LightSources sources = new LightSources();
        sources.add(A, false);
        assertEquals(Effect.TURN_ON, sources.add(A, true));
        assertEquals(Set.of(A), sources.sources());
    }

    @Test
    void unknownSourcesCannotBeSwitched() {
        LightSources sources = new LightSources();
        sources.add(A, false);
        assertEquals(Effect.NONE, sources.setActive(B, true));
        assertFalse(sources.isLit());
        assertEquals(Set.of(A), sources.sources());
    }

    @Test
    void refreshDropsGoneSourcesAndFixesStaleState() {
        LightSources sources = new LightSources();
        sources.add(A, true);
        sources.add(B, false);
        sources.add(C, true);
        Map<Pos, SourceStatus> status = Map.of(A, SourceStatus.GONE, B, SourceStatus.ON, C, SourceStatus.UNKNOWN);
        assertEquals(Effect.NONE, sources.refresh(status::get));
        assertEquals(Set.of(B, C), sources.sources());
        assertEquals(Set.of(B, C), sources.activeSources());

        assertEquals(Effect.TURN_OFF, sources.refresh(Map.of(B, SourceStatus.OFF, C, SourceStatus.OFF)::get));
        assertEquals(Effect.REMOVE, sources.refresh(pos -> SourceStatus.GONE));
    }
}
