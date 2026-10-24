package omtteam.openmodularlighting.core.light;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LightTypeTest {
    @Test
    void phantomLights() {
        assertEquals(LightType.Step.LIGHT, LightType.PHANTOM.step(Cell.AIR));
        assertEquals(LightType.Step.LIGHT, LightType.PHANTOM.step(Cell.PHANTOM_LIGHT));
        assertEquals(LightType.Step.PASS, LightType.PHANTOM.step(Cell.UV_LIGHT), "UV light is passed without light");
        assertEquals(LightType.Step.PASS, LightType.PHANTOM.step(Cell.TRANSPARENT));
        assertEquals(LightType.Step.BLOCK, LightType.PHANTOM.step(Cell.OPAQUE));
        assertEquals(LightType.Step.BLOCK, LightType.PHANTOM.step(Cell.UNLOADED));
    }

    @Test
    void uvLights() {
        assertEquals(LightType.Step.LIGHT, LightType.UV.step(Cell.AIR));
        assertEquals(LightType.Step.LIGHT, LightType.UV.step(Cell.PHANTOM_LIGHT), "phantom lights are taken over");
        assertEquals(LightType.Step.LIGHT, LightType.UV.step(Cell.UV_LIGHT));
        assertEquals(LightType.Step.PASS, LightType.UV.step(Cell.TRANSPARENT));
        assertEquals(LightType.Step.BLOCK, LightType.UV.step(Cell.OPAQUE));
        assertEquals(LightType.Step.BLOCK, LightType.UV.step(Cell.UNLOADED));
    }
}
