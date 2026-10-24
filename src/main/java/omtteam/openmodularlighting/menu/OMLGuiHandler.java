package omtteam.openmodularlighting.menu;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import omtteam.openmodularlighting.OML;
import omtteam.openmodularlighting.blockentity.CarbonFloodlightBlockEntity;
import omtteam.openmodularlighting.blockentity.FloodlightBlockEntity;
import omtteam.openmodularlighting.energy.ElectricPower;

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

    /** The menu for a GUI id at {@code pos}, on either side; null if the block there does not fit. */
    @Nullable
    public static FloodlightMenu createMenu(int id, EntityPlayer player, World world, BlockPos pos) {
        TileEntity tile = world.getTileEntity(pos);
        if (!(tile instanceof FloodlightBlockEntity)) return null;
        FloodlightBlockEntity light = (FloodlightBlockEntity) tile;
        if (id == ELECTRIC) {
            ElectricPower power = light.electricPower();
            return power == null ? null : new ElectricFloodlightMenu(player.inventory, light, power);
        }
        if (id == CARBON && light instanceof CarbonFloodlightBlockEntity) {
            return new CarbonFloodlightMenu(player.inventory, (CarbonFloodlightBlockEntity) light);
        }
        return null;
    }

    @Nullable
    @Override
    public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        return createMenu(id, player, world, new BlockPos(x, y, z));
    }

    @Nullable
    @Override
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        FloodlightMenu menu = createMenu(id, player, world, new BlockPos(x, y, z));
        return menu == null ? null : OML.proxy.createScreen(menu);
    }
}
