package omtteam.openmodularlighting.blockentity;

import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import omtteam.openmodularlighting.block.FloodlightBlock;
import omtteam.openmodularlighting.block.LightBlock;
import omtteam.openmodularlighting.camo.CamoBlock;
import omtteam.openmodularlighting.camo.CamoData;
import omtteam.openmodularlighting.compat.CompatBlockEntity;
import omtteam.openmodularlighting.config.OMLConfig;
import omtteam.openmodularlighting.core.beam.BeamShape;
import omtteam.openmodularlighting.core.energy.EnergyAccess;
import omtteam.openmodularlighting.core.light.LightType;
import omtteam.openmodularlighting.core.state.LightController;
import omtteam.openmodularlighting.core.state.RecheckTimer;
import omtteam.openmodularlighting.energy.ElectricPower;
import omtteam.openmodularlighting.energy.ForgeEnergyStorage;
import omtteam.openmodularlighting.energy.LightPower;
import omtteam.openmodularlighting.inventory.InsertOnlyItemHandler;
import omtteam.openmodularlighting.registry.ModBlocks;
import omtteam.openmodularlighting.world.LightBlocks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Locale;

/**
 * Base of every light (1.7.10 {@code TileEntityMetaFloodlight}): invert, colour, one item slot with its
 * {@link LightPower}, and the cached beam of light blocks (CLAUDE.md §6.3, §6.4). Subclasses define the beam shape and
 * may add a mode.
 */
public abstract class FloodlightBlockEntity extends CompatBlockEntity implements LightSourceBlockEntity, ITickable {
    /** 1.7.10 colour value for "not dyed". Other values are {@code EnumDyeColor} metadata. */
    public static final int NO_COLOR = 16;

    private static final String INVERTED = "inverted";
    private static final String ON = "on";
    private static final String TIMEOUT = "timeout";
    private static final String COLOR = "color";
    private static final String INVENTORY = "inventory";

    private final LightController controller = new LightController();
    /** Periodic re-cast (every ~20 s) for world changes that sent no block update. */
    private final RecheckTimer recheck = new RecheckTimer();
    private int color = NO_COLOR;
    private final CamoData camo = new CamoData();
    protected final ItemStackHandler inventory = new ItemStackHandler(1) {
        @Override
        public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
            return isValidItem(stack);
        }

        @Override
        protected void onContentsChanged(int slot) {
            markDirty();
        }
    };
    /** Hoppers and pipes may insert valid items only and cannot extract (1.7.10). */
    private final IItemHandler itemAccess = new InsertOnlyItemHandler(inventory);
    /** Forge Energy view of the last energy access handed out (the access objects do not change). */
    @Nullable
    private ForgeEnergyStorage energyAdapter;

    /** Light block positions of the current beam, in casting order. */
    private final LightBeam beam = new LightBeam(this);

    /** What this light runs on. */
    protected abstract LightPower power();

    /** The beam shape for the current mode and config (beam space, see {@code BeamShape}). */
    protected abstract BeamShape currentShape();

    /** Everything {@link #currentShape()} depends on (mode, ranges): a different key means re-cast. */
    protected abstract int[] shapeKey();

    protected LightType lightType() {
        return LightType.PHANTOM;
    }

    protected LightBlock lightBlock() {
        return ModBlocks.PHANTOM_LIGHT;
    }

    /** Ticks the light stays off after turning off. */
    protected int timeoutTicks() {
        return OMLConfig.get().general.timeoutFloodlights;
    }

    /** Called every server tick after the light logic (grow light: growth ticks). */
    protected void afterTick(World world, BlockPos pos, IBlockState state, LightController.TickResult result) {
    }

    public boolean isValidItem(ItemStack stack) {
        return power().isValidItem(stack);
    }

    // ---- Ticking ----

    /** Called every server tick before the power is prepared (panel lights: cluster upkeep and energy sharing). */
    protected void beforePower(World world) {
    }

    @Override
    public void update() {
        if (world != null && !world.isRemote) {
            serverTick(world, pos, world.getBlockState(pos));
        }
    }

    private void serverTick(World world, BlockPos pos, IBlockState state) {
        if (!(state.getBlock() instanceof FloodlightBlock)) return;
        beforePower(world);
        power().prepare(inventory);
        if (beam.needsRecast()) {
            controller.requestRecast(); // placed, loaded without cache, mode or config changed
        }
        if (recheck.tick(world.rand)) {
            controller.requestRecast();
        }
        LightController.TickResult result = controller.tick(world.isBlockPowered(pos), power(), timeoutTicks());
        if (result.recast()) {
            beam.recast(world);
        }
        if (result.change() != LightController.Switch.NONE) {
            beam.switchLights(world, result.change() == LightController.Switch.ON);
        }
        boolean on = controller.isOn();
        if (state.getValue(FloodlightBlock.LIT) != on) {
            // no neighbour updates: they would make the light block in front request a re-cast
            world.setBlockState(pos, state.withProperty(FloodlightBlock.LIT, on), LightBlocks.UPDATE_CLIENTS);
        }
        afterTick(world, pos, state, result);
        if (result.running() || result.recast() || result.change() != LightController.Switch.NONE) {
            markDirty();
        }
    }

    /** The mode changed: re-cast at once (1.7.10 {@code changeMode}). */
    protected void shapeChanged() {
        if (world != null && !world.isRemote) {
            beam.recast(world);
        }
        markDirty();
    }

    /** The block was removed: leave all light blocks (removing those this light was the last source of). */
    public void removeLights(World world) {
        beam.removeLights(world);
    }

    public EnumFacing facing() {
        IBlockState state = world.getBlockState(pos);
        return state.getBlock() instanceof FloodlightBlock ? state.getValue(FloodlightBlock.FACING) : EnumFacing.SOUTH;
    }

    // ---- Settings ----

    /** Translation key of the current mode, or null if this light has no mode. */
    @Nullable
    public String modeKey() {
        return null;
    }

    public boolean isInverted() {
        return controller.isInverted();
    }

    public void toggleInverted() {
        controller.toggleInverted();
        markDirty();
    }

    /**
     * Ticks until the light turns on, if it would turn on now but still waits for the timeout after turning off;
     * 0 otherwise.
     */
    public int ticksUntilOn(World world) {
        boolean active = world.isBlockPowered(pos) ^ controller.isInverted();
        return active && !controller.isOn() && power().canRun() ? controller.timeout() : 0;
    }

    /** Ticks as seconds for messages: "2" for 40 ticks, "1.5" for 30. */
    public static String seconds(int ticks) {
        return ticks % 20 == 0 ? String.valueOf(ticks / 20) : String.format(Locale.ROOT, "%.1f", ticks / 20.0);
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        if (this.color == color) return;
        this.color = color;
        syncToClient();
    }

    /** Whether this light can be disguised ({@link CamoBlock}: the electric, carbon and UV floodlights). */
    public boolean supportsCamo() {
        return world != null && world.getBlockState(pos).getBlock() instanceof CamoBlock;
    }

    /** The disguise, or {@code null} for the light's own look. */
    @Nullable
    public IBlockState getCamo() {
        return camo.get();
    }

    /** Sets the disguise ({@code null} removes it); the caller checked {@code CamoRules.isValidDisguise}. */
    public void setCamo(@Nullable IBlockState state) {
        if (supportsCamo() && camo.set(state)) {
            syncToClient();
        }
    }

    public ItemStackHandler getInventory() {
        return inventory;
    }

    /** The electric power of this light, or null if it burns fuel. */
    @Nullable
    public ElectricPower electricPower() {
        return power() instanceof ElectricPower ? (ElectricPower) power() : null;
    }

    /** Whether energy may be inserted from this side (null: unknown side); all sides by default (1.7.10). */
    protected boolean acceptsEnergyFrom(@Nullable EnumFacing side) {
        return true;
    }

    /** Energy exposed to other blocks on {@code side} (wrapped as Forge Energy), or null. */
    @Nullable
    public EnergyAccess getEnergyAccess(@Nullable EnumFacing side) {
        ElectricPower electric = electricPower();
        return electric != null && acceptsEnergyFrom(side) ? electric.store() : null;
    }

    /** The light block positions of the current beam. */
    public List<BlockPos> getShape() {
        return beam.positions();
    }

    // ---- Capabilities (items and Forge Energy) ----

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing side) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) return true;
        if (capability == CapabilityEnergy.ENERGY) return getEnergyAccess(side) != null;
        return super.hasCapability(capability, side);
    }

    @Nullable
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing side) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(itemAccess);
        }
        if (capability == CapabilityEnergy.ENERGY) {
            EnergyAccess access = getEnergyAccess(side);
            if (access == null) return null;
            if (energyAdapter == null || energyAdapter.access() != access) {
                energyAdapter = new ForgeEnergyStorage(access);
            }
            return CapabilityEnergy.ENERGY.cast(energyAdapter);
        }
        return super.getCapability(capability, side);
    }

    // ---- LightSourceBlockEntity ----

    @Override
    public boolean isLightOn() {
        return controller.isOn();
    }

    @Override
    public boolean coversLightAt(BlockPos pos) {
        return beam.covers(pos);
    }

    @Override
    public void requestRecast() {
        controller.requestRecast();
    }

    // ---- Saving ----

    /** Whether the dropped item keeps this light's settings ("Configured" tooltip). */
    public boolean hasCustomSettings() {
        return controller.isInverted() || color != NO_COLOR || camo.get() != null || power().hasStored();
    }

    /**
     * Settings kept on the dropped item: invert, colour, disguise, mode and energy / burn time. Not the beam or
     * on/off state, and not the slot contents, which are dropped separately.
     */
    public NBTTagCompound saveItemData() {
        NBTTagCompound tag = new NBTTagCompound();
        writeSettings(tag);
        return tag;
    }

    /** Settings that are also kept on the dropped item; subclasses add their mode. */
    protected void writeSettings(NBTTagCompound tag) {
        tag.setBoolean(INVERTED, controller.isInverted());
        tag.setInteger(COLOR, color);
        camo.write(tag);
        power().write(tag);
    }

    /** Reads what {@link #writeSettings} wrote. */
    protected void readSettings(NBTTagCompound tag) {
        color = tag.hasKey(COLOR) ? tag.getInteger(COLOR) : NO_COLOR;
        camo.read(tag);
        power().read(tag);
    }

    @Override
    protected void writeData(NBTTagCompound tag) {
        writeSettings(tag);
        tag.setBoolean(ON, controller.isOn());
        tag.setInteger(TIMEOUT, controller.timeout());
        tag.setTag(INVENTORY, inventory.serializeNBT());
        beam.write(tag);
    }

    @Override
    protected void readData(NBTTagCompound tag) {
        int oldColor = color;
        IBlockState oldCamo = camo.get();
        controller.load(tag.getBoolean(INVERTED), tag.getBoolean(ON), tag.getInteger(TIMEOUT));
        readSettings(tag);
        if (tag.hasKey(INVENTORY)) {
            inventory.deserializeNBT(tag.getCompoundTag(INVENTORY));
        }
        beam.read(tag);
        if (oldColor != color || oldCamo != camo.get()) {
            remesh(); // tint and disguise come from the block entity (client only)
        }
    }

    /** Clients only need the colour and the disguise (the on/off state is the {@code LIT} blockstate). */
    @Override
    protected void writeClientData(NBTTagCompound tag) {
        tag.setInteger(COLOR, color);
        camo.write(tag);
    }
}
