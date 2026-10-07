package com.leclowndu93150.thaumaturge.content.spell.delivery;

import com.leclowndu93150.thaumaturge.api.spell.behavior.SpellBehaviorType;
import com.leclowndu93150.thaumaturge.api.spell.cast.CastContext;
import com.leclowndu93150.thaumaturge.api.spell.cast.SpellTarget;
import com.leclowndu93150.thaumaturge.content.spell.carrier.CarrierPayload;
import com.leclowndu93150.thaumaturge.content.spell.carrier.SpellSprite;
import com.leclowndu93150.thaumaturge.registry.TTSpellBehaviors;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;

public final class SummonDelivery extends AbstractCarrierDelivery {
    public static final MapCodec<SummonDelivery> CODEC = RecordCodecBuilder
            .mapCodec(i -> i.group(Codec.intRange(1, 16).optionalFieldOf("max_active", 3).forGetter(SummonDelivery::maxActive)).apply(i, SummonDelivery::new));

    private static final String DURATION = "duration";
    private static final double COUNT_RANGE = 32.0;

    private final int maxActive;

    public SummonDelivery(int maxActive) {
        this.maxActive = maxActive;
    }

    public int maxActive() {
        return maxActive;
    }

    @Override
    public SpellBehaviorType<?> type() {
        return TTSpellBehaviors.SUMMON.get();
    }

    @Override
    protected void launch(CastContext ctx, LivingEntity caster, CarrierPayload payload, SpellTarget origin) {
        AABB area = caster.getBoundingBox().inflate(COUNT_RANGE);
        if (ctx.level().getEntitiesOfClass(SpellSprite.class, area, sprite -> sprite.getOwner() == caster).size() < maxActive) {
            SpellSprite.summon(ctx.level(), caster, payload, seconds(ctx, DURATION));
        }
    }
}
