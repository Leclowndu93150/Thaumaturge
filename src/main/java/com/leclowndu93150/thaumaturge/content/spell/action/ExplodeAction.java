package com.leclowndu93150.thaumaturge.content.spell.action;

import com.leclowndu93150.thaumaturge.api.spell.affinity.ActionContext;
import com.leclowndu93150.thaumaturge.api.spell.affinity.SpellAction;
import com.leclowndu93150.thaumaturge.api.spell.affinity.SpellActionType;
import com.leclowndu93150.thaumaturge.registry.TTSpellActions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public record ExplodeAction(float base, float perPower, float max, boolean breaksBlocks) implements SpellAction {
    public static final MapCodec<ExplodeAction> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                    Codec.FLOAT.optionalFieldOf("base", 1.0F).forGetter(ExplodeAction::base),
                    Codec.FLOAT.optionalFieldOf("per_power", 0.3F).forGetter(ExplodeAction::perPower),
                    Codec.FLOAT.optionalFieldOf("max", 3.0F).forGetter(ExplodeAction::max),
                    Codec.BOOL.optionalFieldOf("breaks_blocks", false).forGetter(ExplodeAction::breaksBlocks))
            .apply(i, ExplodeAction::new));

    @Override
    public SpellActionType<?> type() {
        return TTSpellActions.EXPLODE.get();
    }

    @Override
    public void apply(ActionContext ctx) {
        Vec3 at = ctx.target().position();
        float radius = Math.min(max, ActionTargets.magnitude(base, perPower, ctx));
        ctx.cast()
                .level()
                .explode(
                        ctx.cast().caster(),
                        at.x,
                        at.y,
                        at.z,
                        radius,
                        breaksBlocks ? Level.ExplosionInteraction.MOB : Level.ExplosionInteraction.NONE);
    }
}
