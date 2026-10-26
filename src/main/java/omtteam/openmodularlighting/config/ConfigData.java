package omtteam.openmodularlighting.config;

/**
 * The contents of {@code config/openmodularlighting.json}. Categories, names, defaults and ranges are ported from
 * 1.7.10 {@code handler/ConfigHandler}. Fields are documented and range-checked with {@link Comment} and
 * {@link Range}, which {@link ConfigWriter} turns into comments in the file.
 */
public class ConfigData {
    @Comment("Version of this file's layout, used to update old default values. Do not change it.") @Range(min = 1)
    public int configVersion = ConfigMigrationSteps.CURRENT_VERSION;
    @Comment("Enable or disable blocks (disabled blocks have no recipe).")
    public Blocks blocks = new Blocks();
    @Comment("Energy use, ranges and other behaviour of the lights.")
    public General general = new General();

    /** Clamps annotated ranges. */
    void validate(ConfigValidator v) {
        ConfigRanges.validate(this, v, "");
    }

    public static class Blocks {
        @Comment("Enables the electric floodlight.")
        public boolean electricFloodlightEnabled = true;
        @Comment("Enables the small and square fluorescent lights and the cube light.")
        public boolean smallElectricFloodlightEnabled = true;
        @Comment("Enables the carbon (non-electric) floodlight.")
        public boolean carbonFloodlightEnabled = true;
        @Comment("Enables the UV floodlight.")
        public boolean uvFloodlightEnabled = true;
        @Comment("Enables the grow light.")
        public boolean growLightEnabled = true;

        /**
         * Whether the block with this registry path is enabled (used by the {@code block_enabled} recipe condition).
         * Blocks without a switch are always enabled.
         */
        public boolean isEnabled(String block) {
            return switch (block) {
                case "electric_floodlight" -> electricFloodlightEnabled;
                case "small_fluorescent_light", "square_fluorescent_light", "cube_light" -> smallElectricFloodlightEnabled;
                case "carbon_floodlight" -> carbonFloodlightEnabled;
                case "uv_floodlight" -> uvFloodlightEnabled;
                case "grow_light" -> growLightEnabled;
                default -> true;
            };
        }
    }

    public static class General {
        @Comment("Energy use in E/t of the electric floodlight (x2 for the cone modes).")
        @Range(min = 0, max = 1000)
        public int energyUsage = 10;
        @Comment("Energy use in E/t of the fluorescent lights.")
        @Range(min = 0, max = 100)
        public int energyUsageSmallFloodlight = 1;
        @Comment("Energy use in E/t of the cube light.")
        @Range(min = 0, max = 100)
        public int energyUsageCubeLight = 2;
        @Comment("Energy use in E/t of the UV floodlight.")
        @Range(min = 0, max = 800)
        public int energyUsageUVFloodlight = 60;
        @Comment("Energy use in E/t of the grow light.")
        @Range(min = 0, max = 800)
        public int energyUsageGrowLight = 25;
        @Comment("Seconds one coal lasts in the carbon floodlight (straight mode; half of that in the cone modes).")
        @Range(min = 0, max = 1000)
        public int carbonTime = 300;
        @Comment("Range of the straight floodlight beam in blocks.")
        @Range(min = 1, max = 128)
        public int rangeStraightFloodlight = 64;
        @Comment("Range of the cone floodlight beams in blocks (the wide cone reaches a quarter of it).")
        @Range(min = 1, max = 64)
        public int rangeConeFloodlight = 32;
        @Comment("Range of the UV floodlight beam in blocks.")
        @Range(min = 1, max = 32)
        public int rangeUVFloodlight = 8;
        @Comment("Chance per second that the grow light gives the crop in front a growth tick.")
        @Range(min = 0, max = 1)
        public double chanceGrowLight = 0.1D;
        @Comment("Ticks before a floodlight that turned off can turn on again.")
        @Range(min = 1, max = 240)
        public int timeoutFloodlights = 40;
        @Comment("Damage per second dealt by UV light to living entities.")
        @Range(min = 1, max = 16)
        public double damageUVFloodlight = 4.0D;
        @Comment("Client: whether UV light blocks are visible. Each player's own setting; a server's value is not used.")
        public boolean uvLightRendered = true;
    }
}
