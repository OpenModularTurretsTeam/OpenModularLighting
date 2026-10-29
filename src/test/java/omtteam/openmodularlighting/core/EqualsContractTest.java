package omtteam.openmodularlighting.core;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import omtteam.openmodularlighting.core.beam.BeamShape;
import omtteam.openmodularlighting.core.beam.Ray;
import omtteam.openmodularlighting.core.beam.Spacing;
import omtteam.openmodularlighting.core.light.ShapeDiff;
import omtteam.openmodularlighting.core.math.Pos;
import omtteam.openmodularlighting.core.state.LightController;
import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * equals and hashCode of the core value records. EqualsVerifier builds records through their canonical constructors,
 * so values those reject are kept out: {@code NULL_FIELDS} (the constructors reject or copy their arguments, so those
 * fields are never null), steps of at least 1 for {@link Spacing} and rays without a parent for {@link BeamShape} (a
 * ray's parent must come before it).
 */
class EqualsContractTest {
    @Test
    void beamShape() {
        EqualsVerifier.forClass(BeamShape.class)
                .withPrefabValues(Ray.class, Ray.of(List.of(new Pos(1, 0, 0))), Ray.of(List.of(new Pos(2, 0, 0))))
                .suppress(Warning.NULL_FIELDS)
                .verify();
    }

    @Test
    void ray() {
        EqualsVerifier.forClass(Ray.class).suppress(Warning.NULL_FIELDS).verify();
    }

    @Test
    void spacing() {
        EqualsVerifier.forClass(Spacing.class).withPrefabValuesForField("step", 1, 2).verify();
    }

    @Test
    void shapeDiff() {
        EqualsVerifier.forClass(ShapeDiff.class).suppress(Warning.NULL_FIELDS).verify();
    }

    @Test
    void pos() {
        EqualsVerifier.forClass(Pos.class).verify();
    }

    @Test
    void tickResult() {
        EqualsVerifier.forClass(LightController.TickResult.class).suppress(Warning.NULL_FIELDS).verify();
    }
}
