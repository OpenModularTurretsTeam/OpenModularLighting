package omtteam.openmodularlighting.config;

import omtteam.omlib.config.ConfigMigration;
import com.google.gson.JsonObject;

import java.util.List;

/**
 * The config migrations of the mod, run by {@link ConfigMigration}. Add a step (and keep the older ones) when a default
 * changes; {@link #CURRENT_VERSION} follows the last step.
 */
final class ConfigMigrationSteps {
    static final List<ConfigMigration.Step> STEPS = List.of(
            new ConfigMigration.Step(2, ConfigMigrationSteps::lightDefaults));

    static final int CURRENT_VERSION = ConfigMigration.currentVersion(STEPS);

    private ConfigMigrationSteps() {
    }

    /** Version 2: lower energy use of the electric, fluorescent, UV and grow lights, doubled grow chance. */
    private static void lightDefaults(JsonObject json) {
        if (json.get("general") instanceof JsonObject general) {
            ConfigMigration.replaceDefault(general, "energyUsage", 20, 10);
            ConfigMigration.replaceDefault(general, "energyUsageSmallFloodlight", 2, 1);
            ConfigMigration.replaceDefault(general, "energyUsageUVFloodlight", 80, 60);
            ConfigMigration.replaceDefault(general, "energyUsageGrowLight", 40, 25);
            ConfigMigration.replaceDefault(general, "chanceGrowLight", 0.05, 0.1);
        }
    }
}
