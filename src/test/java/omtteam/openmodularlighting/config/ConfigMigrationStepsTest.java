package omtteam.openmodularlighting.config;

import omtteam.omlib.config.ConfigMigration;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfigMigrationStepsTest {
    private static JsonObject json(String text) {
        return JsonParser.parseString(text).getAsJsonObject();
    }

    private static boolean migrate(JsonObject config) {
        return ConfigMigration.migrate(config, ConfigMigrationSteps.STEPS);
    }

    @Test
    void oldDefaultsAreUpdated() {
        JsonObject config = json("{\"general\": {\"energyUsage\": 20, \"energyUsageSmallFloodlight\": 2,"
                + " \"energyUsageUVFloodlight\": 80, \"energyUsageGrowLight\": 40, \"chanceGrowLight\": 0.05}}");
        assertTrue(migrate(config));
        JsonObject general = config.getAsJsonObject("general");
        ConfigData.General defaults = new ConfigData.General();
        assertEquals(defaults.energyUsage, general.get("energyUsage").getAsInt());
        assertEquals(defaults.energyUsageSmallFloodlight, general.get("energyUsageSmallFloodlight").getAsInt());
        assertEquals(defaults.energyUsageUVFloodlight, general.get("energyUsageUVFloodlight").getAsInt());
        assertEquals(defaults.energyUsageGrowLight, general.get("energyUsageGrowLight").getAsInt());
        assertEquals(defaults.chanceGrowLight, general.get("chanceGrowLight").getAsDouble());
        assertEquals(ConfigMigrationSteps.CURRENT_VERSION, config.get(ConfigMigration.VERSION_KEY).getAsInt());
    }

    @Test
    void chosenValuesStay() {
        JsonObject config = json("{\"general\": {\"energyUsage\": 30, \"chanceGrowLight\": 0.2}}");
        migrate(config);
        JsonObject general = config.getAsJsonObject("general");
        assertEquals(30, general.get("energyUsage").getAsInt());
        assertEquals(0.2, general.get("chanceGrowLight").getAsDouble());
    }

    @Test
    void runsOncePerFile() {
        JsonObject config = json("{\"configVersion\": 2, \"general\": {\"energyUsage\": 20}}");
        assertFalse(migrate(config), "an owner may set the old value again");
        assertEquals(20, config.getAsJsonObject("general").get("energyUsage").getAsInt());
    }

    @Test
    void filesWithoutTheSectionOnlyGetTheVersion() {
        JsonObject config = json("{\"configVersion\": \"x\"}");
        assertTrue(migrate(config), "an invalid version counts as version 1");
        assertEquals(ConfigMigrationSteps.CURRENT_VERSION, config.get(ConfigMigration.VERSION_KEY).getAsInt());
    }
}
