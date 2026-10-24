package omtteam.openmodularlighting.blockentity;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import omtteam.openmodularlighting.OML;
import omtteam.openmodularlighting.block.LightBlock;
import omtteam.openmodularlighting.config.OMLConfig;

/**
 * Block entity of the UV light block: its UV floodlight sources (see {@link LightBlockEntity}), and the damage to
 * living entities inside it while lit (1.7.10 {@code TileEntityUVLightBlock}, CLAUDE.md §6.9).
 */
public class UvLightBlockEntity extends LightBlockEntity implements ITickable {
    /** UV light damage; death message {@code death.attack.openmodularlighting.uv_light} (1.7.10 used cactus damage). */
    public static final DamageSource UV_LIGHT = new DamageSource(OML.MOD_ID + ".uv_light");

    /** Once per second; 1.7.10 used {@code worldTime % 20 == 5}. */
    private static final int DAMAGE_INTERVAL = 20;
    private static final int DAMAGE_PHASE = 5;

    @Override
    public void update() {
        if (world == null || world.isRemote || world.getTotalWorldTime() % DAMAGE_INTERVAL != DAMAGE_PHASE) return;
        IBlockState state = world.getBlockState(pos);
        if (!(state.getBlock() instanceof LightBlock) || !state.getValue(LightBlock.LIT)) return;
        float damage = (float) OMLConfig.get().general.damageUVFloodlight;
        for (EntityLivingBase entity : world.getEntitiesWithinAABB(EntityLivingBase.class, new AxisAlignedBB(pos))) {
            entity.attackEntityFrom(UV_LIGHT, damage);
        }
    }
}
