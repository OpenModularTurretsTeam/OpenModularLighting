package omtteam.openmodularlighting.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

class ConfigDataTest {
    @Test
    void defaultsAreInsideTheirRanges() {
        ConfigValidator validator = new ConfigValidator();
        new ConfigData().validate(validator);
        assertFalse(validator.changed());
    }

    @Test
    void defaultsMatchTheOldMod() {
        ConfigData.General general = new ConfigData().general;
        assertEquals(20, general.energyUsage);
        assertEquals(64, general.rangeStraightFloodlight);
        assertEquals(32, general.rangeConeFloodlight);
        assertEquals(0.05D, general.chanceGrowLight);
        assertEquals(40, general.timeoutFloodlights);
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

    @Test
    void blockSwitchesByRegistryPath() {
        ConfigData.Blocks blocks = new ConfigData().blocks;
        blocks.carbonFloodlightEnabled = false;
        blocks.smallElectricFloodlightEnabled = false;
        assertFalse(blocks.isEnabled("carbon_floodlight"));
        assertFalse(blocks.isEnabled("small_fluorescent_light"));
        assertFalse(blocks.isEnabled("square_fluorescent_light"));
        assertTrue(blocks.isEnabled("electric_floodlight"));
        assertTrue(blocks.isEnabled("uv_floodlight"));
        assertTrue(blocks.isEnabled("grow_light"));
        assertTrue(blocks.isEnabled("something_without_a_switch"));
    }

    @Test
    void writtenFileReadsBack() {
        String text = ConfigWriter.write(new ConfigData(), new ConfigData());
        assertTrue(text.contains("// Range: 1 to 128\n    // Default: 64\n    \"rangeStraightFloodlight\": 64,"), text);
        ConfigData back = new Gson().fromJson(JsonParser.parseString(text), ConfigData.class);
        assertEquals(4.0D, back.general.damageUVFloodlight);
        assertTrue(back.blocks.growLightEnabled);
    }
}
