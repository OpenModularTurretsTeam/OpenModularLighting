package omtteam.openmodularlighting.handler;

import net.minecraftforge.common.config.Configuration;
import omtteam.openmodularlighting.OpenModularLighting;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static omtteam.omlib.util.compat.EntityTools.findClassById;

public class ConfigHandler {
    public static String recipes;

    private static List<String> stringMobBlackList;
    public static final List<String> validMobBlacklist = new ArrayList<>();

    private static int baseTierOneMaxCharge;
    private static int baseTierOneMaxIo;

    private static int baseTierTwoMaxCharge;
    private static int baseTierTwoMaxIo;

    private static int baseTierThreeMaxCharge;
    private static int baseTierThreeMaxIo;

    private static int baseTierFourMaxCharge;
    private static int baseTierFourMaxIo;

    private static int baseTierFiveMaxCharge;
    private static int baseTierFiveMaxIo;

    private static FloodLightSettings floodlightSettings;


    private static double efficiencyUpgradeBoostPercentage;


    private static boolean allowBaseCamo;

    public static void init(File configFile) {
        Configuration config = new Configuration(configFile);
        config.load();

        baseTierOneMaxCharge = config.get("TurretBaseTierOne", "MaxCharge", 500).getInt();
        baseTierOneMaxIo = config.get("TurretBaseTierOne", "MaxIo", 50).getInt();


        baseTierTwoMaxCharge = config.get("TurretBaseTierTwo", "MaxCharge", 50000).getInt();
        baseTierTwoMaxIo = config.get("TurretBaseTierTwo", "MaxIo", 100).getInt();


        baseTierThreeMaxCharge = config.get("TurretBaseTierThree", "MaxCharge", 150000).getInt();
        baseTierThreeMaxIo = config.get("TurretBaseTierThree", "MaxIo", 500).getInt();


        baseTierFourMaxCharge = config.get("TurretBaseTierFour", "MaxCharge", 500000).getInt();
        baseTierFourMaxIo = config.get("TurretBaseTierFour", "MaxIo", 1500).getInt();


        baseTierFiveMaxCharge = config.get("TurretBaseTierFive", "MaxCharge", 10000000).getInt();
        baseTierFiveMaxIo = config.get("TurretBaseTierFive", "MaxIo", 5000).getInt();


        efficiencyUpgradeBoostPercentage = config.get("upgrades", "efficiency", 0.08D,
                "Reduces power consumption linearly").getDouble();


        stringMobBlackList = Arrays.asList(config.getStringList("mobBlackList", "miscellaneous",
                new String[]{"ArmorStand"},
                "Which Entities should not be targetable by turrets? String is the name used by the /summon command."));


        recipes = config.get("miscellaneous",
                "Which recipes should we do? (auto, enderio, mekanism, vanilla)",
                "auto").getString();

        allowBaseCamo = config.get("miscellaneous",
                "Should turret bases be camouflage-able with normal blocks?", true).getBoolean();


        floodlightSettings = new FloodLightSettings(
                config.get("FloodlightSettings", "PowerUsageTierOne", 32, "RF used per tick").getInt(),
                config.get("FloodlightSettings", "PowerUsageTierTwo", 36, "RF used per tick").getInt(),
                config.get("FloodlightSettings", "PowerUsageTierThree", 40, "RF used per tick").getInt(),
                config.get("FloodlightSettings", "PowerUsageTierFour", 48, "RF used per tick").getInt(),
                config.get("FloodlightSettings", "PowerUsageTierFive", 64, "RF used per tick").getInt(),
                config.get("FloodlightSettings", "Enabled", true, "Enabled?").getBoolean());

        if (config.hasChanged()) {
            config.save();
        }
    }

    public static void parseLists() {
        parseMobBlacklist();
    }


    public static int getBaseTierOneMaxIo() {
        return baseTierOneMaxIo;
    }

    public static int getBaseTierTwoMaxIo() {
        return baseTierTwoMaxIo;
    }

    public static int getBaseTierThreeMaxIo() {
        return baseTierThreeMaxIo;
    }

    public static int getBaseTierFourMaxIo() {
        return baseTierFourMaxIo;
    }

    public static int getBaseTierFiveMaxCharge() {
        return baseTierFiveMaxCharge;
    }

    public static int getBaseTierFiveMaxIo() {
        return baseTierFiveMaxIo;
    }

    public static int getBaseTierTwoMaxCharge() {
        return baseTierTwoMaxCharge;
    }

    public static int getBaseTierThreeMaxCharge() {
        return baseTierThreeMaxCharge;
    }

    public static int getBaseTierFourMaxCharge() {
        return baseTierFourMaxCharge;
    }

    public static int getBaseTierOneMaxCharge() {
        return baseTierOneMaxCharge;
    }

    public static double getEfficiencyUpgradeBoostPercentage() {
        return efficiencyUpgradeBoostPercentage;
    }

    public static boolean isAllowBaseCamo() {
        return allowBaseCamo;
    }

    public static class FloodLightSettings {
        int[] energyUsage = new int[5];
        boolean enabled;

        FloodLightSettings(int enTOne, int enTTwo, int enTThree, int enTFour, int enTFive, boolean enabled) {
            this.enabled = enabled;
            this.energyUsage[0] = enTOne;
            this.energyUsage[1] = enTTwo;
            this.energyUsage[2] = enTThree;
            this.energyUsage[3] = enTFour;
            this.energyUsage[4] = enTFive;
        }

        public int getEnergyUsage(int tier) {
            return energyUsage[tier - 1];
        }

        public boolean isEnabled() {
            return enabled;
        }
    }

    public static FloodLightSettings getFloodlightSettings() {
        return floodlightSettings;
    }

    private static void parseMobBlacklist() {
        try {
            if (stringMobBlackList.isEmpty()) return;
            for (String itemListEntry : stringMobBlackList) {
                if (findClassById(itemListEntry) != null) {
                    validMobBlacklist.add(itemListEntry);
                }
            }
        } catch (Exception e) {
            OpenModularLighting.getLogger().error("error while parsing mob blacklist config!");
            e.printStackTrace();
        }
    }
}
