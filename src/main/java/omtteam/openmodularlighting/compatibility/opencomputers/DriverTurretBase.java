package omtteam.openmodularlighting.compatibility.opencomputers;

import li.cil.oc.api.network.ManagedEnvironment;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import omtteam.omlib.compatibility.opencomputers.AbstractOMDriver;
import omtteam.openmodularlighting.reference.OMLNames;
import omtteam.openmodularlighting.reference.Reference;
import omtteam.openmodularlighting.tileentity.LightingBase;

/**
 * Created by nico on 08/06/17.
 * The OpenComputers (Singleton) driver for a turret base.
 */
public class DriverTurretBase extends AbstractOMDriver {
    private static DriverTurretBase instance;

    private DriverTurretBase() {

    }

    public static DriverTurretBase getInstance() {
        if (instance == null) {
            instance = new DriverTurretBase();
        }
        return instance;
    }

    @Override
    public Class<?> clGetTileEntityClass() {
        return LightingBase.class;
    }

    @Override
    public ManagedEnvironment clCreateEnvironment(World world, BlockPos pos, EnumFacing side) {
        TileEntity base = world.getTileEntity(pos);
        return (base != null && base instanceof LightingBase ? new ManagedEnvironmentLightingBase((LightingBase) base) : null);
    }

    @Override
    protected String getName() {
        return Reference.MOD_ID + ":" + OMLNames.Blocks.lightingBase;
    }
}
