package omtteam.openmodularlighting.compatibility.opencomputers;

import li.cil.oc.api.driver.NamedBlock;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import net.minecraft.util.EnumFacing;
import omtteam.omlib.compatibility.opencomputers.AbstractOMTileEntityEnvironment;
import omtteam.omlib.tileentity.EnumMachineMode;
import omtteam.openmodularlighting.tileentity.LightingBase;

/**
 * Created by nico on 09/06/17.
 * The instance of the component wrapper for a specific turret base.
 */
public class ManagedEnvironmentLightingBase extends AbstractOMTileEntityEnvironment<LightingBase> implements NamedBlock {
    private final LightingBase base;

    ManagedEnvironmentLightingBase(LightingBase base) {
        super(base, "turret_base");
        this.base = base;
    }

    @Override
    public String preferredName() {
        return "turret_base";
    }

    @Override
    public int priority() {
        return 10;
    }

    @SuppressWarnings("unused")
    @Callback(doc = "function():string; returns owner of turret base.")
    public Object[] getOwner(Context context, Arguments args) {
        return new Object[]{base.getOwnerName()};
    }

    @SuppressWarnings("unused")
    @Callback(doc = "function():int; returns maximum energy storage.")
    public Object[] getMaxEnergyStorage(Context context, Arguments args) {
        return new Object[]{base.getMaxEnergyLevel(EnumFacing.DOWN)};
    }

    @SuppressWarnings("unused")
    @Callback(doc = "function():int; returns current energy stored.")
    public Object[] getCurrentEnergyStorage(Context context, Arguments args) {

        return new Object[]{base.getEnergyLevel(EnumFacing.DOWN)};
    }

    @SuppressWarnings("unused")
    @Callback(doc = "function():boolean; returns if the turret is currently active.")
    public Object[] getActive(Context context, Arguments args) {
        return new Object[]{base.isActive()};
    }

    @SuppressWarnings("unused")
    @Callback(doc = "function(state:int):void; toggles turret redstone inversion state.")
    public Object[] setMode(Context context, Arguments args) {
        if (!args.isInteger(0) || args.checkInteger(0) <= EnumMachineMode.values().length) {
            return new Object[]{"Set first parameter to any number between 0 and " + EnumMachineMode.values().length,
                    "0 - Always on, 1 - always off, 2 - inverted, 3 - not inverted"};
        }
        base.setMode(EnumMachineMode.values()[args.checkInteger(0)]);
        return null;
    }

    @SuppressWarnings("unused")
    @Callback(doc = "function():int; shows redstone inversion state.")
    public Object[] getInverted(Context context, Arguments args) {
        switch (base.getMode().ordinal()) {
            case 0:
                return new Object[]{"0 - Always on"};
            case 1:
                return new Object[]{"1 - always off"};
            case 2:
                return new Object[]{"2 - inverted"};
            case 3:
                return new Object[]{"3 - not inverted"};
        }
        return new Object[]{};
    }

    @SuppressWarnings("unused")
    @Callback(doc = "function():boolean; shows redstone state.")
    public Object[] getRedstone(Context context, Arguments args) {

        return new Object[]{base.getRedstone()};
    }
}
