package com.leclowndu93150.thaumaturge.content.spell.delivery;

import com.leclowndu93150.thaumaturge.api.spell.behavior.SpellBehaviorType;
import com.leclowndu93150.thaumaturge.api.spell.cast.CastContext;
import com.leclowndu93150.thaumaturge.api.spell.cast.SpellStats;
import com.leclowndu93150.thaumaturge.api.spell.cast.SpellTarget;
import com.leclowndu93150.thaumaturge.content.spell.carrier.CarrierPayload;
import com.leclowndu93150.thaumaturge.content.spell.carrier.SpellCloud;
import com.leclowndu93150.thaumaturge.registry.TTSpellBehaviors;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.entity.LivingEntity;

public final class CloudDelivery extends AbstractCarrierDelivery {
    public static final MapCodec<CloudDelivery> CODEC = MapCodec.unit(new CloudDelivery());

    private static final String RADIUS = "radius";
    private static final String DURATION = "duration";
    private static final float MAX_RADIUS = 6.0F;

    @Override
    public SpellBehaviorType<?> type() {
        return TTSpellBehaviors.CLOUD.get();
    }

    @Override
    protected void launch(CastContext ctx, LivingEntity caster, CarrierPayload payload, SpellTarget origin) {
        float radius = Math.min(MAX_RADIUS, Math.max(1, ctx.setting(RADIUS)) + ctx.state().get(SpellStats.RADIUS));
        SpellCloud.spawn(ctx.level(), caster, payload, origin.position(), radius, seconds(ctx, DURATION));
    }
}
