package omtteam.openmodularlighting.util;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import omtteam.omlib.util.compat.ItemStackTools;
import omtteam.openmodularlighting.handler.ConfigHandler;
import omtteam.openmodularlighting.tileentity.LightingBase;
import omtteam.openmodularlighting.tileentity.lights.AbstractLightSource;

import java.util.HashMap;
import java.util.Map;

import static omtteam.omlib.util.compat.ItemStackTools.getStackSize;

public class LightAddonUtil {

    public static LightingBase getBase(World world, BlockPos pos) {
        if (world == null) {
            return null;
        }

        for (EnumFacing facing : EnumFacing.values()) {
            BlockPos offsetPos = pos.offset(facing);

            if (world.getTileEntity(offsetPos) instanceof LightingBase) {
                return (LightingBase) world.getTileEntity(offsetPos);
            }
        }

        return null;
    }

    public static EnumFacing getBaseFacing(World world, BlockPos pos) {
        if (world == null) {
            return null;
        }

        for (EnumFacing facing : EnumFacing.values()) {
            BlockPos offsetPos = pos.offset(facing);

            if (world.getTileEntity(offsetPos) instanceof LightingBase) {
                return facing;
            }
        }

        return null;
    }

    public static Map<EnumFacing, AbstractLightSource> getBaseLights(World world, BlockPos pos) {
        if (world == null) {
            return null;
        }
        Map<EnumFacing, AbstractLightSource> map = new HashMap<>();

        for (EnumFacing facing : EnumFacing.values()) {
            BlockPos offsetPos = pos.offset(facing);

            if (world.getTileEntity(offsetPos) instanceof AbstractLightSource) {
                map.put(facing, (AbstractLightSource) world.getTileEntity(offsetPos));
            }
        }

        return map;
    }

    @SuppressWarnings("ConstantConditions")
    public static float getEfficiencyUpgrades(LightingBase base) {
        float efficiency = 0.0F;
        int tier = base.getTier();

        if (tier == 1) {
            return efficiency;
        }

        if (tier == 5) {
            if (base.getStackInSlot(3) != ItemStackTools.getEmptyStack()) {
                if (base.getStackInSlot(3).getItemDamage() == 1) {
                    efficiency += (ConfigHandler.getEfficiencyUpgradeBoostPercentage() * getStackSize(base.getStackInSlot(3)));
                }
            }
        }

        if (base.getStackInSlot(2) != ItemStackTools.getEmptyStack()) {
            if (base.getStackInSlot(2).getItemDamage() == 1) {
                efficiency += (ConfigHandler.getEfficiencyUpgradeBoostPercentage() * getStackSize(base.getStackInSlot(2)));
            }
        }

        return efficiency;
    }

    @SuppressWarnings("ConstantConditions")
    public static boolean hasRedstoneReactor(LightingBase base) {
        boolean found = false;
        if (base.getTier() == 1) {
            return false;
        }

        if (base.getStackInSlot(9) != ItemStackTools.getEmptyStack()) {
            found = base.getStackInSlot(9).getItemDamage() == 4;
        }

        if (base.getStackInSlot(10) != ItemStackTools.getEmptyStack() && !found) {
            found = base.getStackInSlot(10).getItemDamage() == 4;
        }
        return found;
    }

    @SuppressWarnings("ConstantConditions")
    public static int getFakeDropsLevel(LightingBase base) {
        int fakeDropsLevel = -1;

        if (base == null) {
            return fakeDropsLevel;
        }

        int tier = base.getTier();

        if (tier == 1) {
            return fakeDropsLevel;
        }

        if (base.getStackInSlot(9) != ItemStackTools.getEmptyStack()) {
            if (base.getStackInSlot(9).getItemDamage() == 7) {
                fakeDropsLevel += getStackSize(base.getStackInSlot(9));
            }
        }

        if (base.getStackInSlot(10) != ItemStackTools.getEmptyStack()) {
            if (base.getStackInSlot(10).getItemDamage() == 7) {
                fakeDropsLevel += getStackSize(base.getStackInSlot(10));
            }
        }

        return Math.min(fakeDropsLevel, 3);
    }
}
