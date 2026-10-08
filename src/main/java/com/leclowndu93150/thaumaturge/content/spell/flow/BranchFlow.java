package com.leclowndu93150.thaumaturge.content.spell.flow;

import com.leclowndu93150.thaumaturge.api.spell.behavior.SpellBehavior;
import com.leclowndu93150.thaumaturge.api.spell.behavior.SpellBehaviorType;
import com.leclowndu93150.thaumaturge.api.spell.cast.CastContext;
import com.leclowndu93150.thaumaturge.api.spell.cast.SpellTarget;
import com.leclowndu93150.thaumaturge.registry.TTSpellBehaviors;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;

public record BranchFlow(int branches, int required) implements SpellBehavior {
    public static final MapCodec<BranchFlow> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                    Codec.intRange(2, 8).optionalFieldOf("branches", 3).forGetter(BranchFlow::branches),
                    Codec.intRange(1, 8).optionalFieldOf("required", 2).forGetter(BranchFlow::required))
            .apply(i, BranchFlow::new));

    @Override
    public SpellBehaviorType<?> type() {
        return TTSpellBehaviors.BRANCH.get();
    }

    @Override
    public void execute(CastContext ctx, List<SpellTarget> incoming) {
        ctx.proceed(incoming);
    }

    @Override
    public int maxChildren() {
        return branches;
    }

    @Override
    public int minChildren() {
        return required;
    }

    @Override
    public boolean standalone() {
        return false;
    }
}
