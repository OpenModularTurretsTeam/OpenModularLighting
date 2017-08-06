package omtteam.openmodularlighting.compatibility;

import dan200.computercraft.api.ComputerCraftAPI;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import omtteam.openmodularlighting.OpenModularLighting;
import omtteam.openmodularlighting.compatibility.computercraft.CCPeripheralProvider;
import omtteam.openmodularlighting.compatibility.opencomputers.DriverTurretBase;
import omtteam.openmodularlighting.reference.Reference;

import static omtteam.omlib.compatibility.ModCompatibility.ComputerCraftLoaded;
import static omtteam.omlib.compatibility.ModCompatibility.OpenComputersLoaded;

/**
 * Created by Keridos on 23/01/2015. This Class
 */
public class ModCompatibility {
    public static boolean IGWModLoaded = false;
    public static boolean ThermalExpansionLoaded = false;
    public static boolean EnderIOLoaded = false;
    public static boolean MekanismLoaded = false;
    public static boolean ThaumcraftLoaded = false;
    public static boolean ValkyrienWarfareLoaded = false;

    private static void checkForMods() {
        ThermalExpansionLoaded = Loader.isModLoaded("ThermalExpansion");
        if (ThermalExpansionLoaded) {
            OpenModularLighting.getLogger().info("Hi there, dV=V0B(t1-t0)! (Found ThermalExpansion)");
        }

        EnderIOLoaded = Loader.isModLoaded("EnderIO");
        if (EnderIOLoaded) {
            OpenModularLighting.getLogger().info("Not sure if iron ingot, or electrical steel ingot... (Found EnderIO)");
        }

        MekanismLoaded = Loader.isModLoaded("Mekanism");
        if (MekanismLoaded) {
            OpenModularLighting.getLogger().info("Mur omsimu, plz. (Found Mekanism)");
        }

        ThaumcraftLoaded = Loader.isModLoaded("Thaumcraft");
        if (ThaumcraftLoaded) {
            OpenModularLighting.getLogger().info("Afrikaners is plesierig. (Found Thaumcraft)");
        }

        if (OpenComputersLoaded || ComputerCraftLoaded) {
            OpenModularLighting.getLogger().info("Enabling LUA integration. (Found OpenComputers/ComputerCraft)");
        }

        ValkyrienWarfareLoaded = Loader.isModLoaded("valkyrienwarfare");
        if (ValkyrienWarfareLoaded) {
            OpenModularLighting.getLogger().info("Valkyrien Warfare Found! You have a good taste in mods");
        }

        IGWModLoaded = Loader.isModLoaded("IGWMod");
    }

    private static void addVersionCheckerInfo() {
        NBTTagCompound versionchecker = new NBTTagCompound();
        versionchecker.setString("curseProjectName", "openmodularlighting");
        versionchecker.setString("curseFilenameParser", "OpenModularLighting-1.10.2-[].jar");
        versionchecker.setString("modDisplayName", "Open Modular Lighting");
        versionchecker.setString("oldVersion", Reference.VERSION);
        FMLInterModComms.sendRuntimeMessage("omtteam/openmodularlighting", "VersionChecker", "addCurseCheck", versionchecker);
    }

    public static void init() {
        FMLInterModComms.sendMessage("Waila", "register",
                "omtteam.openmodularlighting.compatibility.hwyla.WailaDataProvider.register");

        addVersionCheckerInfo();
        if (ComputerCraftLoaded) {
            registerCCCompat();
        }
        if (OpenComputersLoaded) {
            registerOCCompat();
        }
    }

    public static void preinit() {
        checkForMods();
    }

    private static void registerOCCompat() {
        DriverTurretBase.getInstance().registerWrapper();
    }

    private static void registerCCCompat() {
        ComputerCraftAPI.registerPeripheralProvider(CCPeripheralProvider.getInstance());
    }
}
