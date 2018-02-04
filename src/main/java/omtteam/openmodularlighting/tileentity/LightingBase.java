package omtteam.openmodularlighting.tileentity;

import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import omtteam.omlib.api.IDebugTile;
import omtteam.omlib.power.OMEnergyStorage;
import omtteam.omlib.tileentity.EnumMachineMode;
import omtteam.omlib.tileentity.ICamoSupport;
import omtteam.omlib.tileentity.TileEntityMachine;
import omtteam.omlib.util.compat.ItemStackList;
import omtteam.openmodularlighting.items.AddonMetaItem;
import omtteam.openmodularlighting.items.UpgradeMetaItem;
import omtteam.openmodularlighting.reference.OMLNames;
import omtteam.openmodularlighting.reference.Reference;
import omtteam.openmodularturrets.api.network.INetworkTile;
import omtteam.openmodularturrets.api.network.IPowerExchangeTile;
import omtteam.openmodularturrets.api.network.OMTNetwork;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

import static omtteam.omlib.util.BlockUtil.getBlockStateFromNBT;
import static omtteam.omlib.util.BlockUtil.writeBlockFromStateToNBT;
import static omtteam.omlib.util.GeneralUtil.getMachineModeLocalization;

@SuppressWarnings("unused")
@Optional.InterfaceList({
        @Optional.Interface(iface = "dan200.computercraft.api.peripheral.IPeripheral", modid = "ComputerCraft"),
        @Optional.Interface(iface = "omtteam.openmodularturrets.api.network.INetworkTile", modid = "openmodularturrets"),
        @Optional.Interface(iface = "omtteam.openmodularturrets.api.network.IPowerExchangeTile", modid = "openmodularturrets")}
)
public class LightingBase extends TileEntityMachine implements IPeripheral, ICamoSupport, IDebugTile, INetworkTile, IPowerExchangeTile {
    protected IBlockState camoBlockState;

    private int upperBoundMaxRange;

    private ArrayList<IComputerAccess> comp;
    protected int tier;
    private Object omtNetwork;

    public LightingBase() {
        super();
        this.inventory = ItemStackList.create(4);
    }

    public LightingBase(int MaxEnergyStorage, int MaxIO, int tier, IBlockState camoState) {
        super();
        this.upperBoundMaxRange = 0;

        this.storage = new OMEnergyStorage(MaxEnergyStorage, MaxIO);
        this.inventory = ItemStackList.create(tier == 5 ? 4 : tier == 4 ? 3 : tier == 3 ? 2 : tier == 2 ? 1 : 0);
        this.tier = tier;
        this.camoBlockState = camoState;
        this.mode = EnumMachineMode.INVERTED;
        this.maxStorageEU = tier * 7500D;
    }

    @SuppressWarnings("deprecation")
    @Override
    @Nonnull
    public IBlockState getDefaultCamoState() {
        return ForgeRegistries.BLOCKS.getValue(
                new ResourceLocation(Reference.MOD_ID + ":" + OMLNames.Blocks.lightingBase)).getStateFromMeta(this.tier - 1);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbtTagCompound) {
        super.writeToNBT(nbtTagCompound);
        nbtTagCompound.setInteger("upperBoundMaxRange", this.upperBoundMaxRange);
        nbtTagCompound.setInteger("tier", this.tier);
        nbtTagCompound.setInteger("mode", this.mode.ordinal());
        writeBlockFromStateToNBT(nbtTagCompound, this.camoBlockState);
        return nbtTagCompound;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbtTagCompound) {
        super.readFromNBT(nbtTagCompound);
        this.upperBoundMaxRange = nbtTagCompound.getInteger("upperBoundMaxRange");
        this.tier = nbtTagCompound.getInteger("tier");
        this.mode = EnumMachineMode.values()[nbtTagCompound.getInteger("mode")];
        if (getBlockStateFromNBT(nbtTagCompound) != null) {
            this.camoBlockState = getBlockStateFromNBT(nbtTagCompound);
        } else {
            this.camoBlockState = getDefaultCamoState();
        }
        this.maxStorageEU = tier * 7500D;
    }


    @Override
    public void update() {
        super.update();
        if (!this.getWorld().isRemote && dropBlock) {
            this.getWorld().destroyBlock(this.pos, true);
            return;
        }

        long ticks = this.getWorld().getTotalWorldTime();
        if (!this.getWorld().isRemote && ticks % 5 == 0) {

            //maxRange update, needs to happen on both client and server else GUI information may become disjoint.
            //moved by Keridos, added the sync to MessageLightingBase, should sync properly now too.
            setBaseUpperBoundRange();

            //Thaumcraft
            /*if (ModCompatibility.ThaumcraftLoaded && LightAddonUtil.hasPotentiaUpgradeAddon(this)) {
                if (amountOfPotentia > 0.05F && !(storage.getMaxEnergyLevel() - storage.getEnergyLevel() == 0)) {
                    if (VisNetHandler.drainVis(this.getWorld(), xCoord, yCoord, zCoord, Aspect.ORDER, 5) == 5) {
                        this.amountOfPotentia = this.amountOfPotentia - 0.05F;
                        this.storage.modifyEnergyStored(Math.round(ConfigHandler.getPotentiaToRFRatio() * 5));
                    } else {
                        this.amountOfPotentia = this.amountOfPotentia - 0.05F;
                        this.storage.modifyEnergyStored(Math.round(ConfigHandler.getPotentiaToRFRatio() / 2));
                    }
                }
            }*/


        }
    }

    private void setBaseUpperBoundRange() {
        // tier1: 16, tier2: 24, tier3: 32, tier4: 48 and tier5: 64
        upperBoundMaxRange = tier < 3 ? 8 + tier * 8 : tier < 5 ? 16 + tier * 8 : 24 + tier * 8;
    }

    @Override
    public boolean isItemValidForSlot(int i, ItemStack stack) {

        if (i >= 0 && i <= 1) {
            return stack.getItem() instanceof AddonMetaItem;
        } else if (i >= 2 && i <= 3) {
            return stack.getItem() instanceof UpgradeMetaItem;
        }
        return false;
    }

    public int getTier() {
        return tier;
    }

    public void setTier(int tier) {
        this.tier = tier;
    }

    public NBTTagCompound writeMemoryCardNBT() {
        NBTTagCompound nbtTagCompound = new NBTTagCompound();
        nbtTagCompound.setInteger("mode", mode.ordinal());
        return nbtTagCompound;
    }

    @Override
    public void setNetwork(OMTNetwork network) {
        this.omtNetwork = network;
    }

    public void readMemoryCardNBT(NBTTagCompound nbtTagCompound) {
        if (nbtTagCompound.hasKey("mode")) {
            this.mode = EnumMachineMode.values()[nbtTagCompound.getInteger("mode")];
        } else {
            this.mode = EnumMachineMode.INVERTED;
        }
    }

    public int getUpperBoundMaxRange() {
        return upperBoundMaxRange;
    }

    @Nonnull
    @Override
    public IBlockState getCamoState() {
        return camoBlockState != null ? camoBlockState : this.getDefaultCamoState();
    }

    @Override
    public void setCamoState(IBlockState state) {
        this.camoBlockState = state;
    }

    @Override
    @Nonnull
    @ParametersAreNonnullByDefault
    public int[] getSlotsForFace(EnumFacing side) {
        return new int[]{};
    }

    @Override
    @ParametersAreNonnullByDefault
    public boolean canInsertItem(int index, ItemStack itemStackIn, EnumFacing direction) {
        return false;
    }

    @Override
    @ParametersAreNonnullByDefault
    public boolean canExtractItem(int index, ItemStack itemStackIn, EnumFacing direction) {
        return false;
    }

    @Override
    public List<String> getDebugInfo() {
        List<String> debugInfo = new ArrayList<>();
        debugInfo.add("Camo: " + this.camoBlockState.getBlock().getRegistryName());
        debugInfo.add(", UpperMaxRange: " + this.upperBoundMaxRange);
        return debugInfo;
    }

    @Optional.Method(modid = "ComputerCraft")
    @Override
    @Nonnull
    public String getType() {
        // peripheral.getType returns whaaaaat?
        return "light_base";
    }

    @Optional.Method(modid = "ComputerCraft")
    @Override
    @Nonnull
    public String[] getMethodNames() {
        // list commands you want..
        return new String[]{commands.getOwner.toString(), commands.getActive.toString(), commands.getMode.toString(),
                commands.getRedstone.toString(), commands.setMode.toString(), commands.getType.toString()};
    }

    @Optional.Method(modid = "ComputerCraft")
    @Override
    @ParametersAreNonnullByDefault
    public Object[] callMethod(IComputerAccess computer, ILuaContext context, int method, Object[] arguments) throws LuaException, InterruptedException {
        // method is command
        boolean b;
        int i;
        switch (commands.values()[method]) {
            case getOwner:
                return new Object[]{this.getOwner()};
            case getActive:
                return new Object[]{this.active};
            case getMode:
                return new Object[]{getMachineModeLocalization(this.mode)};
            case getRedstone:
                return new Object[]{this.redstone};
            case setMode:
                String arg = arguments[0].toString();
                if (!(arg.equals("0") || arg.equals("1") || arg.equals("2") || arg.equals("3"))) {
                    return new Object[]{"wrong arguments, expect number between 0 and 3"};
                }
                int mode = (Integer.valueOf(arguments[0].toString()));
                this.setMode(EnumMachineMode.values()[mode]);
                return new Object[]{true};
            case getType:
                return new Object[]{this.getType()};
            default:
                break;
        }
        return new Object[]{false};
    }

    @Optional.Method(modid = "ComputerCraft")
    @Override
    @ParametersAreNonnullByDefault
    public void attach(IComputerAccess computer) {
        if (comp == null) {
            comp = new ArrayList<>();
        }
        comp.add(computer);
    }

    @Optional.Method(modid = "ComputerCraft")
    @Override
    @ParametersAreNonnullByDefault
    public void detach(IComputerAccess computer) {
        if (comp == null) {
            comp = new ArrayList<>();
        }
        comp.remove(computer);
    }

    @Optional.Method(modid = "ComputerCraft")
    @Override
    public boolean equals(IPeripheral other) {
        return other.getType().equals(getType());
    }

    public enum commands {
        getOwner, getActive, getMode, getRedstone, setMode, getType
    }

    @Optional.Method(modid = "openmodularturrets")
    @Override
    public boolean requiresEnergy() {
        return true;
    }

    @Optional.Method(modid = "openmodularturrets")
    @Override
    public boolean deliversEnergy() {
        return false;
    }

    @Optional.Method(modid = "openmodularturrets")
    @Override
    public OMEnergyStorage getEnergyStorage() {
        return storage;
    }

    @Optional.Method(modid = "openmodularturrets")
    @Override
    public String getDeviceName() {
        return "Lighting Base";
    }

    @Optional.Method(modid = "openmodularturrets")
    @Nullable
    @Override
    public OMTNetwork getNetwork() {
        return (OMTNetwork) omtNetwork;
    }

    @Optional.Method(modid = "openmodularturrets")
    @Nonnull
    @Override
    public BlockPos getPosition() {
        return this.getPos();
    }
}
