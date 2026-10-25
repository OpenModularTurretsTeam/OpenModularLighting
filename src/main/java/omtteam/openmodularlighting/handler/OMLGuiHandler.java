package omtteam.openmodularlighting.handler;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import omtteam.openmodularlighting.OML;
import omtteam.openmodularlighting.container.CarbonFloodlightContainer;
import omtteam.openmodularlighting.container.FloodlightContainer;
import omtteam.openmodularlighting.container.ElectricFloodlightContainer;
import omtteam.openmodularlighting.energy.ElectricPower;
import omtteam.openmodularlighting.tileentity.CarbonFloodlightTileEntity;
import omtteam.openmodularlighting.tileentity.FloodlightTileEntity;

import javax.annotation.Nullable;

/**
 * Opens the light GUIs: the server creates the menu, the client the menu and its screen (through the proxy, so main
 * code does not reference client classes). The GUI id says which menu.
 */
public final class OMLGuiHandler implements IGuiHandler {
    /** The shared GUI of all electric lights. */
    public static final int ELECTRIC = 0;
    /** The carbon floodlight's GUI. */
    public static final int CARBON = 1;

    /** The container for a GUI id at {@code pos}, on either side; null if the block there does not fit. */
    @Nullable
    public static FloodlightContainer createContainer(int id, EntityPlayer player, World world, BlockPos pos) {
        TileEntity tile = world.getTileEntity(pos);
        if (!(tile instanceof FloodlightTileEntity)) return null;
        FloodlightTileEntity light = (FloodlightTileEntity) tile;
        if (id == ELECTRIC) {
            ElectricPower power = light.electricPower();
            return power == null ? null : new ElectricFloodlightContainer(player.inventory, light, power);
        }
        if (id == CARBON && light instanceof CarbonFloodlightTileEntity) {
            return new CarbonFloodlightContainer(player.inventory, (CarbonFloodlightTileEntity) light);
        }
        return null;
    }

    @Nullable
    @Override
    public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        return createContainer(id, player, world, new BlockPos(x, y, z));
    }

    @Nullable
    @Override
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        FloodlightContainer menu = createContainer(id, player, world, new BlockPos(x, y, z));
        return menu == null ? null : OML.proxy.createGui(menu);
    }
}
