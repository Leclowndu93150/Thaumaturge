package com.leclowndu93150.thaumaturge.content.entity.boss.hierophant;

import com.leclowndu93150.thaumaturge.content.effect.Effects;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

public final class EntityHierophantSigil extends AbstractHierophantSpell {
    public static final int WARNING_TICKS = 32;
    public static final int ACTIVE_TICKS = 12;
    public static final float RADIUS = 2.25F;
    public static final float HEIGHT = 4.0F;

    public EntityHierophantSigil(EntityType<? extends EntityHierophantSigil> type, Level level) {
        super(type, level);
    }

    @Override
    public int lifetime() {
        return WARNING_TICKS + ACTIVE_TICKS;
    }

    @Override
    protected void tickSpell(ServerLevel level, int age) {
        if (age == WARNING_TICKS) {
            level.playSound(null, blockPosition(), TTSounds.ZAP.get(), SoundSource.HOSTILE, 1.0F, 0.65F);
            Effects.arcBolt(level, position())
                    .to(position().add(0, HEIGHT, 0))
                    .color(0xB395CF)
                    .send();
        }
        if (age >= WARNING_TICKS) {
            resolveTargets(
                    level,
                    new AABB(
                            getX() - RADIUS,
                            getY(),
                            getZ() - RADIUS,
                            getX() + RADIUS,
                            getY() + HEIGHT,
                            getZ() + RADIUS),
                    position().add(0, 0.5, 0));
        }
    }

    @Override
    protected boolean intersects(LivingEntity target, int age) {
        final double reach = RADIUS + target.getBbWidth() / 2.0;
        return target.position().subtract(position()).horizontalDistanceSqr() <= reach * reach;
    }
}
