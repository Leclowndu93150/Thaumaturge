package com.leclowndu93150.thaumaturge.content.spell.delivery;

import com.leclowndu93150.thaumaturge.api.spell.behavior.SpellBehavior;
import com.leclowndu93150.thaumaturge.api.spell.behavior.SpellBehaviorType;
import com.leclowndu93150.thaumaturge.api.spell.cast.CastContext;
import com.leclowndu93150.thaumaturge.api.spell.cast.SpellTarget;
import com.leclowndu93150.thaumaturge.registry.TTSpellBehaviors;
import com.mojang.serialization.MapCodec;
import java.util.List;
import net.minecraft.world.entity.LivingEntity;

public record SelfDelivery() implements SpellBehavior {
    public static final MapCodec<SelfDelivery> CODEC = MapCodec.unit(new SelfDelivery());

    @Override
    public SpellBehaviorType<?> type() {
        return TTSpellBehaviors.SELF.get();
    }

    @Override
    public void execute(CastContext ctx, List<SpellTarget> incoming) {
        LivingEntity caster = ctx.caster();
        if (caster == null) {
            return;
        }
        SpellLook look = SpellLook.downstream(ctx);
        ctx.fx().impact(look.fx(), caster.getBoundingBox().getCenter(), look.color());
        ctx.proceed(List.of(SpellTarget.entity(caster, caster.getLookAngle())));
    }
}
