package omtteam.openmodularlighting.compatibility.computercraft;


import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.peripheral.IPeripheralProvider;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import omtteam.openmodularlighting.tileentity.LightingBase;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Created by Keridos on 05/02/2016.
 * This Class
 */

public class CCPeripheralProvider implements IPeripheralProvider {
    private static CCPeripheralProvider instance;

    private CCPeripheralProvider() {
    }

    public static CCPeripheralProvider getInstance() {
        if (instance == null) {
            instance = new CCPeripheralProvider();
        }
        return instance;
    }

    @Override
    @ParametersAreNonnullByDefault
    public IPeripheral getPeripheral(World world, BlockPos pos, EnumFacing side) {
        if (world.getTileEntity(pos) instanceof LightingBase) {
            return (IPeripheral) world.getTileEntity(pos);
        }
        return null;
    }
}