package omtteam.openmodularlighting.tileentity.lights;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import omtteam.omlib.tileentity.TileEntityOwnedBlock;
import omtteam.openmodularlighting.tileentity.LightingBase;
import omtteam.openmodularlighting.util.LightAddonUtil;

import java.util.ArrayList;
import java.util.List;

import static omtteam.openmodularlighting.util.LightAddonUtil.getBaseFacing;

/**
 * Created by Keridos on 27/07/17.
 * This Class
 */
public abstract class AbstractLightSource extends TileEntityOwnedBlock {
    protected LightingBase base;
    protected EnumFacing orientation;
    protected List<BlockPos> placedLights = new ArrayList<>();
    protected boolean turnedOn;
    protected boolean active;


    public AbstractLightSource() {
        this.orientation = EnumFacing.NORTH;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbtTagCompound) {
        super.writeToNBT(nbtTagCompound);
        nbtTagCompound.setByte("direction", (byte) orientation.ordinal());
        return nbtTagCompound;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbtTagCompound) {
        super.readFromNBT(nbtTagCompound);
        if (nbtTagCompound.hasKey("direction")) {
            this.setOrientation(EnumFacing.getFront(nbtTagCompound.getByte("direction")));
        }
    }

    public void setSide() {
        this.setOrientation(getBaseFacing(this.getWorld(), this.pos));
    }

    public LightingBase getBase() {
        return LightAddonUtil.getBase(this.getWorld(), this.pos);
    }

    public EnumFacing getOrientation() {
        return orientation;
    }

    private void setOrientation(EnumFacing orientation) {
        this.orientation = orientation;
    }

    abstract protected boolean energySufficient(int ticks);

    abstract protected void drainEnergy(int ticks);

    abstract protected int getEnergyUsage();
}
