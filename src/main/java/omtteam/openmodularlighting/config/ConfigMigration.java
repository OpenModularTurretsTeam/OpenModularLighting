package omtteam.openmodularlighting.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * Updates config files written by older versions. Each file records its {@code configVersion}; files without it are
 * version 1. A step only replaces values that still equal the old default, so values a server owner chose stay, and
 * since the file is then saved with the current version, it runs once per file.
 */
final class ConfigMigration {
    /** Version 2: lower energy use of the electric, fluorescent, UV and grow lights, doubled grow chance. */
    static final int CURRENT_VERSION = 2;
    static final String VERSION_KEY = "configVersion";

    private ConfigMigration() {
    }

    /**
     * Migrates the JSON of a config file in place.
     *
     * @return whether anything changed
     */
    static boolean migrate(JsonObject json) {
        int version = version(json);
        if (version >= CURRENT_VERSION) return false;
        if (version < 2 && json.get("general") instanceof JsonObject general) {
            replaceDefault(general, "energyUsage", 20, 10);
            replaceDefault(general, "energyUsageSmallFloodlight", 2, 1);
            replaceDefault(general, "energyUsageUVFloodlight", 80, 60);
            replaceDefault(general, "energyUsageGrowLight", 40, 25);
            replaceDefault(general, "chanceGrowLight", 0.05, 0.1);
        }
        json.addProperty(VERSION_KEY, CURRENT_VERSION);
        return true;
    }

    private static int version(JsonObject json) {
        JsonElement version = json.get(VERSION_KEY);
        return version instanceof JsonPrimitive primitive && primitive.isNumber() ? primitive.getAsInt() : 1;
    }

    private static void replaceDefault(JsonObject section, String key, long oldDefault, long newDefault) {
        JsonElement value = section.get(key);
        if (value instanceof JsonPrimitive primitive && primitive.isNumber() && primitive.getAsLong() == oldDefault) {
            section.addProperty(key, newDefault);
        }
    }

    private static void replaceDefault(JsonObject section, String key, double oldDefault, double newDefault) {
        JsonElement value = section.get(key);
        if (value instanceof JsonPrimitive primitive && primitive.isNumber() && primitive.getAsDouble() == oldDefault) {
            section.addProperty(key, newDefault);
        }
    }
}
