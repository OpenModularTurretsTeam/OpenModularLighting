package omtteam.openmodularlighting.config;

import omtteam.omlib.config.ConfigManager;
import omtteam.openmodularlighting.OML;

/**
 * The config of the mod, {@code config/openmodularlighting.json}: a facade on OMLib's {@link ConfigManager}, which
 * loads, migrates, validates and writes the file and holds the server's config on clients (the library sends it when a
 * player joins). The values are documented in the file with their description, range and default, see
 * {@link omtteam.omlib.config.ConfigWriter}.
 */
public final class OMLConfig {
    private static final ConfigManager<ConfigData> MANAGER = new ConfigManager<>(OML.MOD_ID, ConfigData.class,
            ConfigData::new, ConfigData::validate, ConfigMigrationSteps.STEPS);

    private OMLConfig() {
    }

    /**
     * The config currently in effect: the server's while connected to a remote server, otherwise the local one. Do not
     * modify the returned object.
     */
    public static ConfigData get() {
        return MANAGER.get();
    }

    /**
     * This instance's own config file, also while connected to a remote server. Only for client settings that each
     * player chooses ({@code uvLightRendered}); gameplay values come from {@link #get()}. Do not modify it.
     */
    public static ConfigData local() {
        return MANAGER.local();
    }

    /** Loads the config file (writing defaults if it is missing). */
    public static void init() {
        MANAGER.load();
    }
}
