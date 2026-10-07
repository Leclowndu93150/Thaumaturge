package com.leclowndu93150.thaumaturge.content.spell.script;

import com.leclowndu93150.thaumaturge.api.spell.behavior.AbstractEffectBehavior;
import com.leclowndu93150.thaumaturge.api.spell.behavior.SpellBehaviorType;
import com.leclowndu93150.thaumaturge.api.spell.cast.CastContext;
import com.leclowndu93150.thaumaturge.api.spell.cast.SpellTarget;
import com.leclowndu93150.thaumaturge.api.spell.event.ScriptedSpellEvent;
import com.leclowndu93150.thaumaturge.registry.TTSpellBehaviors;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.neoforged.neoforge.common.NeoForge;

public final class ScriptedEffect extends AbstractEffectBehavior {
    public static final MapCodec<ScriptedEffect> CODEC = RecordCodecBuilder.mapCodec(
            i -> i.group(Codec.STRING.fieldOf("key").forGetter(ScriptedEffect::key), Codec.BOOL.optionalFieldOf("widens", true).forGetter(ScriptedEffect::widensAround)).apply(i, ScriptedEffect::new));

    private final String key;
    private final boolean widens;

    public ScriptedEffect(String key, boolean widens) {
        this.key = key;
        this.widens = widens;
    }

    public String key() {
        return key;
    }

    public boolean widensAround() {
        return widens;
    }

    @Override
    public SpellBehaviorType<?> type() {
        return TTSpellBehaviors.SCRIPTED_EFFECT.get();
    }

    @Override
    protected void apply(CastContext ctx, SpellTarget target, float power, int index) {
        NeoForge.EVENT_BUS.post(new ScriptedSpellEvent(key, ctx, List.of(target), power));
    }

    @Override
    protected boolean widens() {
        return widens;
    }
}
