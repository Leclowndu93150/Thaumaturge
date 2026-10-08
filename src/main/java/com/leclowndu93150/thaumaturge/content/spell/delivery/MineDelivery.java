package com.leclowndu93150.thaumaturge.content.spell.delivery;

import com.leclowndu93150.thaumaturge.api.spell.behavior.SpellBehaviorType;
import com.leclowndu93150.thaumaturge.api.spell.cast.CastContext;
import com.leclowndu93150.thaumaturge.api.spell.cast.SpellTarget;
import com.leclowndu93150.thaumaturge.content.spell.carrier.CarrierPayload;
import com.leclowndu93150.thaumaturge.content.spell.carrier.SpellMine;
import com.leclowndu93150.thaumaturge.registry.TTSpellBehaviors;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.entity.LivingEntity;

public final class MineDelivery extends AbstractCarrierDelivery {
    public static final MapCodec<MineDelivery> CODEC = MapCodec.unit(new MineDelivery());

    static final String TARGET = "target";
    static final int ALLIES = 1;

    @Override
    public SpellBehaviorType<?> type() {
        return TTSpellBehaviors.MINE.get();
    }

    @Override
    protected void launch(CastContext ctx, LivingEntity caster, CarrierPayload payload, SpellTarget origin) {
        SpellMine.place(ctx.level(), caster, payload, origin, ctx.setting(TARGET) == ALLIES);
    }
}
