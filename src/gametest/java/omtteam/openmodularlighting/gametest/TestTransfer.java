package omtteam.openmodularlighting.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Energy and items through the loader's API, as other mods see a block (Fabric: Team Reborn Energy / Transfer API,
 * NeoForge: capabilities). Tests use it to check the loader adapters.
 */
public interface TestTransfer {
    /** Whether the block exposes energy on {@code side}. */
    boolean hasEnergy(Level level, BlockPos pos, Direction side);

    /** @return the energy accepted (0 if the block exposes none on {@code side}) */
    long insertEnergy(Level level, BlockPos pos, Direction side, long amount);

    /** @return how many items of {@code stack} were accepted; {@code stack} is not changed */
    long insertItem(Level level, BlockPos pos, Direction side, ItemStack stack);

    /** @return how many items like {@code like} (same item and components) were taken out */
    long extractItem(Level level, BlockPos pos, Direction side, ItemStack like, long amount);
}
