package com.leclowndu93150.thaumaturge.content.spell.engine;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.recipe.ResearchGate;
import com.leclowndu93150.thaumaturge.api.research.pool.AspectPoolAccess;
import com.leclowndu93150.thaumaturge.api.spell.FocusTier;
import com.leclowndu93150.thaumaturge.api.spell.Spell;
import com.leclowndu93150.thaumaturge.api.spell.SpellNode;
import com.leclowndu93150.thaumaturge.api.spell.SpellProblem;
import com.leclowndu93150.thaumaturge.api.spell.SpellSummary;
import com.leclowndu93150.thaumaturge.api.spell.Spells;
import com.leclowndu93150.thaumaturge.api.spell.affinity.AspectAffinity;
import com.leclowndu93150.thaumaturge.api.spell.part.SettingSpec;
import com.leclowndu93150.thaumaturge.api.spell.part.SpellPart;
import com.leclowndu93150.thaumaturge.api.spell.part.SpellPartKind;
import com.leclowndu93150.thaumaturge.content.spell.SpellText;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.Nullable;

public final class SpellAnalyzer {
    public static final float VIS_PER_COMPLEXITY = 0.2F;
    private static final float COOLDOWN_BASE = 5.0F;
    private static final float COOLDOWN_EXPONENT = 1.5F;
    private static final float COOLDOWN_DIVISOR = 6.0F;
    private static final int CHARGE_BASE = 10;
    private static final int CHARGE_DIVISOR = 2;
    private static final int PULSE_BASE = 4;
    private static final int PULSE_DIVISOR = 8;
    private static final int PULSE_MAX = 12;

    private final HolderLookup.Provider registries;
    private final @Nullable Player player;
    private final List<SpellProblem> problems = new ArrayList<>();
    private final Map<ResourceKey<SpellPart>, Integer> occurrences = new HashMap<>();
    private final Map<ResourceKey<IAspect>, Integer> crystals = new LinkedHashMap<>();
    private float complexity;
    private float extraVis;
    private float visMultiplier = 1.0F;
    private float cooldownMultiplier = 1.0F;
    private int branches;
    private int repeats;
    private boolean hasEffect;

    private SpellAnalyzer(HolderLookup.Provider registries, @Nullable Player player) {
        this.registries = registries;
        this.player = player;
    }

    public static SpellSummary analyze(Spell spell, @Nullable FocusTier tier, HolderLookup.Provider registries, @Nullable Player player) {
        SpellAnalyzer analyzer = new SpellAnalyzer(registries, player);
        return analyzer.run(spell, tier);
    }

    private SpellSummary run(Spell spell, @Nullable FocusTier tier) {
        SpellNode root = spell.root();
        if (!root.part().equals(Spell.ORIGIN)) {
            problems.add(SpellProblem.at(root, SpellText.problem("bad_root"), true));
        }
        if (spell.isEmpty()) {
            problems.add(SpellProblem.of(SpellText.problem("empty"), true));
        }
        for (SpellNode child : root.children()) {
            visit(child);
        }
        int total = Math.round(complexity);
        int depth = root.depth() - 1;
        if (!spell.isEmpty() && !hasEffect) {
            problems.add(SpellProblem.of(SpellText.problem("no_effect"), false));
        }
        int budget = 0;
        if (tier != null) {
            budget = tier.complexity();
            check(total > tier.complexity(), "too_complex", total, tier.complexity());
            check(depth > tier.depth(), "too_deep", depth, tier.depth());
            check(branches > tier.branches(), "too_many_branches", branches, tier.branches());
            check(repeats > tier.repeats(), "too_many_repeats", repeats, tier.repeats());
        }
        float vis = (total * VIS_PER_COMPLEXITY + extraVis) * visMultiplier;
        int xp = Math.max(1, Math.round(Mth.sqrt(total)));
        int cooldown = Math.round((COOLDOWN_BASE + (float) Math.pow(total, COOLDOWN_EXPONENT) / COOLDOWN_DIVISOR) * cooldownMultiplier);
        int charge = CHARGE_BASE + total / CHARGE_DIVISOR;
        int pulse = Mth.clamp(PULSE_BASE + total / PULSE_DIVISOR, PULSE_BASE, PULSE_MAX);
        problems.sort(Comparator.comparing(problem -> !problem.fatal()));
        return new SpellSummary(total, budget, vis, xp, crystals, depth, branches, repeats, cooldown, charge, pulse, problems);
    }

    private void visit(SpellNode node) {
        Optional<SpellPart> found = Spells.part(registries, node.part());
        if (found.isEmpty()) {
            problems.add(SpellProblem.at(node, SpellText.problem("unknown_part", node.part().identifier().toString()), true));
            return;
        }
        SpellPart part = found.get();
        if (node.part().equals(Spell.ORIGIN)) {
            problems.add(SpellProblem.at(node, SpellText.problem("misplaced_origin"), true));
        } else if (part.hidden()) {
            problems.add(SpellProblem.at(node, SpellText.problem("hidden_part", SpellText.partName(node.part())), true));
        }
        if (part.kind() == SpellPartKind.EFFECT) {
            hasEffect = true;
        }
        int count = occurrences.merge(node.part(), 1, Integer::sum);
        if (part.maxPerSpell() > 0 && count == part.maxPerSpell() + 1) {
            problems.add(SpellProblem.at(node, SpellText.problem("repeated", SpellText.partName(node.part()), part.maxPerSpell()), true));
        }
        if (player != null && part.research().isPresent() && !ResearchGate.passes(player, part.research().get())) {
            problems.add(SpellProblem.at(node, SpellText.problem("part_locked", SpellText.partName(node.part()), SpellText.researchTitle(part.research().get().entry())), true));
        }
        checkAspect(node, part);
        complexity += nodeComplexity(node, part, registries) * (count + 1) / 2.0F;
        extraVis += nodeVis(node, part, registries);
        visMultiplier *= part.visMultiplier();
        cooldownMultiplier *= part.cooldownMultiplier();
        int children = node.children().size();
        if (children > part.behavior().maxChildren()) {
            problems.add(SpellProblem.at(node, SpellText.problem("too_many_children", SpellText.partName(node.part()), part.behavior().maxChildren()), true));
        }
        if (part.behavior().maxChildren() > 1) {
            branches++;
            if (children < part.behavior().minChildren()) {
                problems.add(SpellProblem.at(node, SpellText.problem("empty_branch"), true));
            }
        }
        repeats += part.behavior().repeats(key -> part.setting(key).map(spec -> spec.clamp(node.settings().getOrDefault(key, spec.defaultValue()))).orElse(0));
        if (children == 0 && !part.behavior().standalone()) {
            problems.add(SpellProblem.at(node, SpellText.problem("leads_nowhere", SpellText.partName(node.part())), false));
        }
        for (SpellNode child : node.children()) {
            visit(child);
        }
    }

    private void checkAspect(SpellNode node, SpellPart part) {
        Optional<ResourceKey<IAspect>> resolved = part.aspect().resolve(node.aspect(), registries);
        if (resolved.isEmpty()) {
            return;
        }
        ResourceKey<IAspect> aspect = resolved.get();
        crystals.merge(aspect, 1, Integer::sum);
        if (node.aspect().isPresent() && part.aspect().selectable() && !node.aspect().get().equals(aspect)) {
            problems.add(SpellProblem.at(node, SpellText.problem("aspect_not_allowed", SpellText.partName(node.part()), SpellText.aspectName(node.aspect().get())), true));
        }
        if (!part.aspect().selectable()) {
            return;
        }
        Optional<AspectAffinity> affinity = Spells.affinity(registries, aspect);
        if (player != null) {
            Optional<Holder.Reference<IAspect>> holder = registries.lookup(IAspect.REGISTRY_KEY).flatMap(lookup -> lookup.get(aspect));
            if (holder.isPresent() && !AspectPoolAccess.isDiscovered(player, holder.get())) {
                problems.add(SpellProblem.at(node, SpellText.problem("aspect_unknown", SpellText.aspectName(aspect)), true));
            }
            if (affinity.isPresent() && affinity.get().research().isPresent() && !ResearchGate.passes(player, affinity.get().research().get())) {
                problems.add(SpellProblem.at(node, SpellText.problem("aspect_locked", SpellText.aspectName(aspect), SpellText.researchTitle(affinity.get().research().get().entry())), true));
            }
        }
    }

    public static float nodeComplexity(SpellNode node, SpellPart part, HolderLookup.Provider registries) {
        float total = part.complexity();
        for (SettingSpec spec : part.settings()) {
            total += spec.steps(node.settings().getOrDefault(spec.key(), spec.defaultValue())) * spec.complexity();
        }
        return total + chosenAffinity(node, part, registries).map(AspectAffinity::complexity).orElse(0);
    }

    public static float nodeVis(SpellNode node, SpellPart part, HolderLookup.Provider registries) {
        float total = part.vis();
        for (SettingSpec spec : part.settings()) {
            total += spec.steps(node.settings().getOrDefault(spec.key(), spec.defaultValue())) * spec.vis();
        }
        return total + chosenAffinity(node, part, registries).map(AspectAffinity::vis).orElse(0.0F);
    }

    private static Optional<AspectAffinity> chosenAffinity(SpellNode node, SpellPart part, HolderLookup.Provider registries) {
        if (!part.aspect().selectable()) {
            return Optional.empty();
        }
        return part.aspect().resolve(node.aspect(), registries).flatMap(aspect -> Spells.affinity(registries, aspect));
    }

    private void check(boolean failed, String key, int value, int limit) {
        if (failed) {
            problems.add(SpellProblem.of(SpellText.problem(key, value, limit), true));
        }
    }
}
