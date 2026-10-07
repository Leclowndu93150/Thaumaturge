package com.leclowndu93150.thaumaturge.content.spell.item;

import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.spell.FocusTier;
import com.leclowndu93150.thaumaturge.api.spell.Spell;
import com.leclowndu93150.thaumaturge.api.spell.SpellSummary;
import com.leclowndu93150.thaumaturge.api.spell.Spells;
import com.leclowndu93150.thaumaturge.content.wands.WandEconomy;
import com.leclowndu93150.thaumaturge.content.wands.WandVisHelper;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public final class FocusItems {
    private static final int WHITE = 0xFFFFFF;

    private FocusItems() {}

    public static boolean isFocus(ItemStack stack) {
        return !stack.isEmpty() && Spells.tierOf(stack).isPresent();
    }

    public static @Nullable Spell spell(ItemStack focus) {
        return Spells.spellOf(focus);
    }

    public static Optional<SpellSummary> summary(ItemStack focus, HolderLookup.Provider registries, @Nullable Player player) {
        Spell spell = Spells.spellOf(focus);
        if (spell == null) {
            return Optional.empty();
        }
        return Optional.of(Spells.analyze(spell, Spells.tierOf(focus).orElse(null), registries, player));
    }

    public static int color(ItemStack focus, HolderLookup.Provider registries) {
        Spell spell = Spells.spellOf(focus);
        return spell == null ? WHITE : Spells.color(registries, spell);
    }

    public static @Nullable String sortKey(ItemStack focus) {
        Spell spell = Spells.spellOf(focus);
        return spell == null ? null : focus.getHoverName().getString() + spell.hashCode();
    }

    public static AspectList aspects(SpellSummary summary, HolderLookup.Provider registries) {
        HolderLookup.RegistryLookup<IAspect> lookup = registries.lookupOrThrow(IAspect.REGISTRY_KEY);
        AspectList aspects = AspectList.EMPTY;
        for (Map.Entry<ResourceKey<IAspect>, Integer> entry : summary.crystals().entrySet()) {
            Optional<Holder.Reference<IAspect>> holder = lookup.get(entry.getKey());
            if (holder.isPresent()) {
                aspects = aspects.add(holder.get(), entry.getValue());
            }
        }
        return aspects;
    }

    public static Map<ResourceKey<IAspect>, Integer> visSplit(SpellSummary summary, float fraction, HolderLookup.Provider registries) {
        int centivis = Math.round(summary.vis() * fraction * WandEconomy.CENTIVIS_PER_VIS);
        return WandVisHelper.primalSplit(Math.max(0, centivis), aspects(summary, registries));
    }

    public static Component formatVis(float amount) {
        float rounded = Math.round(amount * 10.0F) / 10.0F;
        if (Mth.equal(rounded, (int) rounded)) {
            return Component.literal(String.valueOf((int) rounded));
        }
        return Component.literal(String.valueOf(rounded));
    }

    public static Optional<FocusTier> tier(ItemStack focus) {
        return Spells.tierOf(focus);
    }
}
