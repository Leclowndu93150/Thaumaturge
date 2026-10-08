package com.leclowndu93150.thaumaturge.content.spell.action;

import com.leclowndu93150.thaumaturge.api.spell.affinity.ActionContext;
import com.leclowndu93150.thaumaturge.api.spell.affinity.SpellAction;
import com.leclowndu93150.thaumaturge.api.spell.affinity.SpellActionType;
import com.leclowndu93150.thaumaturge.registry.TTSpellActions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.common.Tags;

public record TeleportAction(float base, float perPower) implements SpellAction {
    public static final MapCodec<TeleportAction> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                    Codec.FLOAT.optionalFieldOf("base", 4.0F).forGetter(TeleportAction::base),
                    Codec.FLOAT.optionalFieldOf("per_power", 2.0F).forGetter(TeleportAction::perPower))
            .apply(i, TeleportAction::new));

    private static final int ATTEMPTS = 16;
    private static final float MAX_RANGE = 24.0F;

    @Override
    public SpellActionType<?> type() {
        return TTSpellActions.TELEPORT.get();
    }

    @Override
    public void apply(ActionContext ctx) {
        LivingEntity living = ActionTargets.living(ctx).orElse(null);
        if (living == null
                || living.isPassenger()
                || living.getType().is(Tags.EntityTypes.BOSSES)
                || living == ctx.cast().caster()) {
            return;
        }
        float range = Math.min(MAX_RANGE, ActionTargets.magnitude(base, perPower, ctx));
        RandomSource random = ctx.cast().random();
        double x = living.getX();
        double y = living.getY();
        double z = living.getZ();
        for (int attempt = 0; attempt < ATTEMPTS; attempt++) {
            double tx = x + Mth.nextDouble(random, -range, range);
            double ty = Mth.clamp(
                    y + (random.nextInt((int) range * 2 + 1) - range),
                    living.level().getMinBuildHeight(),
                    living.level().getMaxBuildHeight());
            double tz = z + Mth.nextDouble(random, -range, range);
            if (living.randomTeleport(tx, ty, tz, true)) {
                ctx.cast()
                        .level()
                        .playSound(null, x, y, z, SoundEvents.CHORUS_FRUIT_TELEPORT, SoundSource.PLAYERS, 0.8F, 1.0F);
                return;
            }
        }
    }
}
