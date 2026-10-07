package com.leclowndu93150.thaumaturge.content.spell.modifier;

import com.leclowndu93150.thaumaturge.api.spell.behavior.SpellBehavior;
import com.leclowndu93150.thaumaturge.api.spell.behavior.SpellBehaviorType;
import com.leclowndu93150.thaumaturge.api.spell.cast.CastContext;
import com.leclowndu93150.thaumaturge.api.spell.cast.SpellState;
import com.leclowndu93150.thaumaturge.api.spell.cast.SpellStats;
import com.leclowndu93150.thaumaturge.api.spell.cast.SpellTarget;
import com.leclowndu93150.thaumaturge.registry.TTSpellBehaviors;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.function.ToIntFunction;

public record EchoModifier(int interval, float falloff) implements SpellBehavior {
    public static final MapCodec<EchoModifier> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(Codec.intRange(1, 200).optionalFieldOf("interval", 10).forGetter(EchoModifier::interval),
            Codec.floatRange(0.0F, 1.0F).optionalFieldOf("falloff", 0.6F).forGetter(EchoModifier::falloff)).apply(i, EchoModifier::new));

    private static final String REPEATS = "repeats";

    @Override
    public SpellBehaviorType<?> type() {
        return TTSpellBehaviors.ECHO.get();
    }

    @Override
    public void execute(CastContext ctx, List<SpellTarget> incoming) {
        ctx.proceed(incoming);
        SpellState echo = ctx.state();
        int repeats = ctx.setting(REPEATS);
        for (int repeat = 1; repeat <= repeats; repeat++) {
            echo = echo.multiply(SpellStats.POWER, falloff);
            ctx.proceedLater(interval * repeat, incoming, echo);
        }
    }

    @Override
    public int repeats(ToIntFunction<String> settings) {
        return settings.applyAsInt(REPEATS);
    }

    @Override
    public boolean standalone() {
        return false;
    }
}
