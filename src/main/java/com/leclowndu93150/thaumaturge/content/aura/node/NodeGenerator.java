package com.leclowndu93150.thaumaturge.content.aura.node;

import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.api.aura.BiomeAspects;
import com.leclowndu93150.thaumaturge.api.aura.BiomeAuraModifier;
import com.leclowndu93150.thaumaturge.api.nodes.NodeModifier;
import com.leclowndu93150.thaumaturge.api.nodes.NodeType;
import com.leclowndu93150.thaumaturge.config.ThaumaturgeCommonConfig;
import com.leclowndu93150.thaumaturge.registry.TTBiomeTags;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.leclowndu93150.thaumaturge.registry.TTDataMaps;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public final class NodeGenerator {
    public static final int DEFAULT_SPECIAL_RARITY = 18;
    public static final int DEFAULT_BASE_AURA = 100;

    private static final int GUARANTEED_ROLLS = 64;
    private static final int PLACE_FLAGS = 3;
    private static final double PERCENT_TOTAL = 100.0;
    private static final int MODIFIER_ODDS_DIVISOR = 2;
    private static final float TAINTED_STRENGTH_FACTOR = 1.5F;
    private static final int REDUCED_STRENGTH_DIVISOR = 4;
    private static final int STRENGTH_FLOOR = 8;
    private static final int BONUS_ASPECT_ROLLS = 3;
    private static final float WEIGHT_FLOOR = 0.05F;
    private static final double BONUS_NONE_WEIGHT = 0.5;
    private static final int[] FACTORIALS = {1, 1, 2, 6};
    private static final int DESIRE_POINTS = 2;
    private static final int PURITY_POINTS = 2;
    private static final int SURVEY_RADIUS = 5;
    private static final int COMMON_SURVEY_THRESHOLD = 100;
    private static final int STONE_SURVEY_THRESHOLD = 500;
    private static final NodeModifier[] MODIFIERS = NodeModifier.values();
    private static final List<ResourceKey<IAspect>> GLOOM_ASPECTS =
            List.of(TTAspects.MORTUUS, TTAspects.EXANIMIS, TTAspects.PERDITIO, TTAspects.TENEBRAE);
    private static final TypeFlavour NO_FLAVOUR = (into, registry, random) -> {};
    private static final Map<NodeType, TypeFlavour> FLAVOURS = Map.of(
            NodeType.HUNGRY,
            NodeGenerator::desireFlavour,
            NodeType.PURE,
            NodeGenerator::purityFlavour,
            NodeType.DARK,
            NodeGenerator::gloomFlavour);
    private static final List<TerrainRule> SURVEY_RULES = List.of(
            new TerrainRule(
                    state -> state.getFluidState().is(FluidTags.WATER),
                    COMMON_SURVEY_THRESHOLD,
                    List.of(TTAspects.AQUA)),
            new TerrainRule(
                    state -> state.getFluidState().is(FluidTags.LAVA),
                    COMMON_SURVEY_THRESHOLD,
                    List.of(TTAspects.IGNIS, TTAspects.TERRA)),
            new TerrainRule(
                    state -> state.is(Blocks.STONE) || state.is(Blocks.DEEPSLATE),
                    STONE_SURVEY_THRESHOLD,
                    List.of(TTAspects.TERRA)),
            new TerrainRule(state -> state.is(BlockTags.LEAVES), COMMON_SURVEY_THRESHOLD, List.of(TTAspects.HERBA)));
    private static final List<RollStage> PIPELINE = List.of(
            NodeGenerator::chooseType,
            NodeGenerator::chooseModifier,
            NodeGenerator::settleStrength,
            NodeGenerator::seedAspects,
            NodeGenerator::addBonusAspects,
            NodeGenerator::addTypeFlavour,
            NodeGenerator::surveySurroundings,
            NodeGenerator::spendBudget);

    private NodeGenerator() {}

    public static boolean createRandomNodeAt(
            ServerLevelAccessor level,
            BlockPos pos,
            RandomSource random,
            boolean silverwood,
            boolean eerie,
            boolean small,
            int specialRarity,
            int baseAura) {
        NodeData data = rollRandomNodeData(level, pos, random, silverwood, eerie, small, specialRarity, baseAura);
        if (data == null) {
            return false;
        }
        return createNodeAt(level, pos, data.type(), data.modifier().orElse(null), data.aspects());
    }

    /**
     * Places a specifically Tainted natural node while preserving the normal Tainted-Lands
     * aura/aspect roll. The ordinary generator already makes roughly half of non-Pure nodes in
     * Tainted Lands TAINTED; retrying the data roll here keeps those exact 2.25x-strength semantics
     * instead of fabricating a weaker post-hoc type conversion.
     */
    public static boolean createGuaranteedTaintedNodeAt(ServerLevelAccessor level, BlockPos pos, RandomSource random) {
        return createGuaranteedNaturalNodeAt(level, pos, random, NodeType.TAINTED);
    }

    /**
     * Places a specifically Hungry natural node while retaining the ordinary Tainted-Lands
     * aura/aspect roll. This is used for the single hungry-node landmark in each natural Tainted
     * Lands patch; it does not affect the configured chance for all other nodes.
     */
    public static boolean createGuaranteedHungryNodeAt(ServerLevelAccessor level, BlockPos pos, RandomSource random) {
        return createGuaranteedNaturalNodeAt(level, pos, random, NodeType.HUNGRY);
    }

    private static boolean createGuaranteedNaturalNodeAt(
            ServerLevelAccessor level, BlockPos pos, RandomSource random, NodeType requiredType) {
        if (ThaumaturgeCommonConfig.WUSS_MODE.get() || !isTaintedBiome(level, pos)) {
            return false;
        }
        NodeType rolledType = requiredType == NodeType.HUNGRY ? NodeType.NORMAL : requiredType;
        Optional<NodeData> selected = firstRollOfType(
                () -> rollRandomNodeData(
                        level, pos, random, false, false, false, DEFAULT_SPECIAL_RARITY, DEFAULT_BASE_AURA),
                rolledType);
        if (selected.isEmpty()) {
            return false;
        }

        NodeData data = selected.get();
        AspectList aspects = data.aspects();
        if (requiredType == NodeType.HUNGRY) {
            aspects = addTypeFlavor(aspectRegistry(level), aspects, NodeType.HUNGRY, random);
        }
        return createNodeAt(level, pos, requiredType, data.modifier().orElse(null), aspects);
    }

    static Optional<NodeData> firstRollOfType(Supplier<@Nullable NodeData> roll, NodeType requiredType) {
        for (int attempt = 0; attempt < GUARANTEED_ROLLS; attempt++) {
            NodeData candidate = roll.get();
            if (candidate != null && candidate.type() == requiredType) {
                return Optional.of(candidate);
            }
        }
        return Optional.empty();
    }

    static boolean isTaintedBiome(ServerLevelAccessor level, BlockPos pos) {
        return level.getBiome(pos).is(TTBiomeTags.IS_TAINTED);
    }

    public static @Nullable NodeData rollRandomNodeData(
            ServerLevelAccessor level,
            BlockPos pos,
            RandomSource random,
            boolean silverwood,
            boolean eerie,
            boolean small,
            int specialRarity,
            int baseAura) {
        return rollRandomNodeData(level, pos, random, silverwood, eerie, small, specialRarity, baseAura, null);
    }

    private static @Nullable NodeData rollRandomNodeData(
            ServerLevelAccessor level,
            BlockPos pos,
            RandomSource random,
            boolean silverwood,
            boolean eerie,
            boolean small,
            int specialRarity,
            int baseAura,
            @Nullable NodeType requiredType) {
        Holder<Biome> biome = level.getBiome(pos);
        BiomeAuraModifier auraModifier = biome.getData(TTDataMaps.BIOME_AURA_MODIFIER);
        float biomeStrength = scaleBiomeStrength(baseAura, auraModifier == null ? 1.0F : auraModifier.value());
        HolderLookup.RegistryLookup<IAspect> aspectRegistry = aspectRegistry(level);
        Map<Boolean, List<Holder<IAspect>>> split = aspectRegistry
                .listElements()
                .collect(Collectors.partitioningBy(entry -> entry.value().isPrimal(), Collectors.toList()));
        List<Holder<IAspect>> primals = split.get(Boolean.TRUE);
        List<Holder<IAspect>> compounds = split.get(Boolean.FALSE);
        if (primals.isEmpty() || compounds.isEmpty()) {
            return null;
        }
        AspectPools pools =
                new AspectPools(aspectRegistry, primals, compounds, biomeAspectHolders(aspectRegistry, biome));
        RollContext context = new RollContext(
                level, pos, random, biome, biomeStrength, pools, silverwood, eerie, small, Math.max(1, specialRarity));
        Draft draft = new Draft();
        for (RollStage stage : PIPELINE) {
            stage.apply(context, draft);
        }
        if (requiredType != null && draft.type != requiredType) {
            return null;
        }
        AspectList aspects = fromMap(draft.tally);
        return new NodeData(draft.type, Optional.ofNullable(draft.modifier), aspects, aspects);
    }

    private static HolderLookup.RegistryLookup<IAspect> aspectRegistry(ServerLevelAccessor level) {
        return level.registryAccess().lookupOrThrow(IAspect.REGISTRY_KEY);
    }

    private static AspectList addTypeFlavor(
            HolderLookup.RegistryLookup<IAspect> registry, AspectList aspects, NodeType type, RandomSource random) {
        Map<Holder<IAspect>, Integer> tally = new LinkedHashMap<>();
        for (AspectInstance entry : aspects.entries()) {
            tally.put(entry.aspect(), entry.amount());
        }
        FLAVOURS.getOrDefault(type, NO_FLAVOUR).contribute(tally, registry, random);
        return fromMap(tally);
    }

    private static NodeType rollType(RandomSource random, int specialRarity) {
        return rollType(
                random,
                specialRarity,
                ThaumaturgeCommonConfig.DARK_NODE_CHANCE.get(),
                ThaumaturgeCommonConfig.UNSTABLE_NODE_CHANCE.get(),
                ThaumaturgeCommonConfig.PURE_NODE_CHANCE.get(),
                ThaumaturgeCommonConfig.TAINTED_NODE_CHANCE.get(),
                ThaumaturgeCommonConfig.HUNGRY_NODE_CHANCE.get(),
                ThaumaturgeCommonConfig.WUSS_MODE.get());
    }

    static NodeType rollType(
            RandomSource random,
            int specialRarity,
            double darkChance,
            double unstableChance,
            double pureChance,
            double taintedChance,
            double hungryChance,
            boolean wussMode) {
        double scale = (double) DEFAULT_SPECIAL_RARITY / Math.max(1, specialRarity);
        double dark = darkChance * scale;
        double unstable = unstableChance * scale;
        double pure = pureChance * scale;
        double tainted = wussMode ? 0.0 : taintedChance * scale;
        double hungry = hungryChance * scale;
        double roll = random.nextDouble() * Math.max(PERCENT_TOTAL, dark + unstable + pure + tainted + hungry);
        if (roll < dark) {
            return NodeType.DARK;
        }
        roll -= dark;
        if (roll < unstable) {
            return NodeType.UNSTABLE;
        }
        roll -= unstable;
        if (roll < pure) {
            return NodeType.PURE;
        }
        roll -= pure;
        if (roll < tainted) {
            return NodeType.TAINTED;
        }
        roll -= tainted;
        return roll < hungry ? NodeType.HUNGRY : NodeType.NORMAL;
    }

    public static boolean createNodeAt(
            ServerLevelAccessor level,
            BlockPos pos,
            NodeType type,
            @Nullable NodeModifier modifier,
            AspectList aspects) {
        if (level.getBlockEntity(pos) instanceof BlockEntityNode existing) {
            return configureNode(existing, type, modifier, aspects);
        }
        BlockState current = level.getBlockState(pos);
        if (!current.isAir() && !current.canBeReplaced() && !current.is(BlockTags.LEAVES)) {
            return false;
        }
        level.setBlock(pos, TTBlocks.NODE.get().defaultBlockState(), PLACE_FLAGS);
        if (level.getBlockEntity(pos) instanceof BlockEntityNode node) {
            return configureNode(node, type, modifier, aspects);
        }
        return false;
    }

    private static boolean configureNode(
            BlockEntityNode node, NodeType type, @Nullable NodeModifier modifier, AspectList aspects) {
        node.setNodeType(type);
        node.setNodeModifier(modifier);
        node.setAspects(aspects);
        node.setChanged();
        return true;
    }

    private static List<Holder<IAspect>> biomeAspectHolders(
            HolderLookup.RegistryLookup<IAspect> registry, Holder<Biome> biome) {
        BiomeAspects aspects = biome.getData(TTDataMaps.BIOME_ASPECTS);
        if (aspects == null) {
            return List.of();
        }
        List<Holder<IAspect>> holders = new ArrayList<>();
        aspects.aspects().stream().map(registry::get).flatMap(Optional::stream).forEach(holders::add);
        return holders;
    }

    private static void tally(Map<Holder<IAspect>, Integer> target, Holder<IAspect> aspect) {
        target.merge(aspect, 1, Integer::sum);
    }

    private static Holder<IAspect> pickFrom(List<Holder<IAspect>> pool, RandomSource random) {
        return pool.get(random.nextInt(pool.size()));
    }

    private static void tallyPicks(
            Map<Holder<IAspect>, Integer> target, List<Holder<IAspect>> pool, int count, RandomSource random) {
        for (int pick = 0; pick < count; pick++) {
            tally(target, pool.get(random.nextInt(pool.size())));
        }
    }

    private static void put(
            Map<Holder<IAspect>, Integer> target,
            HolderLookup.RegistryLookup<IAspect> registry,
            ResourceKey<IAspect> key,
            int amount) {
        registry.get(key).ifPresent(aspect -> target.merge(aspect, amount, Integer::sum));
    }

    private static BonusDraw drawBonus(RandomSource random, int specialRarity) {
        double none = BONUS_NONE_WEIGHT;
        double compound = BONUS_NONE_WEIGHT / specialRarity;
        double primal = BONUS_NONE_WEIGHT - compound;
        double roll = random.nextDouble();
        BonusDraw last = new BonusDraw(0, 0);
        for (int compounds = 0; compounds <= BONUS_ASPECT_ROLLS; compounds++) {
            for (int primals = 0; primals + compounds <= BONUS_ASPECT_ROLLS; primals++) {
                int empty = BONUS_ASPECT_ROLLS - primals - compounds;
                double arrangements = FACTORIALS[BONUS_ASPECT_ROLLS]
                        / (double) (FACTORIALS[primals] * FACTORIALS[compounds] * FACTORIALS[empty]);
                double weight = arrangements
                        * Math.pow(primal, primals)
                        * Math.pow(compound, compounds)
                        * Math.pow(none, empty);
                last = new BonusDraw(primals, compounds);
                if (roll < weight) {
                    return last;
                }
                roll -= weight;
            }
        }
        return last;
    }

    private static void allocateBudget(Map<Holder<IAspect>, Integer> target, int budget, RandomSource random) {
        List<Holder<IAspect>> aspects = new ArrayList<>(target.keySet());
        int committed = target.values().stream().mapToInt(Integer::intValue).sum();
        int extra = Math.max(0, budget - committed);
        if (aspects.isEmpty() || extra == 0) {
            return;
        }
        double[] weights = new double[aspects.size()];
        double weightSum = 0.0;
        for (int index = 0; index < aspects.size(); index++) {
            weights[index] = random.nextFloat() + WEIGHT_FLOOR;
            weightSum += weights[index];
        }
        int[] shares = new int[aspects.size()];
        double[] remainders = new double[aspects.size()];
        int assigned = 0;
        for (int index = 0; index < aspects.size(); index++) {
            double quota = extra * weights[index] / weightSum;
            shares[index] = (int) quota;
            remainders[index] = quota - shares[index];
            assigned += shares[index];
        }
        List<Integer> byRemainder = new ArrayList<>(aspects.size());
        for (int index = 0; index < aspects.size(); index++) {
            byRemainder.add(index);
        }
        byRemainder.sort(
                Comparator.comparingDouble((Integer index) -> remainders[index]).reversed());
        int leftover = extra - assigned;
        for (int rank = 0; rank < leftover; rank++) {
            shares[byRemainder.get(rank % byRemainder.size())]++;
        }
        for (int index = 0; index < aspects.size(); index++) {
            target.merge(aspects.get(index), shares[index], Integer::sum);
        }
    }

    private static AspectList fromMap(Map<Holder<IAspect>, Integer> map) {
        List<AspectInstance> entries = new ArrayList<>(map.size());
        map.forEach((aspect, amount) -> {
            if (amount > 0) {
                entries.add(new AspectInstance(aspect, amount));
            }
        });
        return AspectList.ofEntries(entries);
    }

    private static void chooseType(RollContext context, Draft draft) {
        if (context.silverwood()) {
            draft.type = NodeType.PURE;
        } else if (context.eerie()) {
            draft.type = NodeType.DARK;
        } else {
            draft.type = rollType(context.random(), context.specialRarity());
        }
    }

    private static void chooseModifier(RollContext context, Draft draft) {
        RandomSource random = context.random();
        if (random.nextInt(Math.max(1, context.specialRarity() / MODIFIER_ODDS_DIVISOR)) == 0) {
            draft.modifier = MODIFIERS[random.nextInt(MODIFIERS.length)];
        }
    }

    private static void settleStrength(RollContext context, Draft draft) {
        RandomSource random = context.random();
        float strength = context.biomeStrength();
        if (draft.type != NodeType.PURE && context.biome().is(TTBiomeTags.IS_TAINTED)) {
            strength *= TAINTED_STRENGTH_FACTOR;
            if (random.nextBoolean() && !ThaumaturgeCommonConfig.WUSS_MODE.get()) {
                draft.type = NodeType.TAINTED;
                strength *= TAINTED_STRENGTH_FACTOR;
            }
        }
        if (context.silverwood() || context.small()) {
            strength /= REDUCED_STRENGTH_DIVISOR;
        }
        draft.budget = chooseStrengthBudget(Math.max(STRENGTH_FLOOR, (int) strength), random);
    }

    static float scaleBiomeStrength(int baseAura, float modifier) {
        return baseAura * modifier;
    }

    static int chooseStrengthBudget(int strength, RandomSource random) {
        int half = strength / 2;
        return half + random.nextInt(Math.max(1, strength - half));
    }

    private static void seedAspects(RollContext context, Draft draft) {
        draft.tally.putAll(seedAspects(
                context.pools().biomeSpecific(),
                context.pools().primals(),
                context.pools().compounds(),
                context.random()));
    }

    static Map<Holder<IAspect>, Integer> seedAspects(
            List<Holder<IAspect>> biomeSpecific,
            List<Holder<IAspect>> primals,
            List<Holder<IAspect>> compounds,
            RandomSource random) {
        Map<Holder<IAspect>, Integer> seeded = new LinkedHashMap<>();
        if (!biomeSpecific.isEmpty()) {
            biomeSpecific.forEach(aspect -> tally(seeded, aspect));
            tally(seeded, pickFrom(biomeSpecific, random));
        } else {
            tally(seeded, pickFrom(primals, random));
            tally(seeded, pickFrom(compounds, random));
        }
        return seeded;
    }

    private static void addBonusAspects(RollContext context, Draft draft) {
        BonusDraw bonus = drawBonus(context.random(), context.specialRarity());
        tallyPicks(draft.tally, context.pools().primals(), bonus.primals(), context.random());
        tallyPicks(draft.tally, context.pools().compounds(), bonus.compounds(), context.random());
    }

    private static void addTypeFlavour(RollContext context, Draft draft) {
        FLAVOURS.getOrDefault(draft.type, NO_FLAVOUR)
                .contribute(draft.tally, context.pools().registry(), context.random());
    }

    private static void desireFlavour(
            Map<Holder<IAspect>, Integer> into, HolderLookup.RegistryLookup<IAspect> registry, RandomSource random) {
        put(into, registry, TTAspects.DESIDERIUM, DESIRE_POINTS);
        if (random.nextBoolean()) {
            put(into, registry, TTAspects.VACUOS, 1);
        }
    }

    private static void purityFlavour(
            Map<Holder<IAspect>, Integer> into, HolderLookup.RegistryLookup<IAspect> registry, RandomSource random) {
        put(into, registry, random.nextBoolean() ? TTAspects.VICTUS : TTAspects.ORDO, PURITY_POINTS);
    }

    private static void gloomFlavour(
            Map<Holder<IAspect>, Integer> into, HolderLookup.RegistryLookup<IAspect> registry, RandomSource random) {
        for (ResourceKey<IAspect> key : GLOOM_ASPECTS) {
            if (random.nextBoolean()) {
                put(into, registry, key, 1);
            }
        }
    }

    private static void surveySurroundings(RollContext context, Draft draft) {
        ServerLevelAccessor level = context.level();
        BlockPos origin = context.pos();
        int[] counts = new int[SURVEY_RULES.size()];
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int dx = -SURVEY_RADIUS; dx <= SURVEY_RADIUS; dx++) {
            for (int dz = -SURVEY_RADIUS; dz <= SURVEY_RADIUS; dz++) {
                int x = origin.getX() + dx;
                int z = origin.getZ() + dz;
                if (!level.hasChunkAt(x, z)) {
                    continue;
                }
                for (int dy = -SURVEY_RADIUS; dy <= SURVEY_RADIUS; dy++) {
                    BlockState state = level.getBlockState(cursor.set(x, origin.getY() + dy, z));
                    classify(state, counts, SURVEY_RULES);
                }
            }
        }
        for (int ruleIndex = 0; ruleIndex < counts.length; ruleIndex++) {
            TerrainRule rule = SURVEY_RULES.get(ruleIndex);
            if (counts[ruleIndex] > rule.threshold()) {
                for (ResourceKey<IAspect> aspect : rule.aspects()) {
                    put(draft.tally, context.pools().registry(), aspect, 1);
                }
            }
        }
    }

    static void classify(BlockState state, int[] counts, List<TerrainRule> rules) {
        for (int index = 0; index < rules.size(); index++) {
            TerrainRule rule = rules.get(index);
            if (rule.matcher().test(state)) {
                counts[index]++;
                return;
            }
        }
    }

    private static void spendBudget(RollContext context, Draft draft) {
        allocateBudget(draft.tally, draft.budget, context.random());
    }

    @FunctionalInterface
    private interface TypeFlavour {
        void contribute(
                Map<Holder<IAspect>, Integer> into, HolderLookup.RegistryLookup<IAspect> registry, RandomSource random);
    }

    @FunctionalInterface
    private interface RollStage {
        void apply(RollContext context, Draft draft);
    }

    private record AspectPools(
            HolderLookup.RegistryLookup<IAspect> registry,
            List<Holder<IAspect>> primals,
            List<Holder<IAspect>> compounds,
            List<Holder<IAspect>> biomeSpecific) {}

    private record RollContext(
            ServerLevelAccessor level,
            BlockPos pos,
            RandomSource random,
            Holder<Biome> biome,
            float biomeStrength,
            AspectPools pools,
            boolean silverwood,
            boolean eerie,
            boolean small,
            int specialRarity) {}

    private record BonusDraw(int primals, int compounds) {}

    record TerrainRule(Predicate<BlockState> matcher, int threshold, List<ResourceKey<IAspect>> aspects) {}

    private static final class Draft {
        private final Map<Holder<IAspect>, Integer> tally = new LinkedHashMap<>();
        private NodeType type = NodeType.NORMAL;
        private @Nullable NodeModifier modifier;
        private int budget;
    }
}
