package omtteam.openmodularlighting.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.Containers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import omtteam.openmodularlighting.block.FloodlightBlock;
import omtteam.openmodularlighting.block.LightBlock;
import omtteam.openmodularlighting.camo.CamoBlock;
import omtteam.openmodularlighting.camo.CamoData;
import omtteam.openmodularlighting.camo.CamoHost;
import omtteam.openmodularlighting.camo.CamoRules;
import omtteam.openmodularlighting.compat.CompatBlockEntity;
import omtteam.openmodularlighting.compat.VersionCompat;
import omtteam.openmodularlighting.compat.nbt.NbtCompat;
import omtteam.openmodularlighting.config.OMLConfig;
import omtteam.openmodularlighting.core.beam.BeamShape;
import omtteam.openmodularlighting.core.camo.CamoLight;
import omtteam.openmodularlighting.core.light.LightType;
import omtteam.openmodularlighting.core.state.LightController;
import omtteam.openmodularlighting.core.state.RecheckTimer;
import omtteam.openmodularlighting.core.energy.EnergyAccess;
import omtteam.openmodularlighting.energy.ElectricPower;
import omtteam.openmodularlighting.energy.LightPower;
import omtteam.openmodularlighting.inventory.ItemAccess;
import omtteam.openmodularlighting.registry.ModBlocks;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;

/**
 * Base of every light (1.7.10 {@code TileEntityMetaFloodlight}): invert, colour, one item slot with its
 * {@link LightPower}, and the cached beam of light blocks (CLAUDE.md §6.3, §6.4). Subclasses define the beam shape and
 * may add a mode.
 */
public abstract class FloodlightBlockEntity extends CompatBlockEntity implements LightSourceBlockEntity, TransferHost, CamoHost {
    /** 1.7.10 colour value for "not dyed". Other values are {@code DyeColor} ids. */
    public static final int NO_COLOR = 16;

    private static final String INVERTED = "inverted";
    private static final String ON = "on";
    private static final String TIMEOUT = "timeout";
    private static final String COLOR = "color";

    private final LightController controller = new LightController();
    /** Periodic re-cast (every ~20 s) for world changes that sent no block update. */
    private final RecheckTimer recheck = new RecheckTimer();
    private int color = NO_COLOR;
    private final CamoData camo = new CamoData("camo", CamoRules::isValidDisguise);
    protected final SimpleContainer inventory = new SimpleContainer(1) {
        @Override
        public boolean canPlaceItem(int slot, ItemStack stack) {
            return isValidItem(stack);
        }
    };
    /** Hoppers and pipes may insert valid items only and cannot extract (1.7.10). */
    private final ItemAccess itemAccess = ItemAccess.insertOnly(inventory);

    /** Light block positions of the current beam, in casting order. */
    private final LightBeam beam = new LightBeam(this);

    protected FloodlightBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        inventory.addListener(container -> setChanged());
    }

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
    protected void afterTick(Level level, BlockPos pos, BlockState state, LightController.TickResult result) {
    }

    public boolean isValidItem(ItemStack stack) {
        return power().isValidItem(stack);
    }

    // ---- Ticking ----

    /** Called every server tick before the power is prepared (panel lights: cluster upkeep and energy sharing). */
    protected void beforePower(Level level) {
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, FloodlightBlockEntity light) {
        light.beforePower(level);
        light.power().prepare(light.inventory);
        if (light.beam.needsRecast()) {
            light.controller.requestRecast(); // placed, loaded without cache, mode or config changed
        }
        if (light.recheck.tick(() -> level.random.nextLong())) {
            light.controller.requestRecast();
        }
        LightController.TickResult result = light.controller.tick(level.hasNeighborSignal(pos), light.power(),
                light.timeoutTicks());
        if (result.recast()) {
            light.beam.recast(level);
        }
        if (result.change() != LightController.Switch.NONE) {
            light.beam.switchLights(level, result.change() == LightController.Switch.ON);
        }
        boolean on = light.controller.isOn();
        if (state.getValue(FloodlightBlock.LIT) != on) {
            // no neighbour updates: they would make the light block in front request a re-cast
            level.setBlock(pos, state.setValue(FloodlightBlock.LIT, on), Block.UPDATE_CLIENTS);
        }
        light.afterTick(level, pos, state, result);
        if (result.running() || result.recast() || result.change() != LightController.Switch.NONE) {
            light.setChanged();
        }
    }

    /** The mode changed: re-cast at once (1.7.10 {@code changeMode}). */
    protected void shapeChanged() {
        if (level != null && !level.isClientSide()) {
            beam.recast(level);
        }
        setChanged();
    }

    /** The block was removed: leave all light blocks (removing those this light was the last source of). */
    public void removeLights(Level level) {
        beam.removeLights(level);
    }

    /** Broken or replaced: leave the light blocks and drop the slot contents. */
    @Override
    protected void onBlockRemoved(Level level) {
        removeLights(level);
        Containers.dropContents(level, worldPosition, inventory);
    }

    public Direction facing() {
        return getBlockState().getValue(FloodlightBlock.FACING);
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
        setChanged();
    }

    /**
     * Ticks until the light turns on, if it would turn on now but still waits for the timeout after turning off;
     * 0 otherwise.
     */
    public int ticksUntilOn(Level level) {
        boolean active = level.hasNeighborSignal(worldPosition) ^ controller.isInverted();
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
        return getBlockState().getBlock() instanceof CamoBlock;
    }

    /** The disguise, or {@code null} for the light's own look. */
    @Nullable
    @Override
    public BlockState getCamo() {
        return camo.get();
    }

    /** Sets the disguise ({@code null} removes it); the caller checked {@code CamoRules.isValidDisguise}. */
    @Override
    public void setCamo(@Nullable BlockState state) {
        if (supportsCamo() && camo.set(state)) {
            syncToClient();
        }
    }

    @Override
    public boolean isCamoEnabled() {
        return supportsCamo();
    }

    /** The lights have their own light logic: no light sliders in the camouflage menu. */
    @Override
    public boolean supportsLight() {
        return false;
    }

    @Override
    public CamoLight getLight() {
        return CamoLight.DEFAULT;
    }

    @Override
    public void setLight(CamoLight light) {
    }

    /** Lights have no owner: whoever can open the GUI of a light sees and sets its disguise. */
    @Override
    public boolean canSeeThroughCamo(Player player) {
        return true;
    }

    @Override
    public boolean canSetCamo(Player player) {
        return supportsCamo();
    }

    @Override
    public boolean canChangeLight(Player player) {
        return false;
    }

    @Override
    public boolean hasParentMenu() {
        return true;
    }

    /** "Back" from the camouflage menu: the GUI of the light. */
    @Override
    public void openParentMenu(ServerPlayer player) {
        if (getBlockState().getBlock() instanceof FloodlightBlock block) {
            block.openMenu(player, this);
        }
    }

    @Override
    protected @Nullable Object renderData() {
        return camo.get();
    }

    public SimpleContainer getInventory() {
        return inventory;
    }

    /** The slot as other blocks see it, on every side (the loader adapters wrap it). */
    @Override
    public ItemAccess getItemAccess(@Nullable Direction side) {
        return itemAccess;
    }

    /** The electric power of this light, or null if it burns fuel. */
    @Nullable
    public ElectricPower electricPower() {
        return power() instanceof ElectricPower electric ? electric : null;
    }

    /** Whether energy may be inserted from this side (null: unknown side); all sides by default (1.7.10). */
    protected boolean acceptsEnergyFrom(@Nullable Direction side) {
        return true;
    }

    /** Energy exposed to other blocks on {@code side} (the loader adapters wrap it), or null. */
    @Nullable
    @Override
    public EnergyAccess getEnergyAccess(@Nullable Direction side) {
        ElectricPower electric = electricPower();
        return electric != null && acceptsEnergyFrom(side) ? electric.store() : null;
    }

    /** The light block positions of the current beam. */
    public List<BlockPos> getShape() {
        return beam.positions();
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
    public CompoundTag saveItemData() {
        CompoundTag tag = new CompoundTag();
        writeSettings(tag);
        return tag;
    }

    /** Settings that are also kept on the dropped item; subclasses add their mode. */
    protected void writeSettings(CompoundTag tag) {
        tag.putBoolean(INVERTED, controller.isInverted());
        tag.putInt(COLOR, color);
        camo.write(tag);
        power().write(tag);
    }

    /** Reads what {@link #writeSettings} wrote. */
    protected void readSettings(CompoundTag tag) {
        color = NbtCompat.getInt(tag, COLOR, NO_COLOR);
        camo.read(tag);
        power().read(tag);
    }

    @Override
    protected void writeData(CompoundTag tag, HolderLookup.Provider registries) {
        writeSettings(tag);
        tag.putBoolean(ON, controller.isOn());
        tag.putInt(TIMEOUT, controller.timeout());
        VersionCompat.saveItems(tag, NonNullList.of(ItemStack.EMPTY, inventory.getItem(0)), registries);
        beam.write(tag);
    }

    @Override
    protected void readData(CompoundTag tag, HolderLookup.Provider registries) {
        int oldColor = color;
        BlockState oldCamo = camo.get();
        controller.load(NbtCompat.getBoolean(tag, INVERTED, false), NbtCompat.getBoolean(tag, ON, false),
                NbtCompat.getInt(tag, TIMEOUT, 0));
        readSettings(tag);
        NonNullList<ItemStack> items = NonNullList.withSize(1, ItemStack.EMPTY);
        VersionCompat.loadItems(tag, items, registries);
        inventory.setItem(0, items.get(0));
        beam.read(tag);
        if (oldColor != color || oldCamo != camo.get()) {
            remesh(); // tint and disguise come from the block entity (client only)
        }
    }

    /** Clients only need the colour and the disguise (the on/off state is the {@code LIT} blockstate). */
    @Override
    protected void writeClientData(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putInt(COLOR, color);
        camo.write(tag);
    }
}
