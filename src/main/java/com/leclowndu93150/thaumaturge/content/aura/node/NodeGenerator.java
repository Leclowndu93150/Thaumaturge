package com.leclowndu93150.thaumaturge.content.aura.node;

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
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.biome.Biome;
import org.jspecify.annotations.Nullable;

public final class NodeGenerator {
    public static final int DEFAULT_SPECIAL_RARITY = 18;
    public static final int DEFAULT_BASE_AURA = 100;

    private static final int GUARANTEED_ROLLS = 64;
    private static final double PERCENT_TOTAL = 100.0;
    private static final int MODIFIER_ODDS_DIVISOR = 2;
    private static final float BLIGHTED_STRENGTH_FACTOR = 1.5F;
    private static final float REDUCED_STRENGTH_DIVISOR = 4.0F;
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
    private static final List<ResourceKey<IAspect>> GLOOM_ASPECTS = List.of(TTAspects.MORTUUS, TTAspects.EXANIMIS, TTAspects.PERDITIO, TTAspects.TENEBRAE);
    private static final TypeFlavour NO_FLAVOUR = (into, registry, random) -> {
    };
    private static final Map<NodeType, TypeFlavour> FLAVOURS = Map.of(NodeType.HUNGRY, NodeGenerator::desireFlavour, NodeType.PURE, NodeGenerator::purityFlavour, NodeType.DARK,
            NodeGenerator::gloomFlavour);
    private static final List<TerrainRule> SURVEY_RULES = List.of(new TerrainRule(state -> state.getFluidState().is(FluidTags.WATER), COMMON_SURVEY_THRESHOLD, List.of(TTAspects.AQUA)),
            new TerrainRule(state -> state.getFluidState().is(FluidTags.LAVA), COMMON_SURVEY_THRESHOLD, List.of(TTAspects.IGNIS, TTAspects.TERRA)),
            new TerrainRule(state -> state.is(Blocks.STONE) || state.is(Blocks.DEEPSLATE), STONE_SURVEY_THRESHOLD, List.of(TTAspects.TERRA)),
            new TerrainRule(state -> state.is(BlockTags.LEAVES), COMMON_SURVEY_THRESHOLD, List.of(TTAspects.HERBA)));
    private static final List<RollStage> PIPELINE = List.of(NodeGenerator::chooseType, NodeGenerator::chooseModifier, NodeGenerator::settleStrength, NodeGenerator::seedAspects,
            NodeGenerator::addBonusAspects, NodeGenerator::addTypeFlavour, NodeGenerator::surveySurroundings, NodeGenerator::spendBudget);

    private NodeGenerator() {}

    public static boolean createRandomNodeAt(ServerLevelAccessor level, BlockPos pos, RandomSource random, boolean silverwood, boolean eerie, boolean small, int specialRarity, int baseAura) {
        NodeData data = rollRandomNodeData(level, pos, random, silverwood, eerie, small, specialRarity, baseAura);
        if (data == null) {
            return false;
        }
        boolean placed = createNodeAt(level, pos, data.type(), data.modifier().orElse(null), data.aspects());
        if (placed && data.type() == NodeType.TAINTED && !(level instanceof ServerLevel)) {
            markBootstrap(level, pos);
        }
        return placed;
    }

    public static boolean createGuaranteedTaintedNodeAt(ServerLevelAccessor level, BlockPos pos, RandomSource random) {
        return createGuaranteed(level, pos, random, NodeType.TAINTED);
    }

    public static boolean createGuaranteedHungryNodeAt(ServerLevelAccessor level, BlockPos pos, RandomSource random) {
        return createGuaranteed(level, pos, random, NodeType.HUNGRY);
    }

    private static boolean createGuaranteed(ServerLevelAccessor level, BlockPos pos, RandomSource random, NodeType wanted) {
        if (!guaranteedSpawnAllowed(level, pos)) {
            return false;
        }
        NodeType rollType = wanted == NodeType.HUNGRY ? NodeType.NORMAL : wanted;
        Optional<NodeData> rolled = firstRollOfType(level, pos, random, rollType);
        if (rolled.isEmpty()) {
            return false;
        }
        AspectList aspects = guaranteedAspects(level, rolled.get(), wanted, random);
        if (!createNodeAt(level, pos, wanted, rolled.get().modifier().orElse(null), aspects)) {
            return false;
        }
        markBootstrap(level, pos);
        return true;
    }

    private static boolean guaranteedSpawnAllowed(ServerLevelAccessor level, BlockPos pos) {
        return !ThaumaturgeCommonConfig.WUSS_MODE.get() && level.getBiome(pos).is(TTBiomeTags.IS_TAINTED);
    }

    private static AspectList guaranteedAspects(ServerLevelAccessor level, NodeData rolled, NodeType wanted, RandomSource random) {
        if (wanted != NodeType.HUNGRY) {
            return rolled.aspects();
        }
        Map<Holder<IAspect>, Integer> extra = new LinkedHashMap<>();
        desireFlavour(extra, aspectRegistry(level), random);
        return rolled.aspects().add(NodeRules.fromMap(extra));
    }

    private static Optional<NodeData> firstRollOfType(ServerLevelAccessor level, BlockPos pos, RandomSource random, NodeType required) {
        return Stream.generate(() -> rollRandomNodeData(level, pos, random, false, false, false, DEFAULT_SPECIAL_RARITY, DEFAULT_BASE_AURA)).limit(GUARANTEED_ROLLS)
                .filter(candidate -> candidate != null && candidate.type() == required).findFirst();
    }

    public static boolean createNodeAt(ServerLevelAccessor level, BlockPos pos, NodeType type, @Nullable NodeModifier modifier, AspectList aspects) {
        BlockEntityNode target = level.getBlockEntity(pos) instanceof BlockEntityNode existing ? existing : placeNodeBlock(level, pos);
        if (target == null) {
            return false;
        }
        configure(target, type, modifier, aspects);
        return true;
    }

    private static @Nullable BlockEntityNode placeNodeBlock(ServerLevelAccessor level, BlockPos pos) {
        BlockState current = level.getBlockState(pos);
        boolean free = current.isAir() || current.canBeReplaced() || current.is(BlockTags.LEAVES);
        if (!free || !level.setBlock(pos, TTBlocks.NODE.get().defaultBlockState(), Block.UPDATE_ALL)) {
            return null;
        }
        return level.getBlockEntity(pos) instanceof BlockEntityNode placed ? placed : null;
    }

    private static void configure(BlockEntityNode node, NodeType type, @Nullable NodeModifier modifier, AspectList aspects) {
        node.applyNodeData(new NodeData(type, Optional.ofNullable(modifier), aspects, aspects));
        node.setChanged();
    }

    private static void markBootstrap(ServerLevelAccessor level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof BlockEntityNode node) {
            node.markNaturalTaintBootstrap();
        }
    }

    private static HolderLookup.RegistryLookup<IAspect> aspectRegistry(ServerLevelAccessor level) {
        return level.registryAccess().lookupOrThrow(IAspect.REGISTRY_KEY);
    }

    public static @Nullable NodeData rollRandomNodeData(ServerLevelAccessor level, BlockPos pos, RandomSource random, boolean silverwood, boolean eerie, boolean small, int specialRarity, int baseAura) {
        HolderLookup.RegistryLookup<IAspect> registry = aspectRegistry(level);
        Map<Boolean, List<Holder<IAspect>>> split = registry.listElements().collect(Collectors.partitioningBy(entry -> entry.value().isPrimal(), Collectors.<Holder<IAspect>>toList()));
        List<Holder<IAspect>> primals = split.get(Boolean.TRUE);
        List<Holder<IAspect>> compounds = split.get(Boolean.FALSE);
        if (primals.isEmpty() || compounds.isEmpty()) {
            return null;
        }
        Holder<Biome> biome = level.getBiome(pos);
        BiomeAuraModifier auraModifier = biome.getData(TTDataMaps.BIOME_AURA_MODIFIER);
        float biomeStrength = baseAura * (auraModifier == null ? 1.0F : auraModifier.value());
        AspectPools pools = new AspectPools(registry, primals, compounds, biomeAspectHolders(registry, biome));
        RollContext context = new RollContext(level, pos, random, biome, biomeStrength, pools, silverwood, eerie, small, specialRarity);
        Draft draft = new Draft();
        for (RollStage stage : PIPELINE) {
            stage.apply(context, draft);
        }
        AspectList list = NodeRules.fromMap(draft.tally);
        return new NodeData(draft.type, Optional.ofNullable(draft.modifier), list, list);
    }

    private static NodeType rollType(RandomSource random, int specialRarity) {
        double scale = (double) DEFAULT_SPECIAL_RARITY / specialRarity;
        double dark = ThaumaturgeCommonConfig.DARK_NODE_CHANCE.get() * scale;
        double unstable = ThaumaturgeCommonConfig.UNSTABLE_NODE_CHANCE.get() * scale;
        double pure = ThaumaturgeCommonConfig.PURE_NODE_CHANCE.get() * scale;
        double tainted = ThaumaturgeCommonConfig.WUSS_MODE.get() ? 0.0 : ThaumaturgeCommonConfig.TAINTED_NODE_CHANCE.get() * scale;
        double hungry = ThaumaturgeCommonConfig.HUNGRY_NODE_CHANCE.get() * scale;
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

    private static BonusDraw drawBonus(RandomSource random, int specialRarity) {
        double none = BONUS_NONE_WEIGHT;
        double compound = BONUS_NONE_WEIGHT / Math.max(1, specialRarity);
        double primal = BONUS_NONE_WEIGHT - compound;
        double roll = random.nextDouble();
        BonusDraw last = new BonusDraw(0, 0);
        for (int compounds = 0; compounds <= BONUS_ASPECT_ROLLS; compounds++) {
            for (int primals = 0; primals + compounds <= BONUS_ASPECT_ROLLS; primals++) {
                int empty = BONUS_ASPECT_ROLLS - primals - compounds;
                double arrangements = FACTORIALS[BONUS_ASPECT_ROLLS] / (FACTORIALS[primals] * FACTORIALS[compounds] * FACTORIALS[empty]);
                double weight = arrangements * Math.pow(primal, primals) * Math.pow(compound, compounds) * Math.pow(none, empty);
                last = new BonusDraw(primals, compounds);
                if (roll < weight) {
                    return last;
                }
                roll -= weight;
            }
        }
        return last;
    }

    private static List<Holder<IAspect>> biomeAspectHolders(HolderLookup.RegistryLookup<IAspect> registry, Holder<Biome> biome) {
        BiomeAspects biomeAspects = biome.getData(TTDataMaps.BIOME_ASPECTS);
        if (biomeAspects == null) {
            return new ArrayList<>();
        }
        List<Holder<IAspect>> holders = new ArrayList<>();
        biomeAspects.aspects().stream().map(key -> registry.get(key)).flatMap(Optional::stream).forEach(holders::add);
        return holders;
    }

    private static Holder<IAspect> pickFrom(List<Holder<IAspect>> pool, RandomSource random) {
        return pool.get(random.nextInt(pool.size()));
    }

    private static void tally(Map<Holder<IAspect>, Integer> target, Holder<IAspect> aspect) {
        target.merge(aspect, 1, Integer::sum);
    }

    private static void tallyPicks(Map<Holder<IAspect>, Integer> target, List<Holder<IAspect>> pool, int count, RandomSource random) {
        for (int pick = 0; pick < count; pick++) {
            tally(target, pickFrom(pool, random));
        }
    }

    private static void put(Map<Holder<IAspect>, Integer> target, HolderLookup.RegistryLookup<IAspect> registry, ResourceKey<IAspect> key, int amount) {
        registry.get(key).ifPresent(aspect -> target.merge(aspect, amount, Integer::sum));
    }

    private static void allocateBudget(Map<Holder<IAspect>, Integer> target, int budget, RandomSource random) {
        int committed = target.values().stream().mapToInt(Integer::intValue).sum();
        int extra = Math.max(0, budget - committed);
        List<Holder<IAspect>> keys = new ArrayList<>(target.keySet());
        int count = keys.size();
        double[] weights = new double[count];
        double weightSum = 0.0;
        for (int slot = 0; slot < count; slot++) {
            weights[slot] = random.nextFloat() + WEIGHT_FLOOR;
            weightSum += weights[slot];
        }
        int[] shares = new int[count];
        double[] remainders = new double[count];
        int assigned = 0;
        for (int slot = 0; slot < count; slot++) {
            double quota = extra * weights[slot] / weightSum;
            shares[slot] = (int) quota;
            remainders[slot] = quota - shares[slot];
            assigned += shares[slot];
        }
        List<Integer> byRemainder = new ArrayList<>(count);
        for (int slot = 0; slot < count; slot++) {
            byRemainder.add(slot);
        }
        byRemainder.sort(Comparator.comparingDouble((Integer slot) -> remainders[slot]).reversed());
        int leftover = extra - assigned;
        for (int rank = 0; rank < leftover; rank++) {
            shares[byRemainder.get(rank % count)]++;
        }
        for (int slot = 0; slot < count; slot++) {
            target.merge(keys.get(slot), shares[slot], Integer::sum);
        }
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
            strength *= BLIGHTED_STRENGTH_FACTOR;
            if (random.nextBoolean() && !ThaumaturgeCommonConfig.WUSS_MODE.get()) {
                draft.type = NodeType.TAINTED;
                strength *= BLIGHTED_STRENGTH_FACTOR;
            }
        }
        if (context.silverwood() || context.small()) {
            strength /= REDUCED_STRENGTH_DIVISOR;
        }
        int whole = Math.max(STRENGTH_FLOOR, (int) strength);
        int half = whole / 2;
        draft.budget = half + random.nextInt(whole - half);
    }

    private static void seedAspects(RollContext context, Draft draft) {
        AspectPools pools = context.pools();
        RandomSource random = context.random();
        if (pools.biomeSpecific().isEmpty()) {
            tally(draft.tally, pickFrom(pools.primals(), random));
            tally(draft.tally, pickFrom(pools.compounds(), random));
            return;
        }
        for (Holder<IAspect> aspect : pools.biomeSpecific()) {
            tally(draft.tally, aspect);
        }
        tally(draft.tally, pickFrom(pools.biomeSpecific(), random));
    }

    private static void addBonusAspects(RollContext context, Draft draft) {
        RandomSource random = context.random();
        BonusDraw bonus = drawBonus(random, context.specialRarity());
        tallyPicks(draft.tally, context.pools().primals(), bonus.primals(), random);
        tallyPicks(draft.tally, context.pools().compounds(), bonus.compounds(), random);
    }

    private static void addTypeFlavour(RollContext context, Draft draft) {
        FLAVOURS.getOrDefault(draft.type, NO_FLAVOUR).contribute(draft.tally, context.pools().registry(), context.random());
    }

    private static void desireFlavour(Map<Holder<IAspect>, Integer> into, HolderLookup.RegistryLookup<IAspect> registry, RandomSource random) {
        put(into, registry, TTAspects.DESIDERIUM, DESIRE_POINTS);
        if (random.nextBoolean()) {
            put(into, registry, TTAspects.VACUOS, 1);
        }
    }

    private static void purityFlavour(Map<Holder<IAspect>, Integer> into, HolderLookup.RegistryLookup<IAspect> registry, RandomSource random) {
        put(into, registry, random.nextBoolean() ? TTAspects.VICTUS : TTAspects.ORDO, PURITY_POINTS);
    }

    private static void gloomFlavour(Map<Holder<IAspect>, Integer> into, HolderLookup.RegistryLookup<IAspect> registry, RandomSource random) {
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
                    classify(level.getBlockState(cursor.set(x, origin.getY() + dy, z)), counts);
                }
            }
        }
        HolderLookup.RegistryLookup<IAspect> registry = context.pools().registry();
        for (int index = 0; index < counts.length; index++) {
            TerrainRule rule = SURVEY_RULES.get(index);
            if (counts[index] > rule.threshold()) {
                for (ResourceKey<IAspect> key : rule.aspects()) {
                    put(draft.tally, registry, key, 1);
                }
            }
        }
    }

    private static void classify(BlockState state, int[] counts) {
        for (int index = 0; index < counts.length; index++) {
            if (SURVEY_RULES.get(index).matcher().test(state)) {
                counts[index]++;
                return;
            }
        }
    }

    private static void spendBudget(RollContext context, Draft draft) {
        allocateBudget(draft.tally, draft.budget, context.random());
    }

    private interface TypeFlavour {
        void contribute(Map<Holder<IAspect>, Integer> into, HolderLookup.RegistryLookup<IAspect> registry, RandomSource random);
    }

    private interface RollStage {
        void apply(RollContext context, Draft draft);
    }

    private record AspectPools(HolderLookup.RegistryLookup<IAspect> registry, List<Holder<IAspect>> primals, List<Holder<IAspect>> compounds, List<Holder<IAspect>> biomeSpecific) {
    }

    private record RollContext(ServerLevelAccessor level, BlockPos pos, RandomSource random, Holder<Biome> biome, float biomeStrength, AspectPools pools, boolean silverwood, boolean eerie,
            boolean small, int specialRarity) {
    }

    private record BonusDraw(int primals, int compounds) {
    }

    private record TerrainRule(Predicate<BlockState> matcher, int threshold, List<ResourceKey<IAspect>> aspects) {
    }

    private static final class Draft {
        private final Map<Holder<IAspect>, Integer> tally = new LinkedHashMap<>();
        private NodeType type = NodeType.NORMAL;
        private @Nullable NodeModifier modifier;
        private int budget;
    }
}
