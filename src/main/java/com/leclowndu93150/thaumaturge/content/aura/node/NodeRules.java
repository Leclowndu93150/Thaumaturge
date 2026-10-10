package com.leclowndu93150.thaumaturge.content.aura.node;

import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.nodes.NodeModifier;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import org.jspecify.annotations.Nullable;

final class NodeRules {
    private static final int MAX_DECOMPOSITION_DEPTH = 8;
    private static final int PEARL_PRIMAL_PENALTY = 2;
    private static final int PEARL_PRIMAL_SPREAD = 6;
    private static final int PEARL_PRIMAL_SPREAD_RESEARCHED = 9;
    private static final int PEARL_REPLACEMENT_SPREAD = 3;
    private static final int PEARL_REPLACEMENT_SPREAD_RESEARCHED = 4;
    private static final int PEARL_BRIGHTEN_ODDS = 5;
    private static final int REFILL_AVERAGE = 600;
    private static final int REFILL_BRIGHT = 400;
    private static final int REFILL_PALE = 900;
    private static final int REFILL_FADING = 0;

    private NodeRules() {}

    record Contents(AspectList contained, AspectList base) {
    }

    static int baseRefillInterval(@Nullable NodeModifier modifier) {
        if (modifier == null) {
            return REFILL_AVERAGE;
        }
        return switch (modifier) {
            case BRIGHT -> REFILL_BRIGHT;
            case PALE -> REFILL_PALE;
            case FADING -> REFILL_FADING;
        };
    }

    static @Nullable NodeModifier degrade(@Nullable NodeModifier modifier) {
        if (modifier == null) {
            return NodeModifier.PALE;
        }
        return switch (modifier) {
            case BRIGHT -> null;
            case PALE, FADING -> NodeModifier.FADING;
        };
    }

    static @Nullable NodeModifier improve(@Nullable NodeModifier modifier) {
        if (modifier == null) {
            return NodeModifier.BRIGHT;
        }
        return switch (modifier) {
            case FADING -> NodeModifier.PALE;
            case PALE -> null;
            case BRIGHT -> NodeModifier.BRIGHT;
        };
    }

    static @Nullable NodeModifier pearlModifier(@Nullable NodeModifier modifier, RandomSource random) {
        if (modifier == NodeModifier.FADING) {
            return random.nextBoolean() ? NodeModifier.PALE : modifier;
        }
        if (modifier == NodeModifier.PALE) {
            return random.nextBoolean() ? null : modifier;
        }
        if (modifier == null && random.nextInt(PEARL_BRIGHTEN_ODDS) == 0) {
            return NodeModifier.BRIGHT;
        }
        return modifier;
    }

    static BlockPos scatter(BlockPos origin, int reach, RandomSource random) {
        return origin.offset(leaningOffset(reach, random), leaningOffset(reach, random), leaningOffset(reach, random));
    }

    static int leaningOffset(int reach, RandomSource random) {
        float fraction = random.nextFloat();
        int magnitude = Math.min(reach, (int) (fraction * fraction * (reach + 1)));
        return random.nextBoolean() ? magnitude : -magnitude;
    }

    static AspectList toPrimals(AspectList source) {
        Map<Holder<IAspect>, Integer> totals = new LinkedHashMap<>();
        for (AspectInstance entry : source.entries()) {
            expand(entry.aspect(), entry.amount(), 0, totals);
        }
        return fromMap(totals);
    }

    static Contents pearl(Contents source, List<Holder<IAspect>> primals, RandomSource random, boolean researched) {
        Map<Holder<IAspect>, Integer> base = toMap(source.base());
        Map<Holder<IAspect>, Integer> contained = toMap(source.contained());
        int primalSpread = researched ? PEARL_PRIMAL_SPREAD_RESEARCHED : PEARL_PRIMAL_SPREAD;
        for (Map.Entry<Holder<IAspect>, Integer> entry : new ArrayList<>(base.entrySet())) {
            int previous = entry.getValue();
            int next = entry.getKey().value().isPrimal() ? Math.max(0, previous - PEARL_PRIMAL_PENALTY + random.nextInt(primalSpread)) : random.nextBoolean() ? Math.max(0, previous - 1) : previous;
            base.put(entry.getKey(), next);
            if (next < previous) {
                contained.computeIfPresent(entry.getKey(), (aspect, amount) -> Math.min(amount, next));
            }
        }
        int replacementSpread = researched ? PEARL_REPLACEMENT_SPREAD_RESEARCHED : PEARL_REPLACEMENT_SPREAD;
        for (Holder<IAspect> primal : primals) {
            int replacement = random.nextInt(replacementSpread);
            if (replacement > base.getOrDefault(primal, 0)) {
                base.put(primal, replacement);
                if (contained.getOrDefault(primal, 0) < replacement) {
                    contained.merge(primal, 1, Integer::sum);
                }
            }
        }
        return new Contents(fromMap(contained), fromMap(base));
    }

    private static void expand(Holder<IAspect> aspect, int amount, int depth, Map<Holder<IAspect>, Integer> totals) {
        if (aspect.value().isPrimal() || depth >= MAX_DECOMPOSITION_DEPTH) {
            totals.merge(aspect, amount, Integer::sum);
            return;
        }
        for (Holder<IAspect> component : aspect.value().components()) {
            expand(component, amount, depth + 1, totals);
        }
    }

    private static Map<Holder<IAspect>, Integer> toMap(AspectList list) {
        Map<Holder<IAspect>, Integer> map = new LinkedHashMap<>();
        for (AspectInstance entry : list.entries()) {
            map.put(entry.aspect(), entry.amount());
        }
        return map;
    }

    static AspectList fromMap(Map<Holder<IAspect>, Integer> map) {
        List<AspectInstance> entries = new ArrayList<>(map.size());
        for (Map.Entry<Holder<IAspect>, Integer> entry : map.entrySet()) {
            if (entry.getValue() > 0) {
                entries.add(new AspectInstance(entry.getKey(), entry.getValue()));
            }
        }
        return AspectList.ofEntries(entries);
    }
}
