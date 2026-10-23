package com.leclowndu93150.thaumaturge.content.equipment.shield;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileDeflection;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

final class ShieldReflection {
    private static final float HALF_TURN = 180.0F;
    private static final int REFLECT_SHIELD_DAMAGE = 3;
    private static final float BLOCK_SOUND_VOLUME = 1.0F;
    private static final float BLOCK_SOUND_PITCH = 0.8F;
    private static final float BLOCK_SOUND_PITCH_SPREAD = 0.4F;
    private static final ProjectileDeflection MIRROR = (projectile, deflector, random) -> {
        projectile.setDeltaMovement(projectile.getDeltaMovement().reverse());
        projectile.setYRot(projectile.getYRot() + HALF_TURN);
        projectile.yRotO += HALF_TURN;
        projectile.hasImpulse = true;
    };

    private ShieldReflection() {}

    static ItemStack blockingItem(LivingEntity blocker) {
        return blocker.isBlocking() ? blocker.getUseItem() : ItemStack.EMPTY;
    }

    static boolean facing(LivingEntity blocker, Vec3 target) {
        Vec3 view = blocker.calculateViewVector(0.0F, blocker.getYHeadRot());
        Vec3 toward = target.subtract(blocker.position());
        return new Vec3(toward.x, 0.0, toward.z).dot(view) > 0.0;
    }

    static void reflect(Projectile projectile, LivingEntity blocker, ItemStack shield) {
        projectile.deflect(MIRROR, blocker, blocker, false);
        if (blocker.level().isClientSide()) {
            return;
        }
        blocker.level()
                .playSound(
                        null,
                        blocker.getX(),
                        blocker.getY(),
                        blocker.getZ(),
                        SoundEvents.SHIELD_BLOCK,
                        blocker.getSoundSource(),
                        BLOCK_SOUND_VOLUME,
                        BLOCK_SOUND_PITCH + blocker.getRandom().nextFloat() * BLOCK_SOUND_PITCH_SPREAD);
        shield.hurtAndBreak(REFLECT_SHIELD_DAMAGE, blocker, LivingEntity.getSlotForHand(blocker.getUsedItemHand()));
    }
}
