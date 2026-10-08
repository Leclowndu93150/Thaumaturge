package com.leclowndu93150.thaumaturge.content.spell.carrier;

import com.leclowndu93150.thaumaturge.api.spell.cast.SpellTarget;
import com.leclowndu93150.thaumaturge.content.effect.Effects;
import com.leclowndu93150.thaumaturge.content.spell.world.SpellTargeting;
import com.leclowndu93150.thaumaturge.registry.TTEntities;
import java.util.List;
import java.util.Optional;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public final class SpellSprite extends AbstractSpellCarrier {
    private static final double ORBIT_RADIUS = 1.6;
    private static final double ORBIT_HEIGHT = 1.9;
    private static final float ORBIT_SPEED = 0.08F;
    private static final double FOLLOW = 0.3;
    private static final double HUNT_RANGE = 10.0;
    private static final int ZAP_INTERVAL = 30;
    private static final float ZAP_WIDTH = 0.4F;
    private static final double GLOW_SPREAD = 0.08;
    private static final double LEASH = 24.0;

    private float phase;

    public SpellSprite(EntityType<? extends SpellSprite> type, Level level) {
        super(type, level);
        noPhysics = true;
    }

    public static void summon(ServerLevel level, LivingEntity owner, CarrierPayload payload, int lifetime) {
        SpellSprite sprite = new SpellSprite(TTEntities.SPELL_SPRITE.get(), level);
        sprite.bind(owner, payload, lifetime);
        sprite.phase = level.getRandom().nextFloat() * (float) Math.PI * 2.0F;
        Vec3 at = owner.position().add(0.0, ORBIT_HEIGHT, 0.0);
        sprite.setPos(at.x, at.y, at.z);
        level.addFreshEntity(sprite);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {}

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide()) {
            CarrierPayload.particle(
                    level(),
                    charge.look(),
                    position()
                            .add(
                                    random.nextGaussian() * GLOW_SPREAD,
                                    random.nextGaussian() * GLOW_SPREAD,
                                    random.nextGaussian() * GLOW_SPREAD),
                    Vec3.ZERO);
            return;
        }
        LivingEntity master = getOwner();
        if (master == null || expired() || distanceToSqr(master) > LEASH * LEASH) {
            discard();
            return;
        }
        orbit(master);
        if (tickCount % ZAP_INTERVAL == 0 && level() instanceof ServerLevel level) {
            zap(level, master);
        }
    }

    private void orbit(LivingEntity master) {
        phase += ORBIT_SPEED;
        Vec3 goal = master.position().add(Math.cos(phase) * ORBIT_RADIUS, ORBIT_HEIGHT, Math.sin(phase) * ORBIT_RADIUS);
        Vec3 next = position().add(goal.subtract(position()).scale(FOLLOW));
        setPos(next.x, next.y, next.z);
    }

    private void zap(ServerLevel level, LivingEntity master) {
        Vec3 from = position();
        Optional<LivingEntity> prey = SpellTargeting.nearest(
                level,
                from,
                HUNT_RANGE,
                living -> living != master
                        && !SpellTargeting.isAlly(master, living)
                        && SpellTargeting.canSee(level, from, living, this));
        if (prey.isEmpty() || charge.isSpent()) {
            return;
        }
        Entity target = prey.get();
        Vec3 aim = target.getBoundingBox().getCenter();
        Effects.arcBolt(level, from)
                .to(aim)
                .color(charge.look().color())
                .width(ZAP_WIDTH)
                .send();
        level.playSound(
                null, from.x, from.y, from.z, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.6F, 1.6F);
        charge.resume(level, List.of(SpellTarget.entity(target, aim.subtract(from))));
    }
}
