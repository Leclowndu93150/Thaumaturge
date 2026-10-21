package com.leclowndu93150.thaumaturge.content.infusion;

import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.capability.IPlayerKnowledge;
import com.leclowndu93150.thaumaturge.api.capability.KnowledgeAccess;
import com.leclowndu93150.thaumaturge.api.casters.IInteractWithCaster;
import com.leclowndu93150.thaumaturge.api.infusion.InfusionCraftedEvent;
import com.leclowndu93150.thaumaturge.api.items.IGogglesReadout;
import com.leclowndu93150.thaumaturge.content.aspect.ReadOnlyAspectContainer;
import com.leclowndu93150.thaumaturge.content.blockentity.AbstractSyncedBlockEntity;
import com.leclowndu93150.thaumaturge.content.effect.Effects;
import com.leclowndu93150.thaumaturge.content.research.ResearchProgressionEvents;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import org.jspecify.annotations.Nullable;

public final class BlockEntityInfusionMatrix extends AbstractSyncedBlockEntity implements IGogglesReadout, IInteractWithCaster, ReadOnlyAspectContainer {
    public static final float STABILITY_CAP = 25.0F;

    private static final String ACTIVE_KEY = "Active";
    private static final String STABILITY_KEY = "Stability";
    private static final String REPLENISH_KEY = "Replenish";
    private static final String JOB_KEY = "Job";
    private static final String NUMBER_PATTERN = "#######.##";
    private static final String KEY_GAIN = "gui.thaumaturge.infusion.stability.gain_amount";
    private static final String KEY_LOSS = "gui.thaumaturge.infusion.stability.loss_range";
    private static final float VERY_STABLE_THRESHOLD = STABILITY_CAP / 2.0F;
    private static final float UNSTABLE_THRESHOLD = -25.0F;
    private static final float STABILITY_FLOOR = -100.0F;
    private static final Vec3 READOUT_ANCHOR = new Vec3(0.0, 1.5, 0.0);

    private static final int ALTAR_DEPTH = 2;
    private static final int STALE_INTERVAL = 100;
    private static final int CRAFTING_CHECK_INTERVAL = 20;
    private static final int IDLE_CHECK_INTERVAL = 100;
    private static final int IDLE_REGEN_MIN_INTERVAL = 5;
    private static final float IDLE_REGEN_MIN_GAIN = 0.1F;
    private static final int RUNE_INTERVAL = 5;
    private static final int LOOP_SOUND_INTERVAL = 65;
    private static final float RUNE_RED_BASE = 0.5F;
    private static final float RUNE_RED_SPREAD = 0.2F;
    private static final float RUNE_GREEN = 0.1F;
    private static final float RUNE_BLUE_BASE = 0.7F;
    private static final float RUNE_BLUE_SPREAD = 0.3F;
    private static final int RUNE_LIFETIME = 25;
    private static final float RUNE_GRAVITY = -0.03F;
    private static final float SOUND_VOLUME = 0.5F;
    private static final float SOUND_PITCH = 1.0F;
    private static final float FAIL_VOLUME = 1.0F;
    private static final float FAIL_PITCH = 0.6F;
    private static final float MIN_COST_MULTIPLIER = 0.5F;
    private static final int INSTABILITY_ROLL_BOUND = 1500;
    private static final float EVENT_STABILITY_BASE = 5.0F;
    private static final float EVENT_STABILITY_SPREAD = 5.0F;
    private static final float FAILED_DRAIN_PENALTY = 0.25F;
    private static final int PULL_TICKS = 5;
    private static final int FINISH_GRACE_CYCLES = 3;
    private static final int UNREST_MARKER_INTERVAL = 20;
    private static final double UNREST_MARKER_INSET = 0.25;
    private static final double UNREST_MARKER_SPAN = 0.5;
    private static final float UNREST_CRACKLE_CHANCE = 0.2F;

    private boolean active;
    private final StabilityState stability = new StabilityState();
    private @Nullable InfusionCraftJob job;
    private long craftRevision;
    private boolean clientCrafting;
    private int clientInstability;
    private AspectList clientEssentia = AspectList.EMPTY;
    private int tickCount;
    private boolean stale;
    private @Nullable MatrixEnvironment environment;
    private final EssentiaSources sources;
    private final CraftPipeline pipeline;
    private final Map<BlockPos, Integer> clientSources = new HashMap<>();

    public float clientStartUp;
    public int clientCraftTicks;

    public BlockEntityInfusionMatrix(BlockPos pos, BlockState state) {
        super(TTBlockEntities.INFUSION_MATRIX.get(), pos, state);
        this.sources = new EssentiaSources(pos);
        this.pipeline = new CraftPipeline(List.of(new EssentiaStage(sources), new IngredientStage(), new FinishStage()));
    }

    public boolean isActive() {
        return active;
    }

    public boolean isCrafting() {
        return level != null && level.isClientSide() ? clientCrafting : job != null;
    }

    public float stability() {
        return stability.value();
    }

    public AspectList remainingEssentia() {
        return level != null && level.isClientSide() ? clientEssentia : job == null ? AspectList.EMPTY : job.essentia();
    }

    @Override
    public AspectList getAspects() {
        return remainingEssentia();
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BlockEntityInfusionMatrix matrix) {
        if (level instanceof ServerLevel server) {
            matrix.tickServer(server);
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, BlockEntityInfusionMatrix matrix) {
        matrix.tickClient(level);
    }

    @Override
    public boolean onCasterRightClick(Level level, ItemStack casterStack, Player player, BlockPos pos, Direction side, InteractionHand hand) {
        if (level instanceof ServerLevel server) {
            onRightClick(server, player);
        }
        return true;
    }

    public void onRightClick(ServerLevel level, Player player) {
        if (active) {
            if (job == null) {
                tryStartCraft(level, player);
            }
            return;
        }
        if (MatrixEnvironment.validLocation(level, worldPosition)) {
            playSound(level, TTSounds.CRAFTSTART.get(), SOUND_VOLUME, SOUND_PITCH);
            active = true;
            setChangedAndSync();
        }
    }

    public boolean tryStartCraft(ServerLevel level, Player player) {
        return tryStartCraft(level, player, 0);
    }

    public boolean tryStartCraft(ServerLevel level, Player player, int extraInstability) {
        if (player.level() != level) {
            return false;
        }
        return tryStartCraft(level, player.getUUID(), KnowledgeAccess.of(player), null, extraInstability);
    }

    boolean tryStartCraft(ServerLevel level, UUID owner, IPlayerKnowledge knowledge, @Nullable ResourceKey<Recipe<?>> expectedRecipeId, int extraInstability) {
        if (!level.getServer().isSameThread() || !active || job != null || extraInstability < 0 || extraInstability > 100) {
            return false;
        }
        stale = true;
        sources.invalidate();
        if (!MatrixEnvironment.validLocation(level, worldPosition)) {
            active = false;
            setChangedAndSync();
            return false;
        }
        InfusionInput input = stagedInput(level);
        if (input.catalyst().isEmpty() || input.components().isEmpty()) {
            return false;
        }
        InfusionRecipeMatcher.Match match = InfusionRecipeMatcher.find(level, knowledge, input);
        if (match == null || match.locked() || expectedRecipeId != null && !expectedRecipeId.equals(match.holder().id())) {
            return false;
        }
        InfusionJobRecipe recipe = match.recipe();
        List<ItemStack> ingredients = recipe.jobComponents(input);
        if (ingredients == null) {
            return false;
        }
        AspectList quote = projectedCost(level, recipe.jobEssentia(input));
        int instability = recipe.jobInstability(input);
        if (instability < 0 || instability > Integer.MAX_VALUE - extraInstability) {
            return false;
        }
        ItemStack result = recipe.jobResult(input, level.getRandom());
        if (result.isEmpty() || !sameInputs(input, stagedInput(level))) {
            return false;
        }
        job = new InfusionCraftJob(ingredients, quote, quote, result, input.catalyst(), instability + extraInstability, Optional.of(owner),
                recipe instanceof DynamicInfusionAdapter ? Optional.of(new InfusionCraftJob.PreparedRecipe(match.holder().id(), 1, input.components())) : Optional.empty());
        craftRevision++;
        pipeline.reset();
        playSound(level, TTSounds.CRAFTSTART.get(), SOUND_VOLUME, SOUND_PITCH);
        setChangedAndSync();
        return true;
    }

    public long craftRevision() {
        return craftRevision;
    }

    private static boolean sameInputs(InfusionInput expected, InfusionInput current) {
        if (!ItemStack.matches(expected.catalyst(), current.catalyst()) || expected.components().size() != current.components().size()) {
            return false;
        }
        for (int index = 0; index < expected.components().size(); index++) {
            if (!ItemStack.matches(expected.components().get(index), current.components().get(index))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Vec3 readoutAnchor() {
        return READOUT_ANCHOR;
    }

    @Override
    public List<Component> readout() {
        List<Component> lines = new ArrayList<>();
        lines.add(Component.translatable(stability.tier().key).withStyle(ChatFormatting.BOLD));
        lines.add(Component.translatable(KEY_GAIN, formatNumber(stability.gain())).withStyle(ChatFormatting.GOLD, ChatFormatting.ITALIC));
        float maxLoss = level != null && level.isClientSide() ? stability.maxLoss(clientInstability) : job == null ? 0.0F : stability.maxLoss(job.instability());
        if (maxLoss != 0.0F) {
            lines.add(Component.translatable(KEY_LOSS, formatNumber(maxLoss)).withStyle(ChatFormatting.RED, ChatFormatting.ITALIC));
        }
        return lines;
    }

    public void refreshSurroundings() {
        stale = true;
    }

    public void addClientSourceFX(BlockPos pos, int ticks) {
        clientSources.put(pos.immutable(), ticks);
    }

    @Override
    protected void writeClientData(ValueOutput output) {
        output.putBoolean(ACTIVE_KEY, active);
        output.putBoolean("Crafting", job != null);
        output.putInt("Instability", job == null ? 0 : job.instability());
        output.store("Essentia", AspectList.CODEC, remainingEssentia());
        stability.save(output);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putBoolean(ACTIVE_KEY, active);
        output.putLong("CraftRevision", craftRevision);
        stability.save(output);
        if (job != null) {
            output.store(JOB_KEY, InfusionCraftJob.CODEC, job);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        active = input.getBooleanOr(ACTIVE_KEY, false);
        craftRevision = input.getLongOr("CraftRevision", 0);
        clientCrafting = input.getBooleanOr("Crafting", false);
        clientInstability = input.getIntOr("Instability", 0);
        clientEssentia = input.read("Essentia", AspectList.CODEC).orElse(AspectList.EMPTY);
        stability.load(input);
        job = input.read(JOB_KEY, InfusionCraftJob.CODEC).orElse(null);
    }

    InfusionInput stagedInput(ServerLevel level) {
        MatrixEnvironment env = MatrixEnvironment.survey(level, worldPosition);
        BlockEntityPedestal center = centralPedestal(level);
        ItemStack catalyst = ItemStack.EMPTY;
        if (center != null) {
            catalyst = center.getItem().copyWithCount(1);
        }
        List<ItemStack> components = new ArrayList<>(env.pedestals().size());
        for (BlockPos pos : env.pedestals()) {
            if (!(level.getBlockEntity(pos) instanceof BlockEntityPedestal pedestal)) {
                continue;
            }
            ItemStack held = pedestal.getItem();
            if (!held.isEmpty()) {
                components.add(held.copyWithCount(1));
            }
        }
        return new InfusionInput(catalyst, components);
    }

    AspectList projectedCost(ServerLevel level, AspectList base) {
        float multiplier = Math.max(MIN_COST_MULTIPLIER, MatrixEnvironment.survey(level, worldPosition).costMult());
        AspectList scaled = AspectList.EMPTY;
        for (AspectInstance entry : base.entries()) {
            int amount = (int) (entry.amount() * multiplier);
            if (amount <= 0) {
                continue;
            }
            scaled = scaled.add(entry.aspect(), amount);
        }
        return scaled;
    }

    static ItemStack withCatalystWear(ItemStack result, ItemStack catalyst) {
        ItemStack worn = result.copy();
        if (catalyst.isDamageableItem() && catalyst.isDamaged() && worn.isDamageableItem() && !worn.isDamaged()) {
            float fraction = (float) catalyst.getDamageValue() / catalyst.getMaxDamage();
            worn.setDamageValue((int) (worn.getMaxDamage() * fraction));
        }
        return worn;
    }

    private MatrixEnvironment environment(ServerLevel level) {
        MatrixEnvironment current = environment;
        if (current == null || stale) {
            current = MatrixEnvironment.survey(level, worldPosition);
            environment = current;
            stale = false;
            if (stability.adoptGain(current.stabilityReplenish())) {
                setChangedAndSync();
            }
        }
        return current;
    }

    private @Nullable BlockEntityPedestal centralPedestal(ServerLevel level) {
        return level.getBlockEntity(worldPosition.below(ALTAR_DEPTH)) instanceof BlockEntityPedestal pedestal ? pedestal : null;
    }

    private static int halfCycle(MatrixEnvironment env) {
        return Math.max(1, env.cycleTime() / 2);
    }

    private boolean due(int interval) {
        return tickCount % interval == 0;
    }

    private void playSound(ServerLevel level, SoundEvent sound, float volume, float pitch) {
        level.playSound(null, worldPosition, sound, SoundSource.BLOCKS, volume, pitch);
    }

    private static String formatNumber(float value) {
        return new DecimalFormat(NUMBER_PATTERN).format(value);
    }

    private void tickServer(ServerLevel level) {
        tickCount++;
        boolean crafting = job != null;
        if (crafting && due(STALE_INTERVAL)) {
            stale = true;
        }
        MatrixEnvironment env = environment(level);
        if (placementLost(level, crafting)) {
            shutDown();
            return;
        }
        if (!active) {
            return;
        }
        markFaultyStabiliser(level, env);
        int half = halfCycle(env);
        if (!crafting) {
            regenerateIdle(half);
            return;
        }
        ambience(level);
        crackle(level);
        if (due(half)) {
            runCycle(level, env, half);
        }
    }

    private boolean placementLost(ServerLevel level, boolean crafting) {
        if (!active && !crafting) {
            return false;
        }
        int interval = crafting ? CRAFTING_CHECK_INTERVAL : IDLE_CHECK_INTERVAL;
        return due(interval) && !MatrixEnvironment.validLocation(level, worldPosition);
    }

    private void shutDown() {
        active = false;
        job = null;
        craftRevision++;
        pipeline.reset();
        setChangedAndSync();
    }

    private void regenerateIdle(int half) {
        if (!stability.canRegenerate() || !due(Math.max(IDLE_REGEN_MIN_INTERVAL, half))) {
            return;
        }
        stability.regenerate();
        setChangedAndSync();
    }

    private void ambience(ServerLevel level) {
        if (due(RUNE_INTERVAL)) {
            RandomSource random = level.getRandom();
            float red = RUNE_RED_BASE + random.nextFloat() * RUNE_RED_SPREAD;
            float blue = RUNE_BLUE_BASE + random.nextFloat() * RUNE_BLUE_SPREAD;
            Vec3 origin = Vec3.atLowerCornerOf(worldPosition.below(ALTAR_DEPTH));
            Effects.glyphField(level, origin).color(red, RUNE_GREEN, blue).lifetime(RUNE_LIFETIME).drift(RUNE_GRAVITY).send();
        }
        if (due(LOOP_SOUND_INTERVAL)) {
            playSound(level, TTSounds.INFUSER.get(), SOUND_VOLUME, SOUND_PITCH);
        }
    }

    private void markFaultyStabiliser(ServerLevel level, MatrixEnvironment env) {
        List<BlockPos> faulty = env.faultyStabilisers();
        if (!due(UNREST_MARKER_INTERVAL) || faulty.isEmpty()) {
            return;
        }
        RandomSource random = level.getRandom();
        BlockPos marked = faulty.get(random.nextInt(faulty.size()));
        double x = marked.getX() + UNREST_MARKER_INSET + random.nextDouble() * UNREST_MARKER_SPAN;
        double y = marked.getY() + UNREST_MARKER_INSET + random.nextDouble() * UNREST_MARKER_SPAN;
        double z = marked.getZ() + UNREST_MARKER_INSET + random.nextDouble() * UNREST_MARKER_SPAN;
        InfusionFx.unrestSpark(level, new Vec3(x, y, z));
    }

    private void crackle(ServerLevel level) {
        RandomSource random = level.getRandom();
        if (random.nextFloat() >= stability.severity() * UNREST_CRACKLE_CHANCE) {
            return;
        }
        double x = worldPosition.getX() + random.nextDouble();
        double y = worldPosition.getY() + random.nextDouble();
        double z = worldPosition.getZ() + random.nextDouble();
        InfusionFx.unrestSpark(level, new Vec3(x, y, z));
    }

    private void runCycle(ServerLevel level, MatrixEnvironment env, int half) {
        InfusionCraftJob current = job;
        if (current == null) {
            return;
        }
        RandomSource random = level.getRandom();
        stability.drift(random, current.instability(), env.stabilityReplenish());
        BlockEntityPedestal center = centralPedestal(level);
        CycleOutcome outcome = CycleOutcome.decide(center != null && catalystMatches(center, current), stability, random);
        if (outcome.firesEvent()) {
            InstabilityEvents.trigger(level, worldPosition, env.pedestals(), stability.value());
            stability.rebound(random);
        }
        switch (outcome) {
            case CATALYST_LOST -> abortCraft(level);
            case PROCEED -> pipeline.advance(new CycleContext(level, env, half, current, center));
            case EVENT -> {
            }
        }
        setChangedAndSync();
    }

    private static boolean catalystMatches(BlockEntityPedestal center, InfusionCraftJob current) {
        ItemStack held = center.getItem();
        return !held.isEmpty() && ItemStack.isSameItemSameComponents(held, current.catalyst());
    }

    private void abortCraft(ServerLevel level) {
        job = null;
        craftRevision++;
        pipeline.reset();
        playSound(level, TTSounds.CRAFTFAIL.get(), FAIL_VOLUME, FAIL_PITCH);
    }

    private void tickClient(Level level) {
        clientCraftTicks = ClientAnimator.craftTicks(level, worldPosition, clientCraftTicks, isCrafting());
        clientStartUp = ClientAnimator.startUp(clientStartUp, active);
        MatrixPullEffects.tickSources(level, worldPosition, clientSources);
    }

    private enum CycleOutcome {
        PROCEED, EVENT, CATALYST_LOST;

        private static CycleOutcome decide(boolean catalystPresent, StabilityState state, RandomSource random) {
            if (!catalystPresent) {
                return CATALYST_LOST;
            }
            return state.eventRoll(random) ? EVENT : PROCEED;
        }

        private boolean firesEvent() {
            return this != PROCEED;
        }
    }

    private final class CycleContext {
        private final ServerLevel level;
        private final MatrixEnvironment env;
        private final int half;
        private final InfusionCraftJob current;
        private final @Nullable BlockEntityPedestal center;

        private CycleContext(ServerLevel level, MatrixEnvironment env, int half, InfusionCraftJob current, @Nullable BlockEntityPedestal center) {
            this.level = level;
            this.env = env;
            this.half = half;
            this.current = current;
            this.center = center;
        }

        private ServerLevel level() {
            return level;
        }

        private MatrixEnvironment env() {
            return env;
        }

        private int half() {
            return half;
        }

        private InfusionCraftJob job() {
            return current;
        }

        private BlockEntityPedestal center() {
            return center;
        }

        private BlockPos matrixPos() {
            return worldPosition;
        }

        private void penalise() {
            stability.punish();
        }

        private void chargeForAbsentComponent(int position) {
            RandomSource random = level.getRandom();
            if (random.nextInt(position) != 0) {
                return;
            }
            current.addPenaltyEssentia(random);
            stability.punish();
        }

        private void markChanged() {
            setChangedAndSync();
        }

        private void clearJob() {
            job = null;
            craftRevision++;
        }

        private void playSound(SoundEvent sound) {
            BlockEntityInfusionMatrix.this.playSound(level, sound, SOUND_VOLUME, SOUND_PITCH);
        }
    }

    private interface CraftStage {
        boolean complete(InfusionCraftJob job);

        void advance(CycleContext context);

        void reset();
    }

    private static final class CraftPipeline {
        private final List<CraftStage> stages;

        private CraftPipeline(List<CraftStage> stages) {
            this.stages = stages;
        }

        private void reset() {
            for (CraftStage stage : stages) {
                stage.reset();
            }
        }

        private void advance(CycleContext context) {
            for (CraftStage stage : stages) {
                if (!stage.complete(context.job())) {
                    stage.advance(context);
                    return;
                }
            }
        }
    }

    private static final class EssentiaStage implements CraftStage {
        private final EssentiaSources sources;

        private EssentiaStage(EssentiaSources sources) {
            this.sources = sources;
        }

        @Override
        public boolean complete(InfusionCraftJob job) {
            return job.essentia().isEmpty();
        }

        @Override
        public void advance(CycleContext context) {
            InfusionCraftJob current = context.job();
            for (AspectInstance entry : current.essentia().entries()) {
                int extension = entry.amount() > 1 ? context.half() : 0;
                if (sources.drain(context.level(), entry.aspect(), extension)) {
                    current.setEssentia(current.essentia().remove(entry.aspect(), 1));
                    break;
                }
                context.penalise();
            }
        }

        @Override
        public void reset() {}
    }

    private static final class IngredientStage implements CraftStage {
        private int countdown;

        @Override
        public boolean complete(InfusionCraftJob job) {
            return job.ingredients().isEmpty();
        }

        @Override
        public void advance(CycleContext context) {
            List<ItemStack> ingredients = context.job().ingredients();
            for (int index = 0; index < ingredients.size(); index++) {
                BlockPos holder = findHolder(context.level(), context.env().pedestals(), ingredients.get(index));
                if (holder != null) {
                    pull(context, holder, ingredients, index);
                    return;
                }
                context.chargeForAbsentComponent(index + 1);
            }
        }

        @Override
        public void reset() {
            countdown = 0;
        }

        private static @Nullable BlockPos findHolder(ServerLevel level, List<BlockPos> pedestals, ItemStack wanted) {
            for (BlockPos candidate : pedestals) {
                if (level.getBlockEntity(candidate) instanceof BlockEntityPedestal pedestal) {
                    ItemStack held = pedestal.getItem();
                    if (!held.isEmpty() && ItemStack.isSameItemSameComponents(held, wanted)) {
                        return candidate;
                    }
                }
            }
            return null;
        }

        private void pull(CycleContext context, BlockPos holder, List<ItemStack> ingredients, int index) {
            ServerLevel level = context.level();
            if (countdown == 0) {
                countdown = PULL_TICKS;
                InfusionFx.itemStream(level, context.matrixPos(), holder);
                return;
            }
            countdown--;
            if (countdown > 0 || !(level.getBlockEntity(holder) instanceof BlockEntityPedestal pedestal)) {
                return;
            }
            ItemStack held = pedestal.getItem();
            ItemStackTemplate remainder = held.getItem().getCraftingRemainder(held);
            pedestal.setItem(remainder != null ? remainder.create() : ItemStack.EMPTY);
            ingredients.remove(index);
            context.markChanged();
        }
    }

    private static final class FinishStage implements CraftStage {
        private int grace;

        @Override
        public boolean complete(InfusionCraftJob job) {
            return false;
        }

        @Override
        public void advance(CycleContext context) {
            grace++;
            if (grace >= FINISH_GRACE_CYCLES) {
                grace = 0;
                finish(context);
            }
        }

        @Override
        public void reset() {
            grace = 0;
        }

        private static void finish(CycleContext context) {
            ServerLevel level = context.level();
            InfusionCraftJob current = context.job();
            BlockEntityPedestal center = context.center();
            ItemStack catalyst = center.getItem().copy();
            ServerPlayer crafter = current.player().map(id -> level.getServer().getPlayerList().getPlayer(id)).orElse(null);
            InfusionCraftedEvent event = new InfusionCraftedEvent(level, context.matrixPos(), crafter, catalyst, withCatalystWear(current.result(), catalyst));
            NeoForge.EVENT_BUS.post(event);
            ItemStack result = event.getResult();
            center.setItem(result);
            context.clearJob();
            if (crafter != null && !result.isEmpty()) {
                crafter.awardStat(Stats.ITEM_CRAFTED.get(result.getItem()), result.getCount());
                ResearchProgressionEvents.recordCrafted(crafter, result);
            }
            InfusionFx.pedestalBamf(level, context.matrixPos().below(ALTAR_DEPTH));
            context.playSound(TTSounds.WAND.get());
            context.markChanged();
        }
    }

    private static final class StabilityState {
        private float value;
        private float gain;

        private float value() {
            return value;
        }

        private float gain() {
            return gain;
        }

        private StabilityTier tier() {
            return StabilityTier.of(value);
        }

        private float maxLoss(int instability) {
            return (float) instability / tier().divisor;
        }

        private void drift(RandomSource random, int instability, float replenish) {
            float loss = random.nextFloat() * maxLoss(instability);
            value = Mth.clamp(value - loss + replenish, STABILITY_FLOOR, STABILITY_CAP);
        }

        private boolean eventRoll(RandomSource random) {
            return value < 0.0F && random.nextInt(INSTABILITY_ROLL_BOUND) <= Math.abs(value);
        }

        private void rebound(RandomSource random) {
            value += EVENT_STABILITY_BASE + random.nextFloat() * EVENT_STABILITY_SPREAD;
        }

        private void punish() {
            value -= FAILED_DRAIN_PENALTY;
        }

        private float severity() {
            return value >= 0.0F ? 0.0F : Math.min(1.0F, value / STABILITY_FLOOR);
        }

        private boolean canRegenerate() {
            return value < STABILITY_CAP;
        }

        private void regenerate() {
            value = Math.min(STABILITY_CAP, value + Math.max(IDLE_REGEN_MIN_GAIN, gain));
        }

        private boolean adoptGain(float replenish) {
            if (gain == replenish) {
                return false;
            }
            gain = replenish;
            return true;
        }

        private void save(ValueOutput output) {
            output.putFloat(STABILITY_KEY, value);
            output.putFloat(REPLENISH_KEY, gain);
        }

        private void load(ValueInput input) {
            value = input.getFloatOr(STABILITY_KEY, 0.0F);
            gain = input.getFloatOr(REPLENISH_KEY, 0.0F);
        }
    }

    private static final class ClientAnimator {
        private static final float START_UP_MAX = 1.0F;
        private static final float START_UP_RATE = 10.0F;
        private static final float START_UP_MIN_STEP = 0.001F;
        private static final int CRAFT_TICKS_DECAY = 2;
        private static final int CRAFT_TICKS_MAX = 50;

        private ClientAnimator() {}

        private static int craftTicks(Level level, BlockPos pos, int ticks, boolean crafting) {
            if (!crafting) {
                return ticks > 0 ? Mth.clamp(ticks - CRAFT_TICKS_DECAY, 0, CRAFT_TICKS_MAX) : ticks;
            }
            if (ticks == 0) {
                level.playLocalSound(pos, TTSounds.INFUSERSTART.get(), SoundSource.BLOCKS, SOUND_VOLUME, SOUND_PITCH, false);
            }
            return ticks + 1;
        }

        private static float startUp(float current, boolean active) {
            float step = current / START_UP_RATE;
            if (active) {
                return current < START_UP_MAX ? Math.min(START_UP_MAX, current + Math.max(step, START_UP_MIN_STEP)) : current;
            }
            if (current > 0.0F) {
                float eased = current - step;
                return eased < START_UP_MIN_STEP ? 0.0F : eased;
            }
            return current;
        }
    }

    private enum StabilityTier {
        VERY_STABLE("gui.thaumaturge.infusion.stability.very_stable", 5), STABLE("gui.thaumaturge.infusion.stability.stable", 6), UNSTABLE("gui.thaumaturge.infusion.stability.unstable",
                7), VERY_UNSTABLE("gui.thaumaturge.infusion.stability.very_unstable", 8);

        private final String key;
        private final int divisor;

        StabilityTier(String key, int divisor) {
            this.key = key;
            this.divisor = divisor;
        }

        private static StabilityTier of(float stability) {
            if (stability >= 0.0F) {
                return stability > VERY_STABLE_THRESHOLD ? VERY_STABLE : STABLE;
            }
            return stability > UNSTABLE_THRESHOLD ? UNSTABLE : VERY_UNSTABLE;
        }
    }
}
