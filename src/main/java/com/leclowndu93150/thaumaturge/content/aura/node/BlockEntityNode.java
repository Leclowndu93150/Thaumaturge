package com.leclowndu93150.thaumaturge.content.aura.node;

import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.Aspects;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.IAspectContainer;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.api.aura.IVisRelaySource;
import com.leclowndu93150.thaumaturge.api.nodes.NodeModifier;
import com.leclowndu93150.thaumaturge.api.nodes.NodeType;
import com.leclowndu93150.thaumaturge.api.taint.ITaintOrigin;
import com.leclowndu93150.thaumaturge.api.transaction.TransactionContext;
import com.leclowndu93150.thaumaturge.content.blockentity.AbstractSyncedBlockEntity;
import com.leclowndu93150.thaumaturge.content.effect.Effects;
import com.leclowndu93150.thaumaturge.content.taint.spread.TaintOrigins;
import com.leclowndu93150.thaumaturge.content.wands.EntityAspectOrb;
import com.leclowndu93150.thaumaturge.content.wands.WandChargingEvents;
import com.leclowndu93150.thaumaturge.data.worldgen.biome.TTBiomes;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import com.leclowndu93150.thaumaturge.serialization.TTNbt;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.SectionPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class BlockEntityNode extends AbstractSyncedBlockEntity implements IAspectContainer, ITaintOrigin {
    private static final int REGEN_INTERVAL_NORMAL = 600;
    private static final int REGEN_INTERVAL_BRIGHT = 400;
    private static final int REGEN_INTERVAL_PALE = 900;
    private static final float FEED_RAW_VIS_PER_POINT = 3.0F;
    private static final int STARVATION_DRIFT_THRESHOLD = 10;
    private static final int DECAY_INTERVAL = 1200;
    private static final int DISCHARGE_RANGE = 4;
    static final int BEHAVIOR_INTERVAL = 50;
    private static final int STABILITY_INTERVAL = 100;
    private static final float FLUX_TAINT_THRESHOLD = 0.5F;
    private static final int FLUX_TAINT_CHANCE = 20;
    private static final float BRIGHTEN_FLUX_LIMIT = 0.1F;
    private static final float BRIGHTEN_FILL_FRACTION = 0.9F;
    private static final int BRIGHTEN_CHANCE = 50;
    private static final double HUNGRY_PULL_RANGE = 15.0;
    private static final int ORB_BURST_MAX_PER_ASPECT = 10;
    private static final int ORB_BURST_MAX_ORBS_PER_ASPECT = 20;
    private static final float ZAP_WIDTH = 0.3F;
    private static final int LOCK_BASIC = 1;
    private static final int LOCK_ADVANCED = 2;
    private static final int LOCK_BASIC_REGEN_FACTOR = 2;
    private static final int LOCK_ADVANCED_REGEN_FACTOR = 20;
    private static final int UNSTABLE_CURE_ROLL = 10000;
    private static final int UNSTABLE_CURE_MAGIC = 42;
    private static final int FADING_CURE_ROLL = 12500;
    private static final int FADING_CURE_MAGIC = 69;
    private static final int MAX_DECOMPOSE_DEPTH = 8;

    private final NodeHunger hunger = new NodeHunger(this);
    private NodeType nodeType = NodeType.NORMAL;
    private @Nullable NodeModifier nodeModifier;
    protected AspectList aspects = AspectList.EMPTY;
    protected AspectList aspectsBase = AspectList.EMPTY;
    private @Nullable AspectList aspectsBaseOriginal;
    private int count;
    private int regeneration = -1;
    private int wait;
    private int starvation;
    private final IVisRelaySource relaySource = new NodeVisRelaySource(this);
    private int lock;
    protected boolean energized;
    private @Nullable UUID drainPlayer;
    private int drainColor = 0xFFFFFF;
    private int drainTicks;
    private int jarringTicks;
    private float taintReserve;
    int taintPurifyTicks;
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
        if (type != NodeType.TAINTED) {
            taintReserve = 0.0F;
            taintPurifyTicks = 0;
        }
        refreshIndex();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        refreshIndex();
    }

    private void refreshIndex() {
        if (!(level instanceof ServerLevel serverLevel) || !isNodeBlock()) {
            return;
        }
        NodeLocationIndex.get(serverLevel).register(worldPosition, nodeType);
        if (nodeType == NodeType.TAINTED) {
            TaintOrigins.register(serverLevel, worldPosition);
        } else {
            TaintOrigins.unregister(serverLevel, worldPosition);
        }
    }

    @Override
    public boolean isLiveTaintOrigin() {
        return nodeType == NodeType.TAINTED && allowTypeBehavior() && !isRemoved();
    }

    @Override
    public float getTaintReserve() {
        return taintReserve;
    }

    @Override
    public void setTaintReserve(float reserve) {
        taintReserve = Math.max(0.0F, reserve);
        setChanged();
    }

    void removeDepleted(ServerLevel serverLevel, BlockPos pos) {
        BlockState state = serverLevel.getBlockState(pos);
        if (state.getBlock() instanceof NodeHostBlock host) {
            serverLevel.setBlock(pos, host.depletedState(state), Block.UPDATE_ALL);
        } else {
            serverLevel.removeBlock(pos, false);
        }
    }

    void collapse(ServerLevel serverLevel, BlockPos pos) {
        if (getBlockState().getBlock() instanceof NodeHostBlock) {
            removeDepleted(serverLevel, pos);
        } else {
            serverLevel.destroyBlock(pos, true);
        }
    }

    void recordDrain(UUID player, int color) {
        drainPlayer = player;
        drainColor = color;
        drainTicks = DRAIN_LINGER_TICKS;
        setChanged();
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    private boolean isNodeBlock() {
        return getBlockState().is(TTBlocks.NODE.get()) || getBlockState().is(TTBlocks.SILVERWOOD_NODE_LOG.get());
    }

    public @Nullable NodeModifier getNodeModifier() {
        return nodeModifier;
    }

    public void setNodeModifier(@Nullable NodeModifier modifier) {
        this.nodeModifier = modifier;
        regeneration = -1;
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
            // Transducers consume the live store while charging; conversion restores the node's original capacity
            // as primal vis rather than preserving only that depleted charging remainder.
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

    public void nodeChange() {
        regeneration = -1;
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    private final NodeCentivis centivis = new NodeCentivis(this);

    public int centivisRate(Holder<IAspect> aspect) {
        return energized ? aspectsBase.amountOf(aspect) : 0;
    }

    public IVisRelaySource relaySource() {
        return relaySource;
    }

    public int availableCentivis(Holder<IAspect> aspect) {
        if (!energized) {
            return 0;
        }
        return centivis.available(aspect);
    }

    public int drainCentivis(Holder<IAspect> aspect, int amount, TransactionContext transaction) {
        return energized ? centivis.drain(aspect, amount, transaction) : 0;
    }

    void changed() {
        setChangedAndSync();
    }

    public void serverTick(Level tickLevel, BlockPos pos) {
        if (!(tickLevel instanceof ServerLevel serverLevel)) {
            return;
        }
        if (energized) {
            boolean changed = false;
            if (NodeEnergy.refillDue(serverLevel) && NodeEnergy.refill(this, serverLevel, pos)) {
                changed = true;
            }
            centivis.accrue();
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
        change = handleFeeding(serverLevel, pos, change);
        change = handleDischarge(serverLevel, pos, change);
        change = handleDecay(serverLevel, pos, change);
        if (count % DECAY_INTERVAL == 0 && aspectsBase.isEmpty()) {
            return;
        }
        change = handleStability(serverLevel, pos, change);
        if (allowTypeBehavior() && !handleTypeBehavior(serverLevel, pos)) {
            handleBiomeSpread(serverLevel, pos);
        }
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
        int interval = refillInterval();
        if (wait > 0) {
            wait--;
        }
        if (interval <= 0 || wait != 0 || count % interval != 0) {
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
        float drained = nodeType == NodeType.TAINTED
                ? AuraHelper.drainFlux(serverLevel, pos, FEED_RAW_VIS_PER_POINT, false)
                : AuraHelper.drainVis(serverLevel, pos, FEED_RAW_VIS_PER_POINT, false);
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

    int refillInterval() {
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
        return regeneration;
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
        if (nodeType != NodeType.TAINTED
                && nodeType != NodeType.PURE
                && flux > base * FLUX_TAINT_THRESHOLD
                && random.nextInt(FLUX_TAINT_CHANCE) == 0) {
            setNodeType(NodeType.TAINTED);
            nodeChange();
            return;
        }
        boolean healthy = flux < base * BRIGHTEN_FLUX_LIMIT
                && AuraHelper.getVis(serverLevel, pos) >= base * BRIGHTEN_FILL_FRACTION
                && serverLevel.getBiome(pos).is(TTBiomes.MAGICAL_FOREST);
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
                int remaining = entry.amount() - 1;
                if (remaining > 0 && random.nextInt(20) != 0) {
                    aspectsBase = reduce(aspectsBase, entry.aspect(), 1);
                } else {
                    aspects = reduce(aspects, entry.aspect(), aspects.amountOf(entry.aspect()));
                    aspectsBase = reduce(aspectsBase, entry.aspect(), remaining + 1);
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
            removeDepleted(serverLevel, pos);
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
        return level.getChunkSource()
                        .getChunkNow(
                                SectionPos.blockToSectionCoord(pos.getX()), SectionPos.blockToSectionCoord(pos.getZ()))
                != null;
    }

    private static boolean isNeighbourhoodGenerated(ServerLevel level, BlockPos pos) {
        return isGenerated(level, pos)
                && isGenerated(level, pos.east())
                && isGenerated(level, pos.west())
                && isGenerated(level, pos.north())
                && isGenerated(level, pos.south());
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
        int nextLock = 0;
        if (allowLock()
                && !serverLevel.hasNeighborSignal(below)
                && serverLevel.getBlockState(below).getBlock() instanceof BlockNodeStabilizer stabilizer) {
            nextLock = stabilizer.isAdvanced() ? LOCK_ADVANCED : LOCK_BASIC;
        }
        lock = nextLock;
        if (oldLock != nextLock) {
            regeneration = -1;
        }
    }

    protected boolean allowDischarge() {
        return true;
    }

    protected boolean allowLock() {
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
        BlockPos otherPos = NodeScatter.around(pos, DISCHARGE_RANGE, random);
        if (otherPos.equals(pos)) {
            return change;
        }
        if (!isGenerated(serverLevel, otherPos)) {
            return change;
        }
        if (!(serverLevel.getBlockEntity(otherPos) instanceof BlockEntityNode other)
                || !other.allowDischarge()
                || other.lock > 0) {
            return change;
        }
        int otherAvg = (other.aspects.totalAmount() + other.aspectsBase.totalAmount()) / 2;
        int thisAvg = (aspects.totalAmount() + aspectsBase.totalAmount()) / 2;
        if (otherAvg >= thisAvg || other.aspects.isEmpty()) {
            return change;
        }
        List<AspectInstance> otherEntries = other.aspects.entries();
        Holder<IAspect> stolen =
                otherEntries.get(random.nextInt(otherEntries.size())).aspect();
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
                    shrinkCapacity(other, stolen);
                }
            }
            moved = true;
        }
        if (moved) {
            other.wait = other.refillInterval() / 2;
            other.setChanged();
            serverLevel.sendBlockUpdated(otherPos, other.getBlockState(), other.getBlockState(), 3);
            if (other.aspects.isEmpty() && !other.isEnergized()) {
                other.collapse(serverLevel, otherPos);
            }
            serverLevel.playSound(
                    null, otherPos, TTSounds.ZAP.get(), SoundSource.BLOCKS, 0.1F, 1.0F + random.nextFloat() * 0.2F);
            Effects.arcBolt(serverLevel, Vec3.atCenterOf(otherPos))
                    .to(Vec3.atCenterOf(pos))
                    .width(ZAP_WIDTH)
                    .send();
            return true;
        }
        return change;
    }

    private static void shrinkCapacity(BlockEntityNode node, Holder<IAspect> aspect) {
        node.aspectsBase = reduce(node.aspectsBase, aspect, 1);
        int capacity = node.aspectsBase.amountOf(aspect);
        int stored = node.aspects.amountOf(aspect);
        if (stored > capacity) {
            node.aspects = reduce(node.aspects, aspect, stored - capacity);
        }
        node.regeneration = -1;
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
                    serverLevel.addFreshEntity(new EntityAspectOrb(
                            serverLevel, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, key, 1));
                    return true;
                }
            } else if (random.nextInt(UNSTABLE_CURE_ROLL / lock) == UNSTABLE_CURE_MAGIC) {
                setNodeType(NodeType.NORMAL);
                nodeChange();
                return true;
            }
        }
        if (nodeModifier == NodeModifier.FADING
                && lock > 0
                && random.nextInt(FADING_CURE_ROLL / lock) == FADING_CURE_MAGIC) {
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
        NodeBiomeSpread.tick(serverLevel, this, pos, count);
    }

    private boolean handleTypeBehavior(ServerLevel serverLevel, BlockPos pos) {
        RandomSource random = serverLevel.getRandom();
        if (nodeType == NodeType.HUNGRY) {
            hunger.tick(serverLevel, pos, count);
            if (NodeHunger.eatDue(count)) {
                eatBlock(serverLevel, pos, random);
            }
            return false;
        }
        if (count % BEHAVIOR_INTERVAL != 0) {
            return false;
        }
        return NodeTypeBehavior.tick(this, serverLevel, pos, count, random);
    }

    public void applyPrimordialPearl(RandomSource random, boolean researched) {
        List<AspectInstance> existing = List.copyOf(aspectsBase.entries());
        for (AspectInstance entry : existing) {
            Holder<IAspect> aspect = entry.aspect();
            int base = entry.amount();
            if (!aspect.value().isPrimal()) {
                if (random.nextBoolean()) {
                    setBaseAmount(aspect, Math.max(0, base - 1));
                }
                continue;
            }
            int mutated = base - 2 + random.nextInt(researched ? 9 : 6);
            setBaseAmount(aspect, Math.max(0, mutated));
        }

        if (level instanceof ServerLevel serverLevel) {
            for (ResourceKey<IAspect> key : TTAspects.PRIMALS) {
                Holder<IAspect> aspect = Aspects.resolve(serverLevel, key);
                if (aspect == null) {
                    continue;
                }
                int base = aspectsBase.amountOf(aspect);
                int replacement = random.nextInt(researched ? 4 : 3);
                if (replacement > base) {
                    setBaseAmount(aspect, replacement);
                    if (aspects.amountOf(aspect) < replacement) {
                        aspects = aspects.add(aspect, 1);
                    }
                }
            }
        }

        if (nodeModifier == NodeModifier.FADING && random.nextBoolean()) {
            nodeModifier = NodeModifier.PALE;
        } else if (nodeModifier == NodeModifier.PALE && random.nextBoolean()) {
            nodeModifier = null;
        } else if (nodeModifier == null && random.nextInt(5) == 0) {
            nodeModifier = NodeModifier.BRIGHT;
        }
        nodeChange();
    }

    private void setBaseAmount(Holder<IAspect> aspect, int amount) {
        int current = aspectsBase.amountOf(aspect);
        if (amount > current) {
            aspectsBase = aspectsBase.add(aspect, amount - current);
        } else if (amount < current) {
            aspectsBase = aspectsBase.remove(aspect, current - amount);
        }
        int contained = aspects.amountOf(aspect);
        if (contained > amount) {
            aspects = aspects.remove(aspect, contained - amount);
        }
    }

    boolean erodePureNode(ServerLevel serverLevel, BlockPos pos, Holder<IAspect> aspect) {
        if (!aspectsBase.isEmpty()) {
            aspectsBase = reduce(aspectsBase, aspect, 1);
            int excess = aspects.amountOf(aspect) - aspectsBase.amountOf(aspect);
            if (excess > 0) {
                aspects = reduce(aspects, aspect, excess);
            }
            nodeChange();
        }
        if (aspectsBase.isEmpty()) {
            removeDepleted(serverLevel, pos);
            return true;
        }
        return false;
    }

    private void eatBlock(ServerLevel serverLevel, BlockPos pos, RandomSource random) {
        BlockPos target = hungryTarget(serverLevel, pos, random);
        if (target != null) {
            serverLevel.destroyBlock(target, true);
        }
    }

    private @Nullable BlockPos hungryTarget(Level level, BlockPos pos, RandomSource random) {
        return NodeHunger.findTarget(level, pos, nodeModifier, random);
    }

    public void burstIntoOrbs(ServerLevel serverLevel, BlockPos pos) {
        Map<ResourceKey<IAspect>, Integer> primals = WandChargingEvents.reduceToPrimals(aspects);
        RandomSource random = serverLevel.getRandom();
        for (Map.Entry<ResourceKey<IAspect>, Integer> entry : primals.entrySet()) {
            int remaining = entry.getValue();
            int maxPerOrb = Math.max(
                    ORB_BURST_MAX_PER_ASPECT,
                    (2 * remaining + ORB_BURST_MAX_ORBS_PER_ASPECT - 1) / ORB_BURST_MAX_ORBS_PER_ASPECT);
            int orbs = 0;
            while (remaining > 0) {
                orbs++;
                int value = orbs >= ORB_BURST_MAX_ORBS_PER_ASPECT
                        ? remaining
                        : Math.min(remaining, 1 + random.nextInt(maxPerOrb));
                remaining -= value;
                serverLevel.addFreshEntity(new EntityAspectOrb(
                        serverLevel, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, entry.getKey(), value));
            }
        }
        aspects = AspectList.EMPTY;
    }

    public boolean drainToWand(ServerLevel serverLevel, Player player, ItemStack wandStack, int useTicks) {
        return NodeWandTap.tap(this, serverLevel, player, wandStack, useTicks);
    }

    public void clientTick(Level clientLevel, BlockPos pos) {
        if (energized || nodeType != NodeType.HUNGRY || !allowTypeBehavior()) {
            return;
        }
        NodeHunger.predict(clientLevel, pos, nodeModifier);
    }

    @Override
    protected void saveAdditional(CompoundTag output, HolderLookup.Provider registries) {
        super.saveAdditional(output, registries);
        TTNbt.store(output, "Type", NodeType.CODEC, registries, nodeType);
        if (nodeModifier != null) {
            TTNbt.store(output, "Modifier", NodeModifier.CODEC, registries, nodeModifier);
        }
        TTNbt.store(output, "Aspects", AspectList.CODEC, registries, aspects);
        TTNbt.store(output, "AspectsBase", AspectList.CODEC, registries, aspectsBase);
        if (energized) {
            output.putBoolean("Energized", true);
        }
        if (aspectsBaseOriginal != null) {
            TTNbt.store(output, "AspectsBaseOriginal", AspectList.CODEC, registries, aspectsBaseOriginal);
        }
        if (drainPlayer != null) {
            TTNbt.store(output, "DrainPlayer", UUIDUtil.CODEC, registries, drainPlayer);
            output.putInt("DrainColor", drainColor);
        }
        if (jarringTicks > 0) {
            output.putInt("Jarring", jarringTicks);
        }
        if (taintReserve > 0.0F) {
            output.putFloat("TaintReserve", taintReserve);
        }
        if (taintPurifyTicks > 0) {
            output.putInt("TaintPurify", taintPurifyTicks);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag input, HolderLookup.Provider registries) {
        super.loadAdditional(input, registries);
        nodeType = TTNbt.read(input, "Type", NodeType.CODEC, registries).orElse(NodeType.NORMAL);
        energized = input.getBoolean("Energized");
        nodeModifier =
                TTNbt.read(input, "Modifier", NodeModifier.CODEC, registries).orElse(null);
        aspects = TTNbt.read(input, "Aspects", AspectList.CODEC, registries).orElse(AspectList.EMPTY);
        aspectsBase =
                TTNbt.read(input, "AspectsBase", AspectList.CODEC, registries).orElse(AspectList.EMPTY);
        aspectsBaseOriginal = TTNbt.read(input, "AspectsBaseOriginal", AspectList.CODEC, registries)
                .orElse(null);
        drainPlayer =
                TTNbt.read(input, "DrainPlayer", UUIDUtil.CODEC, registries).orElse(null);
        drainColor = input.contains("DrainColor") ? input.getInt("DrainColor") : 0xFFFFFF;
        jarringTicks = input.getInt("Jarring");
        taintReserve = Math.max(0.0F, input.getFloat("TaintReserve"));
        taintPurifyTicks = input.getInt("TaintPurify");
        regeneration = -1;
    }
}
