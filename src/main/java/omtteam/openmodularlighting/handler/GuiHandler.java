package omtteam.openmodularlighting.handler;


import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import omtteam.openmodularlighting.client.gui.*;
import omtteam.openmodularlighting.client.gui.containers.*;
import omtteam.openmodularlighting.tileentity.LightingBase;

public class GuiHandler implements IGuiHandler {
    private static GuiHandler instance;

    private GuiHandler() {
    }

    public static GuiHandler getInstance() {
        if (instance == null) {
            instance = new GuiHandler();
        }
        return instance;
    }

    @Override
    public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        TileEntity tileEntity = world.getTileEntity(new BlockPos(x, y, z));

        switch (id) {
            case 1:
                return new LightingBaseTierOneContainer(player.inventory, (LightingBase) tileEntity);
            case 2:
                return new LightingBaseTierTwoContainer(player.inventory, (LightingBase) tileEntity);
            case 3:
                return new LightingBaseTierThreeContainer(player.inventory, (LightingBase) tileEntity);
            case 4:
                return new LightingBaseTierFourContainer(player.inventory, (LightingBase) tileEntity);
            case 5:
                return new LightingBaseTierFiveContainer(player.inventory, (LightingBase) tileEntity);
            default:
                return null;
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        TileEntity tileEntity = world.getTileEntity(new BlockPos(x, y, z));

        switch (id) {
            case 1:
                return new LightingBaseTierOneGUI(player.inventory, (LightingBase) tileEntity);
            case 2:
                return new LightingBaseTierTwoGUI(player.inventory, (LightingBase) tileEntity);
            case 3:
                return new LightingBaseTierThreeGUI(player.inventory, (LightingBase) tileEntity);
            case 4:
                return new LightingBaseTierFourGUI(player.inventory, (LightingBase) tileEntity);
            case 5:
                return new LightingBaseTierFiveGUI(player.inventory, (LightingBase) tileEntity);
            default:
                return null;
        }
    }
}