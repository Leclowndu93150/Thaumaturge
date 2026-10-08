package com.leclowndu93150.thaumaturge.content.spell.engine;

import com.leclowndu93150.thaumaturge.api.spell.FocusTier;
import com.leclowndu93150.thaumaturge.api.spell.Spell;
import com.leclowndu93150.thaumaturge.api.spell.SpellSummary;
import com.leclowndu93150.thaumaturge.api.spell.Spells;
import com.leclowndu93150.thaumaturge.api.spell.cast.SpellContinuation;
import com.leclowndu93150.thaumaturge.api.spell.cast.SpellTarget;
import com.leclowndu93150.thaumaturge.api.spell.event.SpellCastEvent;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import java.util.List;
import net.minecraft.core.HolderLookup;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public final class SpellBindings implements Spells.Bindings {
    @Override
    public @Nullable Spell spellOf(ItemStack focus) {
        if (focus.isEmpty()) {
            return null;
        }
        Spell spell = focus.get(TTDataComponents.SPELL.get());
        return spell != null ? spell : focus.get(TTDataComponents.LEGACY_FOCUS_PACKAGE.get());
    }

    @Override
    public void setSpell(ItemStack focus, @Nullable Spell spell) {
        focus.remove(TTDataComponents.LEGACY_FOCUS_PACKAGE.get());
        if (spell == null) {
            focus.remove(TTDataComponents.SPELL.get());
        } else {
            focus.set(TTDataComponents.SPELL.get(), spell);
        }
    }

    @Override
    public SpellSummary analyze(
            Spell spell, @Nullable FocusTier tier, HolderLookup.Provider registries, @Nullable Player player) {
        return SpellAnalyzer.analyze(spell, tier, registries, player);
    }

    @Override
    public void cast(LivingEntity caster, ItemStack focus, Spell spell, List<SpellTarget> origin, float power) {
        if (!(caster.level() instanceof ServerLevel)) {
            return;
        }
        SpellCastEvent.Pre pre = SpellEngine.firePre(caster, focus, spell, power, 0.0F);
        if (!pre.isCanceled()) {
            SpellEngine.execute(caster, focus, spell, origin, pre.power());
        }
    }

    @Override
    public void resume(ServerLevel level, SpellContinuation continuation, List<SpellTarget> targets) {
        SpellEngine.resume(level, continuation, targets);
    }
}
