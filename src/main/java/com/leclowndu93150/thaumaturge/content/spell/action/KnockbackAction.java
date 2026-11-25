package com.leclowndu93150.thaumaturge.content.spell.action;

import com.leclowndu93150.thaumaturge.api.spell.affinity.ActionContext;
import com.leclowndu93150.thaumaturge.api.spell.affinity.SpellAction;
import com.leclowndu93150.thaumaturge.api.spell.affinity.SpellActionType;
import com.leclowndu93150.thaumaturge.registry.TTSpellActions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;

public record KnockbackAction(float base, float perPower, float lift, boolean pull) implements SpellAction {
    public static final MapCodec<KnockbackAction> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                    Codec.FLOAT.optionalFieldOf("base", 0.4F).forGetter(KnockbackAction::base),
                    Codec.FLOAT.optionalFieldOf("per_power", 0.2F).forGetter(KnockbackAction::perPower),
                    Codec.FLOAT.optionalFieldOf("lift", 0.2F).forGetter(KnockbackAction::lift),
                    Codec.BOOL.optionalFieldOf("pull", false).forGetter(KnockbackAction::pull))
            .apply(i, KnockbackAction::new));

    private static final float MAX_STRENGTH = 3.0F;

    @Override
    public SpellActionType<?> type() {
        return TTSpellActions.KNOCKBACK.get();
    }

    @Override
    public void apply(ActionContext ctx) {
        Entity entity = ActionTargets.entity(ctx).orElse(null);
        if (entity == null || entity == ctx.cast().caster() && !pull) {
            return;
        }
        float strength = Math.min(MAX_STRENGTH, ActionTargets.magnitude(base, perPower, ctx));
        if (entity instanceof LivingEntity living) {
            strength *= (float) Math.max(0.0, 1.0 - living.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
        }
        Vec3 heading = ctx.target().direction();
        Vec3 horizontal = new Vec3(heading.x, 0.0, heading.z);
        if (horizontal.lengthSqr() > 0.0) {
            horizontal = horizontal.normalize();
        }
        Vec3 push = horizontal.scale(pull ? -strength : strength).add(0.0, lift * strength, 0.0);
        Vec3 velocity = entity.getDeltaMovement();
        Vec3 nextVelocity = velocity.add(push);
        // Repeated hits must not add the same impulse indefinitely.
        Vec3 horizontalVelocity = new Vec3(nextVelocity.x, 0.0, nextVelocity.z);
        double horizontalLimit = Math.max(MAX_STRENGTH, Math.sqrt(velocity.x * velocity.x + velocity.z * velocity.z));
        if (horizontalVelocity.lengthSqr() > horizontalLimit * horizontalLimit) {
            horizontalVelocity = horizontalVelocity.normalize().scale(horizontalLimit);
            nextVelocity = new Vec3(horizontalVelocity.x, nextVelocity.y, horizontalVelocity.z);
        }
        if (push.y > 0.0) {
            double verticalLimit = Math.max(velocity.y, lift * MAX_STRENGTH);
            nextVelocity = new Vec3(nextVelocity.x, Math.min(nextVelocity.y, verticalLimit), nextVelocity.z);
        }
        entity.setDeltaMovement(nextVelocity);
        entity.hurtMarked = true;
    }
}
