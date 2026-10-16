package omtteam.openmodularlighting.config;

import omtteam.openmodularlighting.OML;

/**
 * Clamps config values into their allowed ranges and logs every correction.
 */
final class ConfigValidator {
    private boolean changed;

    /** Whether any value had to be corrected. */
    boolean changed() {
        return changed;
    }

    int range(String name, int value, int min, int max) {
        if (value < min || value > max) {
            int fixed = Math.max(min, Math.min(max, value));
            warn(name, value, fixed, "[" + min + ", " + max + "]");
            return fixed;
        }
        return value;
    }

    double range(String name, double value, double min, double max) {
        if (Double.isNaN(value) || value < min || value > max) {
            double fixed = Double.isNaN(value) ? min : Math.max(min, Math.min(max, value));
            warn(name, value, fixed, "[" + min + ", " + max + "]");
            return fixed;
        }
        return value;
    }

    private void warn(String name, Object value, Object fixed, String range) {
        changed = true;
        OML.LOGGER.warn("Config: {} = {} is outside {}, using {}", name, value, range, fixed);
    }
}
