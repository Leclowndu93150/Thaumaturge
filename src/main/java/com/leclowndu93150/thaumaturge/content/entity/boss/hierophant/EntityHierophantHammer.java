package com.leclowndu93150.thaumaturge.content.entity.boss.hierophant;

import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public final class EntityHierophantHammer extends AbstractHierophantSpell {
    public static final float SPEED = 0.7F;
    public static final float GRAVITY = 0.012F;
    private static final int LIFETIME = 30;
    private static final double HIT_RADIUS = 0.65;
    private static final double IMPACT_RADIUS = 1.25;
    private static final int TRAIL_INTERVAL = 2;
    private static final int SHARD_COUNT = 24;
    private static final int DUST_COLOR = 0x70517F;

    public EntityHierophantHammer(EntityType<? extends EntityHierophantHammer> type, Level level) {
        super(type, level);
    }
    @Override
    public int lifetime() {
        return LIFETIME;
    }
    public Vec3 center(float age) {
        return position().add(forward().scale(age * SPEED)).add(0, -GRAVITY * age * age / 2, 0);
    }
    @Override
    protected void tickSpell(ServerLevel level, int age) {
        final Vec3 previous = center(Math.max(0, age - 1));
        final Vec3 center = center(age);
        final HitResult wall = level.clip(new ClipContext(previous, center, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
        if (wall.getType() != HitResult.Type.MISS) {
            final Vec3 impact = wall.getLocation().subtract(forward().scale(0.05));
            resolveTargets(level, new AABB(impact, impact).inflate(IMPACT_RADIUS), impact);
            shatter(level, impact);
            return;
        }
        if (resolveTargets(level, new AABB(previous, center).inflate(HIT_RADIUS), previous) || age == LIFETIME - 1) {
            shatter(level, center);
            return;
        }
        if (age % TRAIL_INTERVAL == 0) {
            level.sendParticles(new DustParticleOptions(DUST_COLOR, 0.8F), center.x, center.y, center.z, 2, 0.08, 0.08, 0.08, 0);
        }
    }

    private void shatter(ServerLevel level, Vec3 point) {
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, TTBlocks.STONE_ELDRITCH_TILE.get().defaultBlockState()), point.x, point.y, point.z, SHARD_COUNT, 0.3, 0.3, 0.3, 0.12);
        level.sendParticles(new DustParticleOptions(DUST_COLOR, 1.5F), point.x, point.y, point.z, SHARD_COUNT, 0.45, 0.45, 0.45, 0);
        level.playSound(null, point.x, point.y, point.z, TTSounds.ICE.get(), SoundSource.HOSTILE, 1.2F, 0.65F);
        discard();
    }
}
