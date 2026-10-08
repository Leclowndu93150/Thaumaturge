package com.leclowndu93150.thaumaturge.content.spell.delivery;

import com.leclowndu93150.thaumaturge.api.spell.behavior.SpellBehavior;
import com.leclowndu93150.thaumaturge.api.spell.cast.CastContext;
import com.leclowndu93150.thaumaturge.api.spell.cast.SpellStats;
import com.leclowndu93150.thaumaturge.api.spell.cast.SpellTarget;
import com.leclowndu93150.thaumaturge.content.spell.carrier.CarrierPayload;
import java.util.List;
import net.minecraft.world.entity.LivingEntity;

public abstract class AbstractCarrierDelivery implements SpellBehavior {
    protected static final int TICKS_PER_SECOND = 20;

    @Override
    public final void execute(CastContext ctx, List<SpellTarget> incoming) {
        LivingEntity caster = ctx.caster();
        if (caster == null || !ctx.hasNext()) {
            return;
        }
        CarrierPayload payload = new CarrierPayload(ctx.continuation(), SpellLook.downstream(ctx));
        for (SpellTarget origin : incoming) {
            launch(ctx, caster, payload, origin);
        }
    }

    protected abstract void launch(CastContext ctx, LivingEntity caster, CarrierPayload payload, SpellTarget origin);

    protected static int seconds(CastContext ctx, String setting) {
        return Math.round(Math.max(1, ctx.setting(setting))
                * TICKS_PER_SECOND
                * ctx.state().get(SpellStats.DURATION));
    }

    @Override
    public boolean standalone() {
        return false;
    }
}
