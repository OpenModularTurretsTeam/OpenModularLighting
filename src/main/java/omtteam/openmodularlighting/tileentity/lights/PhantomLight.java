package omtteam.openmodularlighting.tileentity.lights;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Created by Keridos on 03/08/17.
 * This Class
 */
public class PhantomLight extends TileEntity {
    private List<BlockPos> sources = new ArrayList<>();

    public PhantomLight() {

    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        NBTTagCompound sourceTag = compound.getCompoundTag("sources");
        int length = sourceTag.getInteger("length");
        for (int i = 0; i < length; i++) {
            int[] pos = sourceTag.getIntArray("" + i);
            sources.add(new BlockPos(pos[0], pos[1], pos[2]));
        }
    }

    @Override
    public void onLoad() {
        if (sources.size() == 0) {
            this.getWorld().setBlockToAir(this.pos);
        }
    }

    @Override
    @Nonnull
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {

        NBTTagCompound sourceTag = new NBTTagCompound();
        sourceTag.setInteger("length", sources.size());
        for (BlockPos source : sources) {
            sourceTag.setIntArray("" + sources.indexOf(source), new int[]{source.getX(), source.getY(), source.getZ()});
        }
        compound.setTag("sources", sourceTag);
        return super.writeToNBT(compound);
    }

    public void addSource(BlockPos pos) {
        if (!sources.contains(pos)) sources.add(pos);
    }

    public void removeSource(BlockPos pos) {
        if (sources.size() > 0) {
            Predicate<BlockPos> posPredicate = p -> p.equals(pos);
            sources.removeIf(posPredicate);
        }
        if (sources.size() == 0) {
            this.getWorld().setBlockToAir(this.pos);
        }
    }
}
