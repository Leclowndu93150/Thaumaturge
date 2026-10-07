package com.leclowndu93150.thaumaturge.content.aura.node;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.Thaumaturge;
import com.leclowndu93150.thaumaturge.api.aspect.AspectIndexAccess;
import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.IAspectContainer;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.api.aura.IVisRelaySource;
import com.leclowndu93150.thaumaturge.api.capability.KnowledgeAccess;
import com.leclowndu93150.thaumaturge.api.nodes.NodeModifier;
import com.leclowndu93150.thaumaturge.api.nodes.NodeType;
import com.leclowndu93150.thaumaturge.api.taint.TaintApi;
import com.leclowndu93150.thaumaturge.config.ThaumaturgeCommonConfig;
import com.leclowndu93150.thaumaturge.content.aspect.EntityAspects;
import com.leclowndu93150.thaumaturge.content.effect.Effects;
import com.leclowndu93150.thaumaturge.content.entity.EntityBrainyZombie;
import com.leclowndu93150.thaumaturge.content.taint.TaintHelper;
import com.leclowndu93150.thaumaturge.content.taint.block.BlockTaintFibre;
import com.leclowndu93150.thaumaturge.content.taint.ecology.TaintBiomeManager;
import com.leclowndu93150.thaumaturge.content.wands.EntityAspectOrb;
import com.leclowndu93150.thaumaturge.content.wands.WandChargingEvents;
import com.leclowndu93150.thaumaturge.content.wands.WandEconomy;
import com.leclowndu93150.thaumaturge.content.wands.WandParts;
import com.leclowndu93150.thaumaturge.content.wands.WandVisHelper;
import com.leclowndu93150.thaumaturge.data.worldgen.biome.TTBiomes;
import com.leclowndu93150.thaumaturge.registry.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.SectionPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.commands.FillBiomeCommand;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

public class BlockEntityNode extends BlockEntity implements IAspectContainer {
    private static final int REGEN_INTERVAL_NORMAL = 600;
    private static final int REGEN_INTERVAL_BRIGHT = 400;
    private static final int REGEN_INTERVAL_PALE = 900;
    private static final float FEED_RAW_VIS_PER_POINT = 3.0F;
    private static final int STARVATION_DRIFT_THRESHOLD = 10;
    private static final int DECAY_INTERVAL = 1200;
    private static final int DISCHARGE_RANGE = 4;
    private static final int BEHAVIOR_INTERVAL = 50;
    private static final int STABILITY_INTERVAL = 100;
    private static final float FLUX_TAINT_THRESHOLD = 0.5F;
    private static final int FLUX_TAINT_CHANCE = 20;
    private static final float BRIGHTEN_FLUX_LIMIT = 0.1F;
    private static final float BRIGHTEN_FILL_FRACTION = 0.9F;
    private static final int BRIGHTEN_CHANCE = 50;
    private static final double HUNGRY_PULL_RANGE = 15.0;
    private static final double HUNGRY_ITEM_PULL_MARGIN = 0.5;
    private static final double HUNGRY_EAT_RANGE_SQ = 2.0;
    private static final int DARK_SPAWN_PLAYER_RANGE = 24;
    private static final int DARK_SPAWN_CAP = 3;
    private static final float PURE_FLUX_CLEANSE = 0.25F;
    private static final int PERIODIC_INTERVAL = 200;
    private static final float TAINTED_POLLUTE_SATURATION_SCALE = 0.8F;
    private static final double TAINTED_FLUX_ASPECT_DIVISOR = 3.0;
    private static final float TAINTED_FLUX_PER_STRENGTH = 0.2F;
    private static final float PURE_EROSION_CHANCE = 0.025F;
    private static final float NATURAL_TAINTED_NODE_FLUX = 100.0F;
    private static final int NATURAL_TAINTED_FIBRE_ATTEMPTS = 16;
    private static final int NATURAL_TAINTED_FIBRE_RANGE = 16;
    private static final int NODE_DRAIN_INTERVAL = 5;
    private static final int ORB_BURST_MAX_PER_ASPECT = 10;
    private static final int ORB_BURST_MAX_ORBS_PER_ASPECT = 20;
    private static final float ZAP_WIDTH = 0.3F;
    private static final float ZAP_VOLUME = 0.1F;
    private static final float ZAP_PITCH_VARIATION = 0.2F;
    private static final int LOCK_BASIC = 1;
    private static final int LOCK_ADVANCED = 2;
    private static final int LOCK_BASIC_REGEN_FACTOR = 2;
    private static final int LOCK_ADVANCED_REGEN_FACTOR = 20;
    private static final int UNSTABLE_CURE_ROLL = 10000;
    private static final int UNSTABLE_CURE_MAGIC = 42;
    private static final int FADING_CURE_ROLL = 12500;
    private static final int BIOME_SPREAD_INTERVAL = 50;
    private static final int DARK_BIOME_SPREAD_RANGE = 12;
    private static final int PURE_BIOME_SPREAD_RANGE = 8;
    private static final int TAINTED_BIOME_SPREAD_RANGE = 8;
    private static final int TAINTED_NODE_CONVERSION_INTERVAL = 100;
    private static final int TAINTED_NODE_CONVERSION_CHANCE = 500;
    private static final int FADING_CURE_MAGIC = 69;
    private static final Identifier RESEARCH_NODE_TAPPER_1 = TTIds.rl("node_tapper_1");
    private static final Identifier RESEARCH_NODE_TAPPER_2 = TTIds.rl("node_tapper_2");
    private static final Identifier RESEARCH_NODE_PRESERVE = TTIds.rl("node_preserve");
    private static final int MAX_DECOMPOSE_DEPTH = 8;
    private static final int PEARL_PRIMAL_DROP = 2;
    private static final int PEARL_PRIMAL_SPREAD = 6;
    private static final int PEARL_PRIMAL_SPREAD_RESEARCHED = 9;
    private static final int PEARL_NEW_PRIMAL_MAX = 3;
    private static final int PEARL_NEW_PRIMAL_MAX_RESEARCHED = 4;
    private static final int PEARL_BRIGHT_CHANCE = 5;

    private NodeType nodeType = NodeType.NORMAL;
    private @Nullable NodeModifier nodeModifier;
    protected AspectList aspects = AspectList.EMPTY;
    protected AspectList aspectsBase = AspectList.EMPTY;
    private @Nullable AspectList aspectsBaseOriginal;
    private int count;
    private int regeneration = -1;
    private int wait;
    private int starvation;
    private int lock;
    protected boolean energized;
    private @Nullable UUID drainPlayer;
    private int drainColor = 0xFFFFFF;
    private int drainTicks;
    private int jarringTicks;
    private boolean naturalTaintBootstrapPending;
    public int clientDrainRed = 255;
    public int clientDrainGreen = 255;
    public int clientDrainBlue = 255;

    private static final int DRAIN_LINGER_TICKS = 12;

    public @Nullable UUID getDrainPlayer() {
        return drainPlayer;
    }

    public int getDrainColor() {
        return drainColor;
    }

    public BlockEntityNode(BlockPos pos, BlockState state) {
        this(TTBlockEntities.NODE.get(), pos, state);
    }

    protected BlockEntityNode(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public void applyNodeData(NodeData data) {
        setNodeType(data.type());
        setNodeModifier(data.modifier().orElse(null));
        this.aspects = data.aspects();
        this.aspectsBase = data.aspectsBase();
    }

    public NodeType getNodeType() {
        return nodeType;
    }

    public void setNodeType(NodeType type) {
        this.nodeType = type;
        updateLocationIndex();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        updateLocationIndex();
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        if (level instanceof ServerLevel serverLevel && isIndexedNodeBlock(state)) {
            NodeLocationIndex.get(serverLevel).remove(pos);
        }
    }

    private void updateLocationIndex() {
        if (level instanceof ServerLevel serverLevel && isIndexedNodeBlock(getBlockState())) {
            NodeLocationIndex.get(serverLevel).register(worldPosition, nodeType);
        }
    }

    private static boolean isIndexedNodeBlock(BlockState state) {
        return state.is(TTBlocks.NODE.get()) || state.is(TTBlocks.SILVERWOOD_NODE_LOG.get());
    }

    private static void removeDepletedNode(ServerLevel serverLevel, BlockPos pos) {
        BlockState state = serverLevel.getBlockState(pos);
        if (state.getBlock() instanceof NodeHostBlock host) {
            serverLevel.setBlock(pos, host.depletedState(state), Block.UPDATE_ALL);
        } else {
            serverLevel.removeBlock(pos, false);
        }
    }

    public @Nullable NodeModifier getNodeModifier() {
        return nodeModifier;
    }

    public void setNodeModifier(@Nullable NodeModifier modifier) {
        this.nodeModifier = modifier;
    }

    @Override
    public AspectList getAspects() {
        return aspects;
    }

    @Override
    public void setAspects(AspectList aspects) {
        this.aspects = aspects;
        this.aspectsBase = aspects;
    }

    @Override
    public boolean accepts(Holder<IAspect> aspect) {
        return true;
    }

    public AspectList getAspectsBase() {
        return aspectsBase;
    }

    public int getNodeVisBase(Holder<IAspect> aspect) {
        return aspectsBase.amountOf(aspect);
    }

    @Override
    public int fill(Holder<IAspect> aspect, int amount) {
        int capped = Math.min(amount, Math.max(0, aspectsBase.amountOf(aspect) - aspects.amountOf(aspect)));
        if (capped > 0) {
            aspects = aspects.add(aspect, capped);
            syncContents();
        }
        return amount - capped;
    }

    @Override
    public boolean drain(Holder<IAspect> aspect, int amount) {
        if (aspects.amountOf(aspect) < amount) {
            return false;
        }
        aspects = reduce(aspects, aspect, amount);
        syncContents();
        return true;
    }

    private void syncContents() {
        setChanged();
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    public boolean holds(Holder<IAspect> aspect, int amount) {
        return aspects.amountOf(aspect) >= amount;
    }

    @Override
    public int amountOf(Holder<IAspect> aspect) {
        return aspects.amountOf(aspect);
    }

    private static AspectList reduce(AspectList list, Holder<IAspect> aspect, int amount) {
        List<AspectInstance> entries = new ArrayList<>();
        for (AspectInstance entry : list.entries()) {
            if (entry.aspect().equals(aspect)) {
                int remaining = entry.amount() - amount;
                if (remaining > 0) {
                    entries.add(new AspectInstance(entry.aspect(), remaining));
                }
            } else {
                entries.add(entry);
            }
        }
        return AspectList.ofEntries(entries);
    }

    private static AspectList raiseBase(AspectList list, Holder<IAspect> aspect, int amount) {
        return list.add(aspect, amount);
    }

    public boolean isEnergized() {
        return energized;
    }

    public void setEnergized(boolean energized) {
        if (energized && !this.energized) {
            aspectsBaseOriginal = aspectsBase;
            aspectsBase = decomposeToPrimals(aspectsBase);
            aspects = aspectsBase;
        } else if (!energized && this.energized && aspectsBaseOriginal != null) {
            aspectsBase = aspectsBaseOriginal;
            aspectsBaseOriginal = null;
        }
        this.energized = energized;
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    private static AspectList decomposeToPrimals(AspectList list) {
        AspectList result = AspectList.EMPTY;
        for (AspectInstance entry : list.entries()) {
            result = addPrimals(result, entry.aspect(), entry.amount(), 0);
        }
        return result;
    }

    private static AspectList addPrimals(AspectList into, Holder<IAspect> aspect, int amount, int depth) {
        if (depth >= MAX_DECOMPOSE_DEPTH || aspect.value().isPrimal()) {
            return into.add(aspect, amount);
        }
        AspectList result = into;
        for (Holder<IAspect> component : aspect.value().components()) {
            result = addPrimals(result, component, amount, depth + 1);
        }
        return result;
    }

    public @Nullable AspectList getAspectsBaseOriginal() {
        return aspectsBaseOriginal;
    }

    public boolean isJarring() {
        return jarringTicks > 0;
    }

    public void beginJarring(int ticks) {
        jarringTicks = ticks;
        setChanged();
    }

    public void applyPrimordialPearl(RandomSource random, boolean researched) {
        for (AspectInstance entry : List.copyOf(aspectsBase.entries())) {
            Holder<IAspect> aspect = entry.aspect();
            if (!aspect.value().isPrimal()) {
                if (random.nextBoolean()) {
                    setBaseAmount(aspect, entry.amount() - 1);
                }
                continue;
            }
            setBaseAmount(aspect, entry.amount() - PEARL_PRIMAL_DROP + random.nextInt(researched ? PEARL_PRIMAL_SPREAD_RESEARCHED : PEARL_PRIMAL_SPREAD));
        }
        if (level != null) {
            HolderLookup.RegistryLookup<IAspect> registry = level.registryAccess().lookupOrThrow(IAspect.REGISTRY_KEY);
            for (ResourceKey<IAspect> key : TTAspects.PRIMALS) {
                Holder<IAspect> primal = registry.getOrThrow(key);
                int replacement = random.nextInt(researched ? PEARL_NEW_PRIMAL_MAX_RESEARCHED : PEARL_NEW_PRIMAL_MAX);
                if (replacement > aspectsBase.amountOf(primal)) {
                    setBaseAmount(primal, replacement);
                    if (aspects.amountOf(primal) < replacement) {
                        aspects = aspects.add(primal, 1);
                    }
                }
            }
        }
        if (nodeModifier == NodeModifier.FADING && random.nextBoolean()) {
            nodeModifier = NodeModifier.PALE;
        } else if (nodeModifier == NodeModifier.PALE && random.nextBoolean()) {
            nodeModifier = null;
        } else if (nodeModifier == null && random.nextInt(PEARL_BRIGHT_CHANCE) == 0) {
            nodeModifier = NodeModifier.BRIGHT;
        }
        nodeChange();
    }

    private void setBaseAmount(Holder<IAspect> aspect, int amount) {
        int target = Math.max(0, amount);
        int current = aspectsBase.amountOf(aspect);
        if (target > current) {
            aspectsBase = aspectsBase.add(aspect, target - current);
        } else if (target < current) {
            aspectsBase = aspectsBase.remove(aspect, current - target);
        }
        int contained = aspects.amountOf(aspect);
        if (contained > target) {
            aspects = aspects.remove(aspect, contained - target);
        }
    }

    public void nodeChange() {
        regeneration = -1;
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    private static final int ENERGIZED_REFILL_INTERVAL = 20;
    private static final int ENERGIZED_REFILL_BASE_DIVISOR = 20;
    private static final int CV_BUFFER_CAP_SECONDS = 10;
    private static final int TICKS_PER_SECOND = 20;

    private final Map<Identifier, Integer> cvAllowance = new HashMap<>();
    private final Map<Identifier, Integer> cvCredit = new HashMap<>();
    private final CentivisJournal centivisJournal = new CentivisJournal();
    private final IVisRelaySource relaySource = new NodeVisRelaySource(this);

    private void accrueCentivis() {
        for (AspectInstance entry : aspectsBase.entries()) {
            Identifier id = entry.aspect().unwrapKey().orElseThrow().identifier();
            int rate = entry.amount();
            int cap = rate * TICKS_PER_SECOND * CV_BUFFER_CAP_SECONDS;
            int allowance = cvAllowance.getOrDefault(id, 0);
            if (allowance < cap) {
                cvAllowance.put(id, Math.min(cap, allowance + rate));
            }
        }
    }

    public int centivisRate(Holder<IAspect> aspect) {
        return energized ? aspectsBase.amountOf(aspect) : 0;
    }

    public int availableCentivis(Holder<IAspect> aspect) {
        if (!energized) {
            return 0;
        }
        Identifier id = aspect.unwrapKey().orElseThrow().identifier();
        int stored = aspects.amountOf(aspect) * WandEconomy.CENTIVIS_PER_VIS + cvCredit.getOrDefault(id, 0);
        return Math.min(cvAllowance.getOrDefault(id, 0), stored);
    }

    public IVisRelaySource relaySource() {
        return relaySource;
    }

    public int drainCentivis(Holder<IAspect> aspect, int amount, TransactionContext transaction) {
        if (availableCentivis(aspect) <= 0 || amount <= 0) {
            return 0;
        }
        Identifier id = aspect.unwrapKey().orElseThrow().identifier();
        int allowance = Math.min(cvAllowance.getOrDefault(id, 0), amount);
        centivisJournal.updateSnapshots(transaction);
        int credit = cvCredit.getOrDefault(id, 0);
        while (credit < allowance && aspects.amountOf(aspect) > 0) {
            aspects = reduce(aspects, aspect, 1);
            credit += WandEconomy.CENTIVIS_PER_VIS;
        }
        int taken = Math.min(allowance, credit);
        cvCredit.put(id, credit - taken);
        cvAllowance.put(id, cvAllowance.getOrDefault(id, 0) - taken);
        return taken;
    }

    private record CentivisSnapshot(AspectList aspects, Map<Identifier, Integer> allowance, Map<Identifier, Integer> credit) {
    }

    private final class CentivisJournal extends SnapshotJournal<CentivisSnapshot> {
        @Override
        protected CentivisSnapshot createSnapshot() {
            return new CentivisSnapshot(aspects, Map.copyOf(cvAllowance), Map.copyOf(cvCredit));
        }

        @Override
        protected void revertToSnapshot(CentivisSnapshot snapshot) {
            aspects = snapshot.aspects();
            cvAllowance.clear();
            cvAllowance.putAll(snapshot.allowance());
            cvCredit.clear();
            cvCredit.putAll(snapshot.credit());
        }

        @Override
        protected void onRootCommit(CentivisSnapshot original) {
            if (!aspects.equals(original.aspects())) {
                syncContents();
            }
        }
    }

    public void serverTick(Level tickLevel, BlockPos pos) {
        if (!(tickLevel instanceof ServerLevel serverLevel)) {
            return;
        }
        applyNaturalTaintBootstrap(serverLevel, pos);
        if (energized) {
            boolean changed = false;
            if (tickLevel.getGameTime() % ENERGIZED_REFILL_INTERVAL == 0) {
                float visPerPoint = ThaumaturgeCommonConfig.ENERGIZED_NODE_VIS_PER_POINT.get().floatValue();
                boolean fluxFed = nodeType == NodeType.TAINTED;
                for (AspectInstance entry : aspectsBase.entries()) {
                    int points = Math.max(1, entry.amount() / ENERGIZED_REFILL_BASE_DIVISOR);
                    for (int i = 0; i < points; i++) {
                        if (aspects.amountOf(entry.aspect()) >= entry.amount()) {
                            break;
                        }
                        float drained = fluxFed ? AuraHelper.drainFlux(serverLevel, pos, visPerPoint, false) : AuraHelper.drainVis(serverLevel, pos, visPerPoint, false);
                        if (drained >= visPerPoint - 0.01F) {
                            aspects = aspects.add(entry.aspect(), 1);
                            changed = true;
                        } else {
                            if (drained > 0.0F) {
                                if (fluxFed) {
                                    AuraHelper.addFlux(serverLevel, pos, drained);
                                } else {
                                    AuraHelper.addVis(serverLevel, pos, drained);
                                }
                            }
                            break;
                        }
                    }
                }
            }
            accrueCentivis();
            if (drainTicks > 0 && --drainTicks == 0) {
                drainPlayer = null;
                changed = true;
            }
            if (changed) {
                setChanged();
                serverLevel.sendBlockUpdated(pos, getBlockState(), getBlockState(), 3);
            }
            return;
        }
        if (jarringTicks > 0) {
            jarringTicks--;
            setChanged();
            if (jarringTicks <= 0) {
                NodeJarRitual.completeJar(serverLevel, pos, this);
            }
            return;
        }
        count++;
        checkLock(serverLevel, pos);
        boolean change = false;
        change = handleHungryNode(serverLevel, pos, change);
        change = handleDischarge(serverLevel, pos, change);
        change = handleFeeding(serverLevel, pos, change);
        change = handleDecay(serverLevel, pos, change);
        change = handleStability(serverLevel, pos, change);
        change = handleTypeBehavior(serverLevel, pos, change);
        handleBiomeSpread(serverLevel, pos);
        if (drainTicks > 0 && --drainTicks == 0) {
            drainPlayer = null;
            change = true;
        }
        if (change) {
            setChanged();
            serverLevel.sendBlockUpdated(pos, getBlockState(), getBlockState(), 3);
        }
    }

    private boolean handleFeeding(ServerLevel serverLevel, BlockPos pos, boolean change) {
        if (regeneration < 0) {
            regeneration = REGEN_INTERVAL_NORMAL;
            if (nodeModifier != null) {
                regeneration = switch (nodeModifier) {
                    case BRIGHT -> REGEN_INTERVAL_BRIGHT;
                    case PALE -> REGEN_INTERVAL_PALE;
                    case FADING -> 0;
                };
            }
            regeneration *= feedingIntervalFactor();
        }
        if (wait > 0) {
            wait--;
        }
        if (regeneration <= 0 || wait != 0 || count % regeneration != 0) {
            return change;
        }
        List<Holder<IAspect>> depleted = new ArrayList<>();
        for (AspectInstance entry : aspects.entries()) {
            if (entry.amount() < aspectsBase.amountOf(entry.aspect())) {
                depleted.add(entry.aspect());
            }
        }
        RandomSource random = serverLevel.getRandom();
        driftFromChunkHealth(serverLevel, pos, random);
        if (depleted.isEmpty()) {
            starvation = 0;
            return change;
        }
        Holder<IAspect> target = depleted.get(random.nextInt(depleted.size()));
        float drained = nodeType == NodeType.TAINTED ? AuraHelper.drainFlux(serverLevel, pos, FEED_RAW_VIS_PER_POINT, false) : AuraHelper.drainVis(serverLevel, pos, FEED_RAW_VIS_PER_POINT, false);
        if (drained >= FEED_RAW_VIS_PER_POINT - 0.01F) {
            fill(target, 1);
            starvation = 0;
            return true;
        }
        if (drained > 0.0F) {
            if (nodeType == NodeType.TAINTED) {
                AuraHelper.addFlux(serverLevel, pos, drained);
            } else {
                AuraHelper.addVis(serverLevel, pos, drained);
            }
        }
        starvation++;
        if (starvation >= STARVATION_DRIFT_THRESHOLD) {
            starvation = 0;
            starve();
            return true;
        }
        return change;
    }

    private void starve() {
        if (nodeModifier == NodeModifier.BRIGHT) {
            nodeModifier = null;
        } else if (nodeModifier == null) {
            nodeModifier = NodeModifier.PALE;
        } else if (nodeModifier == NodeModifier.PALE) {
            nodeModifier = NodeModifier.FADING;
        } else if (nodeModifier == NodeModifier.FADING && nodeType != NodeType.HUNGRY) {
            setNodeType(NodeType.HUNGRY);
        }
        nodeChange();
    }

    private void driftFromChunkHealth(ServerLevel serverLevel, BlockPos pos, RandomSource random) {
        float base = AuraHelper.getAuraBase(serverLevel, pos);
        if (base <= 0.0F) {
            return;
        }
        float flux = AuraHelper.getFlux(serverLevel, pos);
        if (nodeType != NodeType.TAINTED && nodeType != NodeType.PURE && flux > base * FLUX_TAINT_THRESHOLD && random.nextInt(FLUX_TAINT_CHANCE) == 0) {
            setNodeType(NodeType.TAINTED);
            nodeChange();
            return;
        }
        boolean healthy = flux < base * BRIGHTEN_FLUX_LIMIT && AuraHelper.getVis(serverLevel, pos) >= base * BRIGHTEN_FILL_FRACTION && serverLevel.getBiome(pos).is(TTBiomes.MAGICAL_FOREST);
        if (healthy && random.nextInt(BRIGHTEN_CHANCE) == 0) {
            if (nodeModifier == NodeModifier.FADING) {
                nodeModifier = NodeModifier.PALE;
            } else if (nodeModifier == NodeModifier.PALE) {
                nodeModifier = null;
            } else if (nodeModifier == null) {
                nodeModifier = NodeModifier.BRIGHT;
            }
            nodeChange();
        }
    }

    private boolean handleDecay(ServerLevel serverLevel, BlockPos pos, boolean change) {
        if (count % DECAY_INTERVAL != 0) {
            return change;
        }
        RandomSource random = serverLevel.getRandom();
        for (AspectInstance entry : aspectsBase.entries()) {
            if (aspects.amountOf(entry.aspect()) <= 0) {
                aspectsBase = reduce(aspectsBase, entry.aspect(), 1);
                if (random.nextInt(20) == 0 || aspectsBase.amountOf(entry.aspect()) <= 0) {
                    aspects = reduce(aspects, entry.aspect(), aspects.amountOf(entry.aspect()));
                    aspectsBase = reduce(aspectsBase, entry.aspect(), aspectsBase.amountOf(entry.aspect()));
                    if (random.nextInt(5) == 0) {
                        if (nodeModifier == NodeModifier.BRIGHT) {
                            nodeModifier = null;
                        } else if (nodeModifier == null) {
                            nodeModifier = NodeModifier.PALE;
                        }
                        if (nodeModifier == NodeModifier.PALE && random.nextInt(5) == 0) {
                            nodeModifier = NodeModifier.FADING;
                        }
                    }
                }
                nodeChange();
                change = true;
                break;
            }
        }
        if (aspectsBase.isEmpty()) {
            removeDepletedNode(serverLevel, pos);
        }
        return change;
    }

    protected int feedingIntervalFactor() {
        if (lock == LOCK_BASIC) {
            return LOCK_BASIC_REGEN_FACTOR;
        }
        if (lock == LOCK_ADVANCED) {
            return LOCK_ADVANCED_REGEN_FACTOR;
        }
        return 1;
    }

    public int getLock() {
        return lock;
    }

    private static boolean isGenerated(ServerLevel level, BlockPos pos) {
        return level.getChunkSource().getChunkNow(SectionPos.blockToSectionCoord(pos.getX()), SectionPos.blockToSectionCoord(pos.getZ())) != null;
    }

    private static boolean isNeighbourhoodGenerated(ServerLevel level, BlockPos pos) {
        return isGenerated(level, pos) && isGenerated(level, pos.east()) && isGenerated(level, pos.west()) && isGenerated(level, pos.north()) && isGenerated(level, pos.south());
    }

    private void checkLock(ServerLevel serverLevel, BlockPos pos) {
        if (count > 1 && count % BEHAVIOR_INTERVAL != 0) {
            return;
        }
        BlockPos below = pos.below();
        if (!isNeighbourhoodGenerated(serverLevel, below)) {
            return;
        }
        int oldLock = lock;
        lock = 0;
        if (!serverLevel.hasNeighborSignal(below) && serverLevel.getBlockState(below).getBlock() instanceof BlockNodeStabilizer stabilizer) {
            lock = stabilizer.isAdvanced() ? LOCK_ADVANCED : LOCK_BASIC;
        }
        if (oldLock != lock) {
            regeneration = -1;
        }
    }

    protected boolean allowDischarge() {
        return true;
    }

    protected boolean allowTypeBehavior() {
        return true;
    }

    private boolean handleDischarge(ServerLevel serverLevel, BlockPos pos, boolean change) {
        if (nodeModifier == NodeModifier.FADING || !allowDischarge() || lock == LOCK_BASIC) {
            return change;
        }
        boolean shiny = nodeType == NodeType.HUNGRY || nodeModifier == NodeModifier.BRIGHT;
        int interval = nodeModifier == null ? 2 : (shiny ? 1 : (nodeModifier == NodeModifier.PALE ? 3 : 2));
        if (count % interval != 0) {
            return change;
        }
        RandomSource random = serverLevel.getRandom();
        if (nodeModifier == NodeModifier.PALE && random.nextBoolean()) {
            return change;
        }
        int x = random.nextInt(DISCHARGE_RANGE + 1) - random.nextInt(DISCHARGE_RANGE + 1);
        int y = random.nextInt(DISCHARGE_RANGE + 1) - random.nextInt(DISCHARGE_RANGE + 1);
        int z = random.nextInt(DISCHARGE_RANGE + 1) - random.nextInt(DISCHARGE_RANGE + 1);
        if (x == 0 && y == 0 && z == 0) {
            return change;
        }
        BlockPos otherPos = pos.offset(x, y, z);
        if (!isGenerated(serverLevel, otherPos)) {
            return change;
        }
        if (!(serverLevel.getBlockEntity(otherPos) instanceof BlockEntityNode other) || !other.allowDischarge() || other.lock > 0) {
            return change;
        }
        int otherAvg = (other.aspects.totalAmount() + other.aspectsBase.totalAmount()) / 2;
        int thisAvg = (aspects.totalAmount() + aspectsBase.totalAmount()) / 2;
        if (otherAvg >= thisAvg || other.aspects.isEmpty()) {
            return change;
        }
        List<AspectInstance> otherEntries = other.aspects.entries();
        Holder<IAspect> stolen = otherEntries.get(random.nextInt(otherEntries.size())).aspect();
        boolean moved = false;
        if (aspects.amountOf(stolen) < aspectsBase.amountOf(stolen) && other.drain(stolen, 1)) {
            fill(stolen, 1);
            moved = true;
        } else if (other.drain(stolen, 1)) {
            if (random.nextInt(1 + (int) (aspectsBase.amountOf(stolen) / (shiny ? 1.5 : 1.0))) == 0) {
                aspectsBase = raiseBase(aspectsBase, stolen, 1);
                if (nodeModifier == NodeModifier.PALE && random.nextInt(100) == 0) {
                    nodeModifier = null;
                    regeneration = -1;
                }
                if (random.nextInt(3) == 0) {
                    other.aspectsBase = reduce(other.aspectsBase, stolen, 1);
                }
            }
            moved = true;
        }
        if (moved) {
            other.wait = other.regeneration / 2;
            other.setChanged();
            serverLevel.sendBlockUpdated(otherPos, other.getBlockState(), other.getBlockState(), 3);
            serverLevel.playSound(null, otherPos, TTSounds.ZAP.get(), SoundSource.BLOCKS, ZAP_VOLUME, 1.0F + random.nextFloat() * ZAP_PITCH_VARIATION);
            Effects.arcBolt(serverLevel, Vec3.atCenterOf(otherPos)).to(Vec3.atCenterOf(pos)).width(ZAP_WIDTH).send();
            return true;
        }
        return change;
    }

    private boolean handleStability(ServerLevel serverLevel, BlockPos pos, boolean change) {
        if (count % STABILITY_INTERVAL != 0) {
            return change;
        }
        RandomSource random = serverLevel.getRandom();
        if (nodeType == NodeType.UNSTABLE && random.nextBoolean()) {
            if (lock == 0) {
                Holder<IAspect> primal = randomStoredPrimal(random);
                if (primal != null && drain(primal, 1)) {
                    ResourceKey<IAspect> key = primal.unwrapKey().orElseThrow();
                    serverLevel.addFreshEntity(new EntityAspectOrb(serverLevel, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, key, 1));
                    return true;
                }
            } else if (random.nextInt(UNSTABLE_CURE_ROLL / lock) == UNSTABLE_CURE_MAGIC) {
                setNodeType(NodeType.NORMAL);
                nodeChange();
                return true;
            }
        }
        if (nodeModifier == NodeModifier.FADING && lock > 0 && random.nextInt(FADING_CURE_ROLL / lock) == FADING_CURE_MAGIC) {
            nodeModifier = NodeModifier.PALE;
            nodeChange();
            return true;
        }
        return change;
    }

    private @Nullable Holder<IAspect> randomStoredPrimal(RandomSource random) {
        List<Holder<IAspect>> primals = new ArrayList<>();
        for (AspectInstance entry : aspects.entries()) {
            if (entry.aspect().value().isPrimal() && entry.amount() > 0) {
                primals.add(entry.aspect());
            }
        }
        return primals.isEmpty() ? null : primals.get(random.nextInt(primals.size()));
    }

    private void handleBiomeSpread(ServerLevel serverLevel, BlockPos pos) {
        if (!allowTypeBehavior() || serverLevel.dimension() != Level.OVERWORLD) {
            return;
        }
        RandomSource random = serverLevel.getRandom();
        if (count % TAINTED_NODE_CONVERSION_INTERVAL == 0 && nodeType != NodeType.PURE && nodeType != NodeType.TAINTED && TaintBiomeManager.isTainted(serverLevel, pos)
                && random.nextInt(TAINTED_NODE_CONVERSION_CHANCE) == 0) {
            setNodeType(NodeType.TAINTED);
            nodeChange();
        }
        if (count % BIOME_SPREAD_INTERVAL != 0) {
            return;
        }
        if (nodeType == NodeType.TAINTED) {
            BlockPos target = randomBiomeTarget(serverLevel, pos, TAINTED_BIOME_SPREAD_RANGE);
            if (target != null) {
                TaintBiomeManager.taintColumn(serverLevel, target);
            }
        } else if (nodeType == NodeType.DARK) {
            spreadBiomeColumn(serverLevel, pos, DARK_BIOME_SPREAD_RANGE, TTBiomes.EERIE);
        } else if (nodeType == NodeType.PURE) {
            BlockPos target = randomBiomeTarget(serverLevel, pos, PURE_BIOME_SPREAD_RANGE);
            if (target != null && TaintBiomeManager.isTainted(serverLevel, target)) {
                TaintBiomeManager.replaceColumn(serverLevel, target, TTBiomes.MAGICAL_FOREST);
            } else if (nearSilverwood(serverLevel, pos)) {
                spreadBiomeColumn(serverLevel, pos, PURE_BIOME_SPREAD_RANGE, TTBiomes.MAGICAL_FOREST);
            }
        }
    }

    private static boolean nearSilverwood(ServerLevel serverLevel, BlockPos pos) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    cursor.setWithOffset(pos, dx, dy, dz);
                    if (serverLevel.getBlockState(cursor).is(TTBlockTags.SILVERWOOD_LOGS)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static void spreadBiomeColumn(ServerLevel serverLevel, BlockPos origin, int range, ResourceKey<Biome> biomeKey) {
        BlockPos sample = randomBiomeTarget(serverLevel, origin, range);
        if (sample == null || serverLevel.getBiome(sample).is(biomeKey)) {
            return;
        }
        Holder<Biome> biome = serverLevel.registryAccess().lookupOrThrow(Registries.BIOME).getOrThrow(biomeKey);
        FillBiomeCommand.fill(serverLevel, new BlockPos(sample.getX(), serverLevel.getMinY(), sample.getZ()), new BlockPos(sample.getX(), serverLevel.getMaxY(), sample.getZ()), biome);
    }

    private static @Nullable BlockPos randomBiomeTarget(ServerLevel serverLevel, BlockPos origin, int range) {
        RandomSource random = serverLevel.getRandom();
        BlockPos sample = new BlockPos(origin.getX() + random.nextInt(range) - random.nextInt(range), origin.getY(), origin.getZ() + random.nextInt(range) - random.nextInt(range));
        return serverLevel.hasChunkAt(sample) ? sample : null;
    }

    private boolean handleTypeBehavior(ServerLevel serverLevel, BlockPos pos, boolean change) {
        if (!allowTypeBehavior()) {
            return change;
        }
        if (nodeType == NodeType.HUNGRY) {
            if (count % ThaumaturgeCommonConfig.HUNGRY_NODE_BLOCK_EAT_INTERVAL.get() == 0) {
                eatBlock(serverLevel, pos, serverLevel.getRandom());
            }
            return change;
        }
        if (count % BEHAVIOR_INTERVAL != 0) {
            return change;
        }
        RandomSource random = serverLevel.getRandom();
        switch (nodeType) {
            case TAINTED -> {
                BlockPos target = pos.offset(random.nextInt(5) - random.nextInt(5), random.nextInt(5) - random.nextInt(5), random.nextInt(5) - random.nextInt(5));
                if (random.nextBoolean()) {
                    TaintApi.spreadFibres(serverLevel, target, true);
                }
                if (count % PERIODIC_INTERVAL == 0) {
                    float saturation = AuraHelper.getFlux(serverLevel, pos) / Math.max(1.0F, AuraHelper.getAuraBase(serverLevel, pos));
                    if (random.nextFloat() > saturation * TAINTED_POLLUTE_SATURATION_SCALE) {
                        AuraHelper.polluteAura(serverLevel, pos, taintedFluxStrength(), true);
                    }
                }
            }
            case PURE -> {
                float drained = AuraHelper.drainFlux(serverLevel, pos, PURE_FLUX_CLEANSE, false);
                if (drained > 0.0F && count % PERIODIC_INTERVAL == 0 && random.nextFloat() < PURE_EROSION_CHANCE) {
                    erodePureNode(serverLevel, pos, random);
                    change = true;
                }
            }
            case DARK -> spawnDarkGuard(serverLevel, pos, random);
            default -> {
            }
        }
        return change;
    }

    private float taintedFluxStrength() {
        int strength = (int) Math.max(1.0, Math.sqrt(Math.max(1, aspectsBase.totalAmount()) / TAINTED_FLUX_ASPECT_DIVISOR));
        return Math.max(1.0F, strength * TAINTED_FLUX_PER_STRENGTH);
    }

    private void erodePureNode(ServerLevel serverLevel, BlockPos pos, RandomSource random) {
        if (aspectsBase.isEmpty()) {
            return;
        }
        List<AspectInstance> entries = aspectsBase.entries();
        AspectInstance chosen = entries.get(random.nextInt(entries.size()));
        aspectsBase = reduce(aspectsBase, chosen.aspect(), 1);
        int excess = aspects.amountOf(chosen.aspect()) - aspectsBase.amountOf(chosen.aspect());
        if (excess > 0) {
            aspects = reduce(aspects, chosen.aspect(), excess);
        }
        nodeChange();
        if (aspectsBase.isEmpty()) {
            removeDepletedNode(serverLevel, pos);
        }
    }

    public void markNaturalTaintBootstrap() {
        naturalTaintBootstrapPending = true;
        setChanged();
    }

    private void applyNaturalTaintBootstrap(ServerLevel level, BlockPos pos) {
        if (!naturalTaintBootstrapPending) {
            return;
        }
        naturalTaintBootstrapPending = false;
        setChanged();
        if (nodeType != NodeType.TAINTED || ThaumaturgeCommonConfig.WUSS_MODE.get()) {
            return;
        }
        AuraHelper.polluteAura(level, pos, NATURAL_TAINTED_NODE_FLUX, false);
        RandomSource random = level.getRandom();
        for (int attempt = 0; attempt < NATURAL_TAINTED_FIBRE_ATTEMPTS; attempt++) {
            BlockPos target = pos.offset(bootstrapOffset(random), bootstrapOffset(random), bootstrapOffset(random));
            if (!isNeighbourhoodGenerated(level, target)) {
                continue;
            }
            BlockState targetState = level.getBlockState(target);
            if ((targetState.isAir() || targetState.canBeReplaced()) && TaintHelper.isAdjacentToSolidBlock(level, target)) {
                level.setBlock(target, BlockTaintFibre.stateForWorld(level, target), Block.UPDATE_ALL);
            }
        }
    }

    private static int bootstrapOffset(RandomSource random) {
        return random.nextInt(NATURAL_TAINTED_FIBRE_RANGE) - random.nextInt(NATURAL_TAINTED_FIBRE_RANGE);
    }

    private void spawnDarkGuard(ServerLevel serverLevel, BlockPos pos, RandomSource random) {
        if (!random.nextBoolean()) {
            return;
        }
        Player player = serverLevel.getNearestPlayer(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, DARK_SPAWN_PLAYER_RANGE, false);
        if (player == null) {
            return;
        }
        AABB box = new AABB(pos).inflate(10.0, 6.0, 10.0);
        if (serverLevel.getEntitiesOfClass(EntityBrainyZombie.class, box).size() > DARK_SPAWN_CAP) {
            return;
        }
        double x = pos.getX() + (random.nextDouble() - random.nextDouble()) * 5.0;
        double y = pos.getY() + random.nextInt(3) - 1;
        double z = pos.getZ() + (random.nextDouble() - random.nextDouble()) * 5.0;
        EntityBrainyZombie zombie = TTEntities.BRAINY_ZOMBIE.get().create(serverLevel, EntitySpawnReason.EVENT);
        if (zombie == null) {
            return;
        }
        zombie.snapTo(x, y, z, random.nextFloat() * 360.0F, 0.0F);
        if (zombie.checkSpawnRules(serverLevel, EntitySpawnReason.EVENT)) {
            serverLevel.addFreshEntity(zombie);
            serverLevel.levelEvent(2004, pos, 0);
        } else {
            zombie.discard();
        }
    }

    private boolean handleHungryNode(ServerLevel serverLevel, BlockPos pos, boolean change) {
        if (nodeType != NodeType.HUNGRY || !allowTypeBehavior()) {
            return change;
        }
        Vec3 center = Vec3.atCenterOf(pos);
        double itemPullRange = hungryBlockEatRange() + HUNGRY_ITEM_PULL_MARGIN;
        List<Entity> targets = serverLevel.getEntitiesOfClass(Entity.class, new AABB(pos).inflate(Math.max(itemPullRange, HUNGRY_PULL_RANGE)));
        for (Entity target : targets) {
            if (target instanceof Player player && (player.isCreative() || player.isSpectator())) {
                continue;
            }
            if (!target.isAlive()) {
                continue;
            }
            double distanceSq = target.distanceToSqr(center);
            if (distanceSq < HUNGRY_EAT_RANGE_SQ) {
                target.hurtServer(serverLevel, serverLevel.damageSources().fellOutOfWorld(), 1.0F);
                if (!target.isAlive() && devour(serverLevel, target)) {
                    change = true;
                }
            }
            double pullRange = target instanceof ItemEntity ? itemPullRange : HUNGRY_PULL_RANGE;
            Vec3 delta = center.subtract(target.position()).scale(1.0 / pullRange);
            double length = delta.length();
            double power = 1.0 - length;
            if (power > 0.0) {
                power *= power;
                Vec3 pull = delta.normalize();
                target.push(pull.x * power * 0.15, pull.y * power * 0.25, pull.z * power * 0.15);
                if (target instanceof ServerPlayer player) {
                    player.connection.send(new ClientboundSetEntityMotionPacket(player));
                }
            }
        }
        return change;
    }

    private boolean devour(ServerLevel serverLevel, Entity target) {
        AspectList devoured = devouredAspects(target);
        if (devoured.isEmpty()) {
            return false;
        }
        Map<ResourceKey<IAspect>, Integer> primals = WandChargingEvents.reduceToPrimals(devoured);
        if (primals.isEmpty()) {
            return false;
        }
        RandomSource random = serverLevel.getRandom();
        List<ResourceKey<IAspect>> keys = new ArrayList<>(primals.keySet());
        ResourceKey<IAspect> chosen = keys.get(random.nextInt(keys.size()));
        Holder<IAspect> holder = serverLevel.registryAccess().lookupOrThrow(IAspect.REGISTRY_KEY).getOrThrow(chosen);
        if (aspects.amountOf(holder) < aspectsBase.amountOf(holder)) {
            fill(holder, 1);
        } else if (random.nextInt(1 + aspectsBase.amountOf(holder) * 2) < primals.get(chosen)) {
            aspectsBase = raiseBase(aspectsBase, holder, 1);
        }
        nodeChange();
        return true;
    }

    private static AspectList devouredAspects(Entity target) {
        if (target instanceof ItemEntity item) {
            return AspectIndexAccess.of(item.getItem().copyWithCount(1));
        }
        if (target instanceof LivingEntity living) {
            return EntityAspects.of(living);
        }
        return AspectList.EMPTY;
    }

    private void eatBlock(ServerLevel serverLevel, BlockPos pos, RandomSource random) {
        BlockPos target = hungryTarget(serverLevel, pos, random);
        if (target != null) {
            serverLevel.destroyBlock(target, true);
        }
    }

    private @Nullable BlockPos hungryTarget(Level level, BlockPos pos, RandomSource random) {
        int range = hungryBlockEatRange();
        int tx = pos.getX() + random.nextInt(range) - random.nextInt(range);
        int ty = pos.getY() + random.nextInt(range) - random.nextInt(range);
        int tz = pos.getZ() + random.nextInt(range) - random.nextInt(range);
        if (!level.hasChunk(SectionPos.blockToSectionCoord(tx), SectionPos.blockToSectionCoord(tz))) {
            return null;
        }
        ty = Math.min(ty, level.getHeight(Heightmap.Types.MOTION_BLOCKING, tx, tz));
        Vec3 from = Vec3.atCenterOf(pos);
        Vec3 to = new Vec3(tx + 0.5, ty + 0.5, tz + 0.5);
        BlockHitResult hit = level.clip(new SourceIgnoringClipContext(pos, from, to, ClipContext.Block.OUTLINE, ClipContext.Fluid.SOURCE_ONLY));
        if (hit.getType() != HitResult.Type.BLOCK) {
            return null;
        }
        BlockPos target = hit.getBlockPos();
        if (target.equals(pos) || target.distSqr(pos) > (double) range * range) {
            return null;
        }
        BlockState state = level.getBlockState(target);
        float hardness = state.getDestroySpeed(level, target);
        if (state.isAir() || hardness < 0.0F || hardness >= ThaumaturgeCommonConfig.HUNGRY_NODE_BLOCK_HARDNESS.get()) {
            return null;
        }
        return target;
    }

    private int hungryBlockEatRange() {
        if (!ThaumaturgeCommonConfig.SCALE_HUNGRY_NODE_RANGE_BY_MODIFIER.get()) {
            return ThaumaturgeCommonConfig.HUNGRY_NODE_BLOCK_EAT_RANGE.get();
        }
        int minimum = ThaumaturgeCommonConfig.HUNGRY_NODE_MINIMUM_BLOCK_EAT_RANGE.get();
        int maximum = Math.max(minimum, ThaumaturgeCommonConfig.HUNGRY_NODE_MAXIMUM_BLOCK_EAT_RANGE.get());
        int difference = maximum - minimum;
        if (nodeModifier == NodeModifier.BRIGHT) {
            return maximum;
        }
        if (nodeModifier == NodeModifier.PALE) {
            return minimum + Math.round(difference / 3.0F);
        }
        if (nodeModifier == NodeModifier.FADING) {
            return minimum;
        }
        return minimum + Math.round(difference * 2.0F / 3.0F);
    }

    public void burstIntoOrbs(ServerLevel serverLevel, BlockPos pos) {
        Map<ResourceKey<IAspect>, Integer> primals = WandChargingEvents.reduceToPrimals(aspects);
        RandomSource random = serverLevel.getRandom();
        for (Map.Entry<ResourceKey<IAspect>, Integer> entry : primals.entrySet()) {
            int remaining = entry.getValue();
            int maxPerOrb = Math.max(ORB_BURST_MAX_PER_ASPECT, (2 * remaining + ORB_BURST_MAX_ORBS_PER_ASPECT - 1) / ORB_BURST_MAX_ORBS_PER_ASPECT);
            int orbs = 0;
            while (remaining > 0) {
                orbs++;
                int value = orbs >= ORB_BURST_MAX_ORBS_PER_ASPECT ? remaining : Math.min(remaining, 1 + random.nextInt(maxPerOrb));
                remaining -= value;
                serverLevel.addFreshEntity(new EntityAspectOrb(serverLevel, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, entry.getKey(), value));
            }
        }
        aspects = AspectList.EMPTY;
    }

    public boolean drainToWand(ServerLevel serverLevel, Player player, ItemStack wandStack, int useTicks) {
        if (useTicks % NODE_DRAIN_INTERVAL != 0) {
            return false;
        }
        int tap = 1;
        if (KnowledgeAccess.of(player).isResearchKnown(RESEARCH_NODE_TAPPER_1)) {
            tap++;
        }
        if (KnowledgeAccess.of(player).isResearchKnown(RESEARCH_NODE_TAPPER_2)) {
            tap++;
        }
        WandParts parts = WandVisHelper.getParts(wandStack);
        boolean starterWand = parts.rod() == TTWandParts.ROD_WOOD.get() && parts.cap() == TTWandParts.CAP_IRON.get();
        boolean preserve = !player.isShiftKeyDown() && !starterWand && KnowledgeAccess.of(player).isResearchKnown(RESEARCH_NODE_PRESERVE);
        RandomSource random = serverLevel.getRandom();
        List<Holder<IAspect>> candidates = new ArrayList<>();
        int min = preserve ? 1 : 0;
        for (AspectInstance entry : aspects.entries()) {
            if (!entry.aspect().value().isPrimal() || entry.amount() <= min) {
                continue;
            }
            ResourceKey<IAspect> key = entry.aspect().unwrapKey().orElseThrow();
            if (WandVisHelper.getVis(wandStack, key) < WandVisHelper.getMaxVis(wandStack)) {
                candidates.add(entry.aspect());
            }
        }
        if (candidates.isEmpty()) {
            return false;
        }
        Holder<IAspect> chosen = candidates.get(random.nextInt(candidates.size()));
        int available = aspects.amountOf(chosen);
        int take = Math.min(tap, available);
        if (preserve && take == available) {
            take--;
        }
        if (take <= 0) {
            return false;
        }
        ResourceKey<IAspect> key = chosen.unwrapKey().orElseThrow();
        int leftover = WandVisHelper.addVis(wandStack, key, take, true);
        int moved = take - leftover;
        if (moved <= 0) {
            return false;
        }
        drain(chosen, moved);
        drainPlayer = player.getUUID();
        drainColor = chosen.value().color();
        drainTicks = DRAIN_LINGER_TICKS;
        setChanged();
        serverLevel.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        return true;
    }

    public void clientTick(Level clientLevel, BlockPos pos) {
        if (energized || nodeType != NodeType.HUNGRY || !allowTypeBehavior()) {
            return;
        }
        RandomSource random = clientLevel.getRandom();
        BlockPos target = hungryTarget(clientLevel, pos, random);
        if (target == null) {
            return;
        }
        BlockState state = clientLevel.getBlockState(target);
        Vec3 from = Vec3.atCenterOf(pos);
        Vec3 pull = from.subtract(Vec3.atCenterOf(target)).normalize().scale(0.3);
        for (int i = 0; i < 3; i++) {
            clientLevel.addParticle(new BlockParticleOption(ParticleTypes.BLOCK, state), target.getX() + random.nextFloat(), target.getY() + random.nextFloat(), target.getZ() + random.nextFloat(),
                    pull.x, pull.y + 0.05, pull.z);
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.store("Type", NodeType.CODEC, nodeType);
        if (nodeModifier != null) {
            output.store("Modifier", NodeModifier.CODEC, nodeModifier);
        }
        output.store("Aspects", AspectList.CODEC, aspects);
        output.store("AspectsBase", AspectList.CODEC, aspectsBase);
        if (energized) {
            output.putBoolean("Energized", true);
        }
        if (aspectsBaseOriginal != null) {
            output.store("AspectsBaseOriginal", AspectList.CODEC, aspectsBaseOriginal);
        }
        if (drainPlayer != null) {
            output.store("DrainPlayer", UUIDUtil.CODEC, drainPlayer);
            output.putInt("DrainColor", drainColor);
        }
        if (jarringTicks > 0) {
            output.putInt("Jarring", jarringTicks);
        }
        if (naturalTaintBootstrapPending) {
            output.putBoolean("NaturalTaintBootstrap", true);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        nodeType = input.read("Type", NodeType.CODEC).orElse(NodeType.NORMAL);
        energized = input.getBooleanOr("Energized", false);
        nodeModifier = input.read("Modifier", NodeModifier.CODEC).orElse(null);
        aspects = input.read("Aspects", AspectList.CODEC).orElse(AspectList.EMPTY);
        aspectsBase = input.read("AspectsBase", AspectList.CODEC).orElse(AspectList.EMPTY);
        aspectsBaseOriginal = input.read("AspectsBaseOriginal", AspectList.CODEC).orElse(null);
        drainPlayer = input.read("DrainPlayer", UUIDUtil.CODEC).orElse(null);
        drainColor = input.getIntOr("DrainColor", 0xFFFFFF);
        jarringTicks = input.getIntOr("Jarring", 0);
        naturalTaintBootstrapPending = input.getBooleanOr("NaturalTaintBootstrap", false);
        regeneration = -1;
        updateLocationIndex();
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag nbt = super.getUpdateTag(registries);
        try (ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(problemPath(), Thaumaturge.LOGGER)) {
            TagValueOutput output = TagValueOutput.createWithContext(reporter, registries);
            saveAdditional(output);
            nbt.merge(output.buildResult());
        }
        return nbt;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
