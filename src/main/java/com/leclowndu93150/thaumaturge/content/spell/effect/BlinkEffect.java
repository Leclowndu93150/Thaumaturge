package com.leclowndu93150.thaumaturge.content.spell.effect;

import com.leclowndu93150.thaumaturge.api.spell.behavior.AbstractEffectBehavior;
import com.leclowndu93150.thaumaturge.api.spell.behavior.SpellBehaviorType;
import com.leclowndu93150.thaumaturge.api.spell.cast.CastContext;
import com.leclowndu93150.thaumaturge.api.spell.cast.SpellTarget;
import com.leclowndu93150.thaumaturge.registry.TTSpellBehaviors;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

public final class BlinkEffect extends AbstractEffectBehavior {
    public static final MapCodec<BlinkEffect> CODEC = RecordCodecBuilder
            .mapCodec(i -> i.group(Codec.doubleRange(1.0, 256.0).optionalFieldOf("max_distance", 48.0).forGetter(BlinkEffect::maxDistance)).apply(i, BlinkEffect::new));

    private static final int HEADROOM_TRIES = 3;
    private static final double STAND_OFF = 0.6;

    private final double maxDistance;

    public BlinkEffect(double maxDistance) {
        this.maxDistance = maxDistance;
    }

    public double maxDistance() {
        return maxDistance;
    }

    @Override
    public SpellBehaviorType<?> type() {
        return TTSpellBehaviors.BLINK.get();
    }

    @Override
    protected boolean widens() {
        return false;
    }

    @Override
    protected void apply(CastContext ctx, SpellTarget target, float power, int index) {
        LivingEntity caster = ctx.caster();
        if (index > 0 || caster == null || caster.isPassenger() || target.entity().filter(entity -> entity == caster).isPresent()) {
            return;
        }
        Vec3 landing = landing(target);
        if (landing.distanceToSqr(caster.position()) > maxDistance * maxDistance) {
            return;
        }
        ServerLevel level = ctx.level();
        for (int lift = 0; lift < HEADROOM_TRIES; lift++) {
            Vec3 spot = landing.add(0.0, lift, 0.0);
            AABB box = caster.getDimensions(caster.getPose()).makeBoundingBox(spot);
            if (level.noCollision(caster, box)) {
                Vec3 from = caster.position();
                ctx.fx().impact(ctx.part().fx(), from.add(0.0, caster.getBbHeight() / 2.0, 0.0), ctx.color());
                caster.teleportTo(spot.x, spot.y, spot.z);
                caster.resetFallDistance();
                ctx.fx().impact(ctx.part().fx(), spot.add(0.0, caster.getBbHeight() / 2.0, 0.0), ctx.color());
                level.playSound(null, from.x, from.y, from.z, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.6F, 1.2F);
                level.playSound(null, spot.x, spot.y, spot.z, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.6F, 1.2F);
                return;
            }
        }
    }

    private static Vec3 landing(SpellTarget target) {
        if (target.block().isPresent()) {
            BlockHitResult hit = target.block().get();
            Direction face = hit.getDirection();
            if (face == Direction.UP) {
                return hit.getLocation();
            }
            return Vec3.atBottomCenterOf(hit.getBlockPos().relative(face));
        }
        return target.entity().map(entity -> besideOf(entity, target.direction())).orElse(target.position());
    }

    private static Vec3 besideOf(Entity entity, Vec3 direction) {
        Vec3 flat = new Vec3(direction.x, 0.0, direction.z);
        if (flat.lengthSqr() < Mth.EPSILON) {
            return entity.position();
        }
        return entity.position().subtract(flat.normalize().scale(entity.getBbWidth() + STAND_OFF));
    }
}
