package com.leclowndu93150.thaumaturge.content.spell.casting;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.spell.CastStyle;
import com.leclowndu93150.thaumaturge.api.spell.FocusTier;
import com.leclowndu93150.thaumaturge.api.spell.Spell;
import com.leclowndu93150.thaumaturge.api.spell.SpellProblem;
import com.leclowndu93150.thaumaturge.api.spell.SpellSummary;
import com.leclowndu93150.thaumaturge.api.spell.Spells;
import com.leclowndu93150.thaumaturge.api.spell.cast.SpellTarget;
import com.leclowndu93150.thaumaturge.api.spell.event.SpellCastEvent;
import com.leclowndu93150.thaumaturge.content.casters.CasterManager;
import com.leclowndu93150.thaumaturge.content.misc.TTActionBar;
import com.leclowndu93150.thaumaturge.content.spell.SpellText;
import com.leclowndu93150.thaumaturge.content.spell.engine.SpellEngine;
import com.leclowndu93150.thaumaturge.content.spell.item.FocusItems;
import com.leclowndu93150.thaumaturge.content.wands.WandEconomy;
import com.leclowndu93150.thaumaturge.content.wands.WandVisHelper;
import com.leclowndu93150.thaumaturge.registry.TTAttachments;
import java.util.List;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class SpellCasting {
    private static final float MIN_CHARGE = 0.25F;
    private static final float CHARGE_POWER_BASE = 0.5F;
    private static final float CHARGE_VIS_BASE = 0.5F;
    private static final float TICKS_PER_SECOND = 20.0F;
    private static final float CHANNEL_POWER_BONUS = 1.2F;
    private static final int CHARGED_COOLDOWN_DIVISOR = 2;

    private SpellCasting() {}

    public static InteractionResult use(
            Level level, Player player, InteractionHand hand, ItemStack wand, ItemStack focus) {
        Spell spell = Spells.spellOf(focus);
        if (spell == null || CasterManager.isOnCooldown(player)) {
            return InteractionResult.PASS;
        }
        if (spell.style() != CastStyle.INSTANT) {
            player.startUsingItem(hand);
            return InteractionResult.CONSUME;
        }
        SpellSummary summary = Spells.analyze(spell, Spells.tierOf(focus).orElse(null), level.registryAccess(), player);
        if (!summary.valid() || !affordable(player, wand, summary, 1.0F)) {
            if (player instanceof ServerPlayer serverPlayer) {
                refuse(serverPlayer, summary);
            }
            return InteractionResult.FAIL;
        }
        if (level instanceof ServerLevel && cast(player, wand, focus, spell, summary, 1.0F, 1.0F)) {
            startCooldown(player, wand, summary.cooldown());
            player.swing(hand);
        }
        return InteractionResult.SUCCESS;
    }

    public static boolean channels(ItemStack focus) {
        Spell spell = Spells.spellOf(focus);
        return spell != null && spell.style() != CastStyle.INSTANT;
    }

    public static void tick(ServerLevel level, Player player, ItemStack wand, ItemStack focus, int usedTicks) {
        Spell spell = Spells.spellOf(focus);
        if (spell == null || spell.style() != CastStyle.CHANNELED || usedTicks <= 0) {
            return;
        }
        FocusTier tier = Spells.tierOf(focus).orElse(null);
        SpellSummary summary = player.getData(TTAttachments.CHANNEL_SUMMARY)
                .summaryFor(spell, tier, () -> Spells.analyze(spell, tier, level.registryAccess(), player));
        if (usedTicks % summary.pulseInterval() != 0) {
            return;
        }
        float share = summary.pulseInterval() / TICKS_PER_SECOND;
        if (!cast(player, wand, focus, spell, summary, share * CHANNEL_POWER_BONUS, share)) {
            player.stopUsingItem();
        }
    }

    public static void release(ServerLevel level, Player player, ItemStack wand, ItemStack focus, int usedTicks) {
        Spell spell = Spells.spellOf(focus);
        if (spell == null || spell.style() != CastStyle.CHARGED) {
            return;
        }
        SpellSummary summary = Spells.analyze(spell, Spells.tierOf(focus).orElse(null), level.registryAccess(), player);
        float charge = chargeOf(summary, usedTicks);
        if (charge < MIN_CHARGE) {
            return;
        }
        if (cast(
                player,
                wand,
                focus,
                spell,
                summary,
                CHARGE_POWER_BASE + charge,
                CHARGE_VIS_BASE + (1.0F - CHARGE_VIS_BASE) * charge)) {
            startCooldown(player, wand, summary.cooldown() / CHARGED_COOLDOWN_DIVISOR);
        }
    }

    public static float chargeOf(SpellSummary summary, int usedTicks) {
        return Math.min(1.0F, usedTicks / (float) Math.max(1, summary.chargeTicks()));
    }

    private static boolean cast(
            Player player,
            ItemStack wand,
            ItemStack focus,
            Spell spell,
            SpellSummary summary,
            float power,
            float visShare) {
        if (!summary.valid()) {
            if (player instanceof ServerPlayer serverPlayer) {
                refuse(serverPlayer, summary);
            }
            return false;
        }
        SpellCastEvent.Pre pre = SpellEngine.firePre(player, focus, spell, power, summary.vis() * visShare);
        if (pre.isCanceled()) {
            return false;
        }
        Map<ResourceKey<IAspect>, Integer> split = WandVisHelper.primalSplit(
                Math.round(pre.visCost() * WandEconomy.CENTIVIS_PER_VIS),
                FocusItems.aspects(summary, player.registryAccess()));
        if (!player.getAbilities().instabuild && !WandVisHelper.consumeAllVis(wand, player, split, true, false)) {
            if (player instanceof ServerPlayer serverPlayer) {
                TTActionBar.send(
                        serverPlayer,
                        SpellText.notEnoughVis().copy().withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
            }
            return false;
        }
        SpellEngine.execute(player, focus, spell, List.of(SpellTarget.origin(player)), pre.power());
        return true;
    }

    private static boolean affordable(Player player, ItemStack wand, SpellSummary summary, float visShare) {
        if (player.getAbilities().instabuild) {
            return true;
        }
        Map<ResourceKey<IAspect>, Integer> split = FocusItems.visSplit(summary, visShare, player.registryAccess());
        return WandVisHelper.consumeAllVis(wand, player, split, false, false);
    }

    private static void startCooldown(Player player, ItemStack wand, int ticks) {
        CasterManager.setCooldown(player, ticks);
        player.getCooldowns().addCooldown(wand.getItem(), ticks);
    }

    private static void refuse(ServerPlayer player, SpellSummary summary) {
        for (SpellProblem problem : summary.problems()) {
            if (problem.fatal()) {
                TTActionBar.send(
                        player, problem.message().copy().withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
                return;
            }
        }
        TTActionBar.send(
                player, SpellText.notEnoughVis().copy().withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
    }
}
