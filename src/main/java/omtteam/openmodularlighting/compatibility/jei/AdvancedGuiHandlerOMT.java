package omtteam.openmodularlighting.compatibility.jei;

import mcp.MethodsReturnNonnullByDefault;
import mezz.jei.api.gui.IAdvancedGuiHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import omtteam.omlib.client.gui.BlockingAbstractGuiContainer;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.awt.*;

/**
 * Created by Keridos on 03/12/16.
 * This Class
 */
@MethodsReturnNonnullByDefault
class AdvancedGuiHandlerOMT implements IAdvancedGuiHandler<BlockingAbstractGuiContainer> {
    @Override
    public Class<BlockingAbstractGuiContainer> getGuiContainerClass() {
        return BlockingAbstractGuiContainer.class;
    }

    @Nullable
    @Override
    @ParametersAreNonnullByDefault
    @SideOnly(Side.CLIENT)
    public java.util.List<Rectangle> getGuiExtraAreas(BlockingAbstractGuiContainer guiContainer) {
        return guiContainer.getBlockingAreas();
    }

    @Nullable
    @Override
    @ParametersAreNonnullByDefault
    public Object getIngredientUnderMouse(BlockingAbstractGuiContainer guiContainer, int mouseX, int mouseY) {
        return null;
    }
}
