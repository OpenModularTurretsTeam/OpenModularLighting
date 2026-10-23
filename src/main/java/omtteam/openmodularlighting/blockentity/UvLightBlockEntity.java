package omtteam.openmodularlighting.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import omtteam.openmodularlighting.block.LightBlock;
import omtteam.openmodularlighting.config.OMLConfig;
import omtteam.openmodularlighting.registry.ModBlockEntities;
import omtteam.openmodularlighting.registry.ModDamageTypes;

/**
 * Block entity of the UV light block: its UV floodlight sources (see {@link LightBlockEntity}), and the damage to
 * living entities inside it while lit (1.7.10 {@code TileEntityUVLightBlock}, CLAUDE.md §6.9).
 */
public class UvLightBlockEntity extends LightBlockEntity {
    /** Once per second; 1.7.10 used {@code worldTime % 20 == 5}. */
    private static final int DAMAGE_INTERVAL = 20;
    private static final int DAMAGE_PHASE = 5;

    public UvLightBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.UV_LIGHT, pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, UvLightBlockEntity light) {
        if (!state.getValue(LightBlock.LIT) || level.getGameTime() % DAMAGE_INTERVAL != DAMAGE_PHASE) return;
        // built from the damage type: DamageSources#source is private in vanilla 1.20.1 (Fabric API widens it, Forge does not)
        DamageSource source = new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE)
                .getHolderOrThrow(ModDamageTypes.UV_LIGHT));
        float damage = (float) OMLConfig.get().general.damageUVFloodlight;
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, new AABB(pos))) {
            entity.hurt(source, damage);
        }
    }
}
