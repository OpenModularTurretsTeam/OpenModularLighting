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

/**
 * equals and hashCode of the core value classes (the Java 8 stand-ins for records). {@code NULL_FIELDS}: the
 * constructors reject or copy their arguments, so those fields are never null.
 */
class EqualsContractTest {
    @Test
    void beamShape() {
        EqualsVerifier.forClass(BeamShape.class).suppress(Warning.NULL_FIELDS).verify();
    }

    @Test
    void ray() {
        EqualsVerifier.forClass(Ray.class).suppress(Warning.NULL_FIELDS).verify();
    }

    @Test
    void spacing() {
        EqualsVerifier.forClass(Spacing.class).verify();
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
