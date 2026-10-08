package com.leclowndu93150.thaumaturge.content.spell.casting;

import com.leclowndu93150.thaumaturge.api.spell.FocusTier;
import com.leclowndu93150.thaumaturge.api.spell.Spell;
import com.leclowndu93150.thaumaturge.api.spell.SpellSummary;
import java.util.Objects;
import java.util.function.Supplier;
import org.jspecify.annotations.Nullable;

public final class ChannelSummaryCache {
    private @Nullable Spell spell;
    private @Nullable FocusTier tier;
    private @Nullable SpellSummary summary;

    public SpellSummary summaryFor(Spell spell, @Nullable FocusTier tier, Supplier<SpellSummary> analyze) {
        if (summary == null || !spell.equals(this.spell) || !Objects.equals(tier, this.tier)) {
            this.spell = spell;
            this.tier = tier;
            this.summary = analyze.get();
        }
        return summary;
    }
}
