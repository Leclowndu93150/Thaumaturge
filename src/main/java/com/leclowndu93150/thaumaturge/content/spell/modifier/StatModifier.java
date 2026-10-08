package com.leclowndu93150.thaumaturge.content.spell.modifier;

import com.leclowndu93150.thaumaturge.api.spell.behavior.AbstractModifierBehavior;
import com.leclowndu93150.thaumaturge.api.spell.behavior.SpellBehaviorType;
import com.leclowndu93150.thaumaturge.api.spell.cast.CastContext;
import com.leclowndu93150.thaumaturge.api.spell.cast.SpellState;
import com.leclowndu93150.thaumaturge.registry.TTSpellBehaviors;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;

public final class StatModifier extends AbstractModifierBehavior {
    public static final MapCodec<StatModifier> CODEC = RecordCodecBuilder.mapCodec(
            i -> i.group(StatChange.CODEC.listOf().fieldOf("changes").forGetter(StatModifier::changes))
                    .apply(i, StatModifier::new));

    private final List<StatChange> changes;

    public StatModifier(List<StatChange> changes) {
        this.changes = List.copyOf(changes);
    }

    public List<StatChange> changes() {
        return changes;
    }

    @Override
    public SpellBehaviorType<?> type() {
        return TTSpellBehaviors.STAT.get();
    }

    @Override
    protected SpellState modify(CastContext ctx, SpellState state) {
        SpellState out = state;
        for (StatChange change : changes) {
            out = change.applyTo(ctx, out);
        }
        return out;
    }
}
