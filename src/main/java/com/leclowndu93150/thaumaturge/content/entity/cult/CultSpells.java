package com.leclowndu93150.thaumaturge.content.entity.cult;

import com.leclowndu93150.thaumaturge.content.entity.EntityGolemOrb;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.phys.Vec3;

public final class CultSpells {
    private static final float ORB_CHANCE = 0.34F;
    private static final double ORB_LEAD_TICKS = 10.0;
    private static final float ORB_SPEED = 0.66F;
    private static final float ORB_SPREAD = 3.0F;
    private static final float ORB_SOUND_VOLUME = 1.0F;
    private static final float ORB_SOUND_PITCH_SPREAD = 0.1F;
    private static final int FIREBALL_COUNT = 3;
    private static final float FIREBALL_FAN_SPREAD = 2.5F;
    private static final int FIREBALL_LEVEL_EVENT = 1009;
    private static final double FIREBALL_LIFT = 0.5;

    private CultSpells() {}

    public static void cast(Mob caster, LivingEntity target, float distanceFactor) {
        if (!(caster.level() instanceof ServerLevel level)) {
            return;
        }
        caster.swing(caster.getUsedItemHand());
        if (caster.getRandom().nextFloat() > 1.0F - ORB_CHANCE) {
            orb(caster, level, target);
        } else {
            volley(caster, level, target, distanceFactor);
        }
    }

    public static float fireballFanSpread(float distanceFactor) {
        return Mth.sqrt(distanceFactor) * FIREBALL_FAN_SPREAD;
    }

    private static void orb(Mob caster, ServerLevel level, LivingEntity target) {
        RandomSource random = caster.getRandom();
        EntityGolemOrb blast = new EntityGolemOrb(level, caster, target, true);
        Vec3 aim = target.position()
                .add(target.getDeltaMovement().scale(ORB_LEAD_TICKS))
                .subtract(caster.position())
                .normalize();
        blast.setPos(blast.getX() + aim.x, blast.getY() + aim.y, blast.getZ() + aim.z);
        blast.shoot(aim.x, aim.y, aim.z, ORB_SPEED, ORB_SPREAD);
        caster.playSound(TTSounds.EGATTACK.get(), ORB_SOUND_VOLUME, 1.0F + random.nextFloat() * ORB_SOUND_PITCH_SPREAD);
        level.addFreshEntity(blast);
    }

    private static void volley(Mob caster, ServerLevel level, LivingEntity target, float distanceFactor) {
        RandomSource random = caster.getRandom();
        double originY = caster.getY() + caster.getBbHeight() / 2.0F;
        double dx = target.getX() - caster.getX();
        double dy = target.getBoundingBox().minY + target.getBbHeight() / 2.0F - originY;
        double dz = target.getZ() - caster.getZ();
        float spread = fireballFanSpread(distanceFactor);
        level.levelEvent(null, FIREBALL_LEVEL_EVENT, caster.blockPosition(), 0);
        for (int i = 0; i < FIREBALL_COUNT; i++) {
            Vec3 shot = new Vec3(dx + random.nextGaussian() * spread, dy, dz + random.nextGaussian() * spread);
            SmallFireball fireball = new SmallFireball(level, caster, shot.normalize());
            fireball.setPos(fireball.getX(), originY + FIREBALL_LIFT, fireball.getZ());
            level.addFreshEntity(fireball);
        }
    }
}
