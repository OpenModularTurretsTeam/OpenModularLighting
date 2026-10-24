package omtteam.openmodularlighting.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

class ConfigDataTest {
    @Test
    void defaultsAreInsideTheirRanges() {
        ConfigValidator validator = new ConfigValidator();
        new ConfigData().validate(validator);
        assertFalse(validator.changed());
    }

    @Test
    void outOfRangeValuesAreClamped() {
        ConfigData data = new ConfigData();
        data.general.rangeStraightFloodlight = 500;
        data.general.chanceGrowLight = -1;
        ConfigValidator validator = new ConfigValidator();
        data.validate(validator);
        assertEquals(128, data.general.rangeStraightFloodlight);
        assertEquals(0D, data.general.chanceGrowLight);
        assertTrue(validator.changed());
    }

    /** The blocks that are disabled when only the switch {@code field} is off. */
    private static Set<String> disabledBy(String field) throws ReflectiveOperationException {
        ConfigData.Blocks blocks = new ConfigData().blocks;
        ConfigData.Blocks.class.getField(field).setBoolean(blocks, false);
        Set<String> disabled = new HashSet<>();
        for (String block : new String[]{"electric_floodlight", "small_fluorescent_light", "square_fluorescent_light",
                "cube_light", "carbon_floodlight", "uv_floodlight", "grow_light", "something_without_a_switch"}) {
            if (!blocks.isEnabled(block)) disabled.add(block);
        }
        return disabled;
    }

    @Test
    void blockSwitchesByRegistryPath() throws ReflectiveOperationException {
        assertEquals(Collections.singleton("electric_floodlight"), disabledBy("electricFloodlightEnabled"));
        assertEquals(new HashSet<>(Arrays.asList("small_fluorescent_light", "square_fluorescent_light", "cube_light")),
                disabledBy("smallElectricFloodlightEnabled"));
        assertEquals(Collections.singleton("carbon_floodlight"), disabledBy("carbonFloodlightEnabled"));
        assertEquals(Collections.singleton("uv_floodlight"), disabledBy("uvFloodlightEnabled"));
        assertEquals(Collections.singleton("grow_light"), disabledBy("growLightEnabled"));
    }

    @Test
    void writtenFileReadsBack() {
        String text = ConfigWriter.write(new ConfigData(), new ConfigData());
        assertTrue(text.contains("// Range: 1 to 128\n    // Default: 64\n    \"rangeStraightFloodlight\": 64,"), text);
        ConfigData back = new Gson().fromJson(new JsonParser().parse(text), ConfigData.class);
        assertEquals(4.0D, back.general.damageUVFloodlight);
        assertTrue(back.blocks.growLightEnabled);
    }
}
