package com.leclowndu93150.thaumaturge.content.spell.delivery;

import com.leclowndu93150.thaumaturge.api.spell.behavior.SpellBehaviorType;
import com.leclowndu93150.thaumaturge.api.spell.cast.CastContext;
import com.leclowndu93150.thaumaturge.api.spell.cast.SpellTarget;
import com.leclowndu93150.thaumaturge.content.spell.carrier.CarrierPayload;
import com.leclowndu93150.thaumaturge.content.spell.carrier.SpellBat;
import com.leclowndu93150.thaumaturge.registry.TTSpellBehaviors;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.entity.LivingEntity;

public final class BatDelivery extends AbstractCarrierDelivery {
    public static final MapCodec<BatDelivery> CODEC = MapCodec.unit(new BatDelivery());

    @Override
    public SpellBehaviorType<?> type() {
        return TTSpellBehaviors.SPELLBAT.get();
    }

    @Override
    protected void launch(CastContext ctx, LivingEntity caster, CarrierPayload payload, SpellTarget origin) {
        SpellBat.release(ctx.level(), caster, payload, origin, ctx.setting(MineDelivery.TARGET) == MineDelivery.ALLIES);
    }
}
