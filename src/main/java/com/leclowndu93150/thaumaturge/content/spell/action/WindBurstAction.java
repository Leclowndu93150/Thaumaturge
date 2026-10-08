package com.leclowndu93150.thaumaturge.content.spell.action;

import com.leclowndu93150.thaumaturge.api.spell.affinity.ActionContext;
import com.leclowndu93150.thaumaturge.api.spell.affinity.SpellAction;
import com.leclowndu93150.thaumaturge.api.spell.affinity.SpellActionType;
import com.leclowndu93150.thaumaturge.registry.TTSpellActions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.windcharge.AbstractWindCharge;
import net.minecraft.world.entity.projectile.windcharge.WindCharge;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public record WindBurstAction(float base, float perPower, float max) implements SpellAction {
    public static final MapCodec<WindBurstAction> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                    Codec.FLOAT.optionalFieldOf("base", 1.0F).forGetter(WindBurstAction::base),
                    Codec.FLOAT.optionalFieldOf("per_power", 0.4F).forGetter(WindBurstAction::perPower),
                    Codec.FLOAT.optionalFieldOf("max", 3.0F).forGetter(WindBurstAction::max))
            .apply(i, WindBurstAction::new));

    @Override
    public SpellActionType<?> type() {
        return TTSpellActions.WIND_BURST.get();
    }

    @Override
    public void apply(ActionContext ctx) {
        ServerLevel level = ctx.cast().level();
        Vec3 at = ctx.target().position();
        float radius = Math.min(max, ActionTargets.magnitude(base, perPower, ctx) + ctx.radius());
        WindCharge charge = new WindCharge(EntityType.WIND_CHARGE, level);
        charge.setPos(at);
        charge.setOwner(ctx.cast().caster());
        level.explode(
                charge,
                null,
                AbstractWindCharge.EXPLOSION_DAMAGE_CALCULATOR,
                at.x,
                at.y,
                at.z,
                radius,
                false,
                Level.ExplosionInteraction.TRIGGER,
                ParticleTypes.GUST_EMITTER_SMALL,
                ParticleTypes.GUST_EMITTER_LARGE,
                SoundEvents.WIND_CHARGE_BURST);
    }
}
