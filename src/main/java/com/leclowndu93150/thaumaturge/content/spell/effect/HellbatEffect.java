package com.leclowndu93150.thaumaturge.content.spell.effect;

import com.leclowndu93150.thaumaturge.api.spell.behavior.AbstractEffectBehavior;
import com.leclowndu93150.thaumaturge.api.spell.behavior.SpellBehaviorType;
import com.leclowndu93150.thaumaturge.api.spell.cast.CastContext;
import com.leclowndu93150.thaumaturge.api.spell.cast.SpellTarget;
import com.leclowndu93150.thaumaturge.content.entity.EntityFireBat;
import com.leclowndu93150.thaumaturge.registry.TTEntities;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import com.leclowndu93150.thaumaturge.registry.TTSpellBehaviors;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class HellbatEffect extends AbstractEffectBehavior {
    public static final MapCodec<HellbatEffect> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                    Codec.intRange(1, 64).optionalFieldOf("max_active", 16).forGetter(HellbatEffect::maxActive),
                    Codec.doubleRange(1.0, 128.0)
                            .optionalFieldOf("count_range", 32.0)
                            .forGetter(HellbatEffect::countRange))
            .apply(i, HellbatEffect::new));

    private static final String BATS = "bats";
    private static final float SPREAD = 0.5F;
    private static final double HOVER = 1.5;

    private final int maxActive;
    private final double countRange;

    public HellbatEffect(int maxActive, double countRange) {
        this.maxActive = maxActive;
        this.countRange = countRange;
    }

    public int maxActive() {
        return maxActive;
    }

    public double countRange() {
        return countRange;
    }

    @Override
    public SpellBehaviorType<?> type() {
        return TTSpellBehaviors.HELLBAT.get();
    }

    @Override
    protected boolean widens() {
        return false;
    }

    @Override
    protected void apply(CastContext ctx, SpellTarget target, float power, int index) {
        ServerLevel level = ctx.level();
        LivingEntity caster = ctx.caster();
        LivingEntity quarry = target.entity()
                .filter(entity -> entity != caster)
                .filter(LivingEntity.class::isInstance)
                .map(LivingEntity.class::cast)
                .orElse(null);
        Vec3 at = target.position();
        int active = level.getEntitiesOfClass(
                        EntityFireBat.class, new AABB(at, at).inflate(countRange), bat -> bat.owner == caster)
                .size();
        int count = Math.min(ctx.setting(BATS), maxActive - active);
        int bonus = Math.max(0, Math.round(power) - 1);
        RandomSource random = ctx.random();
        boolean spawned = false;
        for (int bat = 0; bat < count; bat++) {
            EntityFireBat fireBat = TTEntities.FIRE_BAT.get().create(level);
            if (fireBat == null) {
                continue;
            }
            fireBat.moveTo(
                    at.x + (random.nextFloat() - random.nextFloat()) * SPREAD,
                    at.y + HOVER + random.nextFloat() * SPREAD,
                    at.z + (random.nextFloat() - random.nextFloat()) * SPREAD,
                    random.nextFloat() * 360.0F,
                    0.0F);
            if (level.noCollision(fireBat)) {
                fireBat.summon(caster, quarry, bonus);
                spawned |= level.addFreshEntity(fireBat);
            }
        }
        if (spawned) {
            level.levelEvent(LevelEvent.PARTICLES_MOBBLOCK_SPAWN, BlockPos.containing(at), 0);
            level.playSound(
                    null,
                    at.x,
                    at.y,
                    at.z,
                    TTSounds.ICE.get(),
                    SoundSource.PLAYERS,
                    0.2F,
                    0.95F + random.nextFloat() * 0.1F);
        }
    }
}
