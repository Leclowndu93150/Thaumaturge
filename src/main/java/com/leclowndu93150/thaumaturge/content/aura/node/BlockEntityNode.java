package com.leclowndu93150.thaumaturge.content.aura.node;

import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.Aspects;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.IAspectContainer;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.api.aura.IVisRelaySource;
import com.leclowndu93150.thaumaturge.api.nodes.NodeModifier;
import com.leclowndu93150.thaumaturge.api.nodes.NodeType;
import com.leclowndu93150.thaumaturge.api.nodes.NodeSnapshot;
import com.leclowndu93150.thaumaturge.api.nodes.NodeChange;
import com.leclowndu93150.thaumaturge.api.nodes.NodeBehaviorState;
import com.leclowndu93150.thaumaturge.api.nodes.NodeBehavior;
import com.leclowndu93150.thaumaturge.api.nodes.NodeBehaviorContext;
import com.leclowndu93150.thaumaturge.api.nodes.NodeOperationContext;
import com.leclowndu93150.thaumaturge.api.nodes.NodeResult;
import com.leclowndu93150.thaumaturge.config.ThaumaturgeCommonConfig;
import com.leclowndu93150.thaumaturge.content.blockentity.AbstractSyncedBlockEntity;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jspecify.annotations.Nullable;

public class BlockEntityNode extends AbstractSyncedBlockEntity implements IAspectContainer {
    private static final AtomicLong REVISIONS = new AtomicLong();
    private UUID identity = UUID.randomUUID();
    private boolean legacyIdentity;
    private boolean persistedIdentity;
    private long revision = REVISIONS.incrementAndGet();
    private @Nullable NodeBehaviorState behavior;
    private boolean pendingUpdate;
    private final MutationJournal mutationJournal = new MutationJournal();
    private final NodeBehaviorRunner behaviorRunner = new NodeBehaviorRunner();
    private boolean invokingBehavior;
    private boolean transferReady = true;
    static final int LOCK_NONE = 0;
    static final int LOCK_BASIC = 1;
    static final int LOCK_ADVANCED = 2;
    static final int UNSET = -1;

    private static final String KEY_TYPE = "Type";
    private static final String KEY_MODIFIER = "Modifier";
    private static final String KEY_ASPECTS = "Aspects";
    private static final String KEY_ASPECTS_BASE = "AspectsBase";
    private static final String KEY_ENERGIZED = "Energized";
    private static final String KEY_ASPECTS_BASE_ORIGINAL = "AspectsBaseOriginal";
    private static final String KEY_DRAIN_PLAYER = "DrainPlayer";
    private static final String KEY_DRAIN_COLOR = "DrainColor";
    private static final String KEY_JARRING = "Jarring";
    private static final String KEY_BOOTSTRAP = "NaturalTaintBootstrap";
    private static final int DEFAULT_DRAIN_COLOR = 0xFFFFFF;
    private static final int DEFAULT_CLIENT_CHANNEL = 255;
    private static final int LOCK_REFRESH_INTERVAL = 50;
    private static final int BASIC_LOCK_FEED_FACTOR = 2;
    private static final int ADVANCED_LOCK_FEED_FACTOR = 20;
    private static final int TYPE_BEHAVIOR_INTERVAL = 50;
    private static final int DRAIN_LINGER_TICKS = 12;
    private static final int CONTENT_AVERAGE_DIVISOR = 2;

    protected AspectList held = AspectList.EMPTY;
    protected AspectList aspectsBase = AspectList.EMPTY;
    protected boolean energized;
    public int clientDrainRed = DEFAULT_CLIENT_CHANNEL;
    public int clientDrainGreen = DEFAULT_CLIENT_CHANNEL;
    public int clientDrainBlue = DEFAULT_CLIENT_CHANNEL;

    int tickCounter;
    int lock;
    int refillWait;
    int starvation;
    private int refillInterval = UNSET;
    private int drainLinger;
    private NodeType type = NodeType.NORMAL;
    private @Nullable NodeModifier modifier;
    private @Nullable AspectList aspectsBaseOriginal;
    private @Nullable UUID drainPlayer;
    private int drainColor = DEFAULT_DRAIN_COLOR;
    private int jarring;
    private boolean naturalTaintBootstrap;
    private boolean inTick;
    private boolean tickDirty;
    private final NodeCentivis centivis = new NodeCentivis(this);
    private final NodeHunger hunger = new NodeHunger(this);
    private final IVisRelaySource relay = new NodeVisRelaySource(this);

    public BlockEntityNode(BlockPos pos, BlockState state) {
        this(TTBlockEntities.NODE.get(), pos, state);
    }

    protected BlockEntityNode(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState state) {
        super(blockEntityType, pos, state);
    }

    public @Nullable UUID getDrainPlayer() {
        return drainPlayer;
    }

    public int getDrainColor() {
        return drainColor;
    }

    public void applyNodeData(NodeData data) {
        type = data.type();
        modifier = data.modifier().orElse(null);
        held = data.aspects();
        aspectsBase = data.aspectsBase();
        refreshIndex();
        invalidateRefill();
    }

    public NodeType kind() {
        return type;
    }

    public NodeSnapshot snapshot() {
        return new NodeSnapshot(identity, revision, held, aspectsBase, Optional.ofNullable(aspectsBaseOriginal), type, Optional.ofNullable(modifier), energized, lock != LOCK_NONE,
                isJarring() || pendingUpdate || !transferReady, Optional.ofNullable(behavior));
    }

    public boolean hasLegacyIdentity() {
        return legacyIdentity;
    }

    public boolean hasPersistedIdentity() {
        return persistedIdentity;
    }

    boolean availableForApi() {
        return transferReady;
    }

    void resumeTransfer() {
        transferReady = true;
        refreshIndex();
    }

    void suspendTransfer() {
        transferReady = false;
        if (level instanceof ServerLevel serverLevel) {
            NodeLocationIndex.get(serverLevel).remove(worldPosition);
        }
    }

    public void restoreSnapshot(NodeSnapshot snapshot) {
        restore(snapshot);
        persistedIdentity = false;
        behaviorRunner.reset();
        refreshIndex();
        invalidateRefill();
    }

    private void restore(NodeSnapshot snapshot) {
        identity = snapshot.identity();
        legacyIdentity = false;
        held = snapshot.current();
        aspectsBase = snapshot.base();
        aspectsBaseOriginal = snapshot.originalBase().orElse(null);
        type = snapshot.type();
        modifier = snapshot.modifier().orElse(null);
        energized = snapshot.energized();
        behavior = snapshot.behavior().orElse(null);
    }

    void update(NodeChange change, TransactionContext transaction) {
        mutationJournal.updateSnapshots(transaction);
        pendingUpdate = true;
        held = change.current();
        aspectsBase = change.base();
        type = change.type();
        modifier = change.modifier().orElse(null);
        behavior = change.behavior().orElse(null);
        revision = REVISIONS.incrementAndGet();
    }

    private final class MutationJournal extends SnapshotJournal<NodeSnapshot> {
        @Override
        protected NodeSnapshot createSnapshot() {
            return snapshot();
        }

        @Override
        protected void revertToSnapshot(NodeSnapshot snapshot) {
            restore(snapshot);
            revision = snapshot.revision();
            pendingUpdate = false;
        }

        @Override
        protected void onRootCommit(NodeSnapshot original) {
            pendingUpdate = false;
            refreshIndex();
            invalidateRefill();
            if (!invokingBehavior && !original.behavior().equals(Optional.ofNullable(behavior))) {
                behaviorRunner.reset();
                boolean sameType = original.behavior().isPresent() && behavior != null && original.behavior().get().id().equals(behavior.id())
                        && original.behavior().get().version() == behavior.version();
                if (!sameType) {
                    notifyDetached(original);
                    behaviorEvent(NodeBehavior.Event.ATTACH);
                }
            }
        }
    }

    private void notifyDetached(NodeSnapshot original) {
        if (original.behavior().isEmpty() || !(level instanceof ServerLevel serverLevel)) {
            return;
        }
        new NodeBehaviorRunner().invoke(new NodeBehaviorContext() {
            @Override
            public NodeSnapshot snapshot() {
                return original;
            }

            @Override
            public RandomSource random() {
                return serverLevel.getRandom();
            }

            @Override
            public NodeResult update(NodeChange change) {
                return NodeResult.UNSUPPORTED;
            }
        }, original.behavior().get(), NodeBehavior.Event.DETACH);
    }

    public void behaviorEvent(NodeBehavior.Event event) {
        if (behavior == null || invokingBehavior || pendingUpdate || !(level instanceof ServerLevel serverLevel) || NodeOperations.find(serverLevel, worldPosition) != this) {
            return;
        }
        NodeSnapshot before = snapshot();
        NodeChange[] staged = new NodeChange[1];
        invokingBehavior = true;
        try {
            NodeBehaviorRunner.Result result = behaviorRunner.invoke(new NodeBehaviorContext() {
                @Override
                public NodeSnapshot snapshot() {
                    return before;
                }

                @Override
                public RandomSource random() {
                    return serverLevel.getRandom();
                }

                @Override
                public NodeResult update(NodeChange change) {
                    if (event == NodeBehavior.Event.UNLOAD || event == NodeBehavior.Event.DETACH || event == NodeBehavior.Event.CAPTURE) {
                        return NodeResult.UNSUPPORTED;
                    }
                    if (!NodeOperations.valid(change)) {
                        return NodeResult.INVALID;
                    }
                    if (staged[0] != null) {
                        return NodeResult.BUSY;
                    }
                    staged[0] = change;
                    return NodeResult.SUCCESS;
                }
            }, behavior, event);
            if (!result.success() || event == NodeBehavior.Event.UNLOAD || event == NodeBehavior.Event.CAPTURE || (staged[0] == null && result.state().equals(behavior))) {
                return;
            }
            NodeChange change = staged[0] == null ? NodeChange.of(before) : staged[0];
            change = new NodeChange(change.current(), change.base(), change.type(), change.modifier(), Optional.of(result.state()));
            NodeOperationContext context = NodeOperationContext.server(serverLevel, worldPosition, behavior.id());
            try (Transaction transaction = Transaction.openRoot()) {
                if (NodeOperations.update(context, before.identity(), before.revision(), change, transaction) == NodeResult.SUCCESS) {
                    transaction.commit();
                }
            }
        } finally {
            invokingBehavior = false;
        }
    }

    public void reclassify(NodeType nodeType) {
        type = nodeType;
        refreshIndex();
        changed();
    }

    public @Nullable NodeModifier trait() {
        return modifier;
    }

    public void assignTrait(@Nullable NodeModifier nodeModifier) {
        modifier = nodeModifier;
        refillInterval = UNSET;
        changed();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        transferReady = NodeTransferOperations.reconcile(this);
        if (!transferReady) {
            return;
        }
        refreshIndex();
        behaviorRunner.reset();
        behaviorEvent(NodeBehavior.Event.LOAD);
    }

    @Override
    public void setRemoved() {
        behaviorEvent(NodeBehavior.Event.UNLOAD);
        super.setRemoved();
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        if (level instanceof ServerLevel serverLevel) {
            NodeLocationIndex.get(serverLevel).remove(pos);
        }
    }

    private void refreshIndex() {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        BlockState state = getBlockState();
        if (state.is(TTBlocks.NODE.get()) || state.is(TTBlocks.SILVERWOOD_NODE_LOG.get())) {
            NodeLocationIndex.get(serverLevel).register(worldPosition, type);
        }
    }

    @Override
    public AspectList getAspects() {
        return held;
    }

    @Override
    public void setAspects(AspectList list) {
        held = list;
        aspectsBase = list;
        invalidateRefill();
    }

    @Override
    public boolean accepts(Holder<IAspect> aspect) {
        return transferReady;
    }

    public AspectList capacity() {
        return aspectsBase;
    }

    public int capacityOf(Holder<IAspect> aspect) {
        return aspectsBase.amountOf(aspect);
    }

    @Override
    public int fill(Holder<IAspect> aspect, int amount) {
        if (amount <= 0) {
            return 0;
        }
        if (!transferReady) {
            return amount;
        }
        int room = Math.max(0, aspectsBase.amountOf(aspect) - held.amountOf(aspect));
        if (room == 0) {
            return amount;
        }
        int added = Math.min(room, amount);
        held = held.add(aspect, added);
        changed();
        return amount - added;
    }

    @Override
    public boolean drain(Holder<IAspect> aspect, int amount) {
        if (!transferReady) {
            return false;
        }
        if (held.amountOf(aspect) < amount) {
            return false;
        }
        if (amount > 0) {
            held = held.reduce(aspect, amount);
            changed();
        }
        return true;
    }

    @Override
    public boolean holds(Holder<IAspect> aspect, int amount) {
        return held.amountOf(aspect) >= amount;
    }

    @Override
    public int amountOf(Holder<IAspect> aspect) {
        return held.amountOf(aspect);
    }

    public boolean isEnergized() {
        return energized;
    }

    public void setEnergized(boolean value) {
        if (!transferReady || value == energized) {
            return;
        }
        energized = value;
        centivis.clear();
        if (value) {
            aspectsBaseOriginal = aspectsBase;
            aspectsBase = NodeRules.toPrimals(aspectsBase);
            held = aspectsBase;
        } else if (aspectsBaseOriginal != null) {
            aspectsBase = aspectsBaseOriginal;
            aspectsBaseOriginal = null;
        }
        changed();
    }

    public @Nullable AspectList getAspectsBaseOriginal() {
        return aspectsBaseOriginal;
    }

    public boolean isJarring() {
        return jarring > 0;
    }

    public void beginJarring(int ticks) {
        if (!transferReady) {
            return;
        }
        jarring = ticks;
        changed();
    }

    public void applyPrimordialPearl(RandomSource random, boolean researched) {
        List<Holder<IAspect>> primals = new ArrayList<>();
        for (ResourceKey<IAspect> key : TTAspects.PRIMALS) {
            Holder<IAspect> primal = Aspects.resolve(level, key);
            if (primal != null) {
                primals.add(primal);
            }
        }
        NodeRules.Contents result = NodeRules.pearl(new NodeRules.Contents(held, aspectsBase), primals, random, researched);
        held = result.contained();
        aspectsBase = result.base();
        modifier = NodeRules.pearlModifier(modifier, random);
        invalidateRefill();
    }

    public void invalidateRefill() {
        refillInterval = UNSET;
        changed();
    }

    public int centivisRate(Holder<IAspect> aspect) {
        return energized ? aspectsBase.amountOf(aspect) : 0;
    }

    public int availableCentivis(Holder<IAspect> aspect) {
        return energized ? centivis.available(aspect) : 0;
    }

    public IVisRelaySource relaySource() {
        return relay;
    }

    public int drainCentivis(Holder<IAspect> aspect, int amount, TransactionContext transaction) {
        return transferReady && energized ? centivis.drain(aspect, amount, transaction) : 0;
    }

    void changed() {
        revision = REVISIONS.incrementAndGet();
        if (inTick) {
            tickDirty = true;
        } else {
            setChangedAndSync();
        }
    }

    int refillInterval() {
        if (refillInterval == UNSET) {
            refillInterval = NodeRules.baseRefillInterval(modifier) * feedingIntervalFactor();
        }
        return refillInterval;
    }

    int averageContent() {
        return (held.totalAmount() + aspectsBase.totalAmount()) / CONTENT_AVERAGE_DIVISOR;
    }

    void clearContained() {
        held = AspectList.EMPTY;
        invalidateRefill();
    }

    void removeDepleted(ServerLevel serverLevel, BlockPos pos) {
        BlockState state = getBlockState();
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

    public void serverTick(Level tickLevel, BlockPos pos) {
        if (!(tickLevel instanceof ServerLevel serverLevel)) {
            return;
        }
        if (pendingUpdate || !transferReady) {
            return;
        }
        inTick = true;
        try {
            tickServer(serverLevel, pos);
        } finally {
            inTick = false;
        }
        if (tickDirty) {
            tickDirty = false;
            revision = REVISIONS.incrementAndGet();
            setChangedAndSync();
        }
        behaviorEvent(NodeBehavior.Event.TICK);
    }

    private void tickServer(ServerLevel serverLevel, BlockPos pos) {
        consumeBootstrapFlag(serverLevel, pos);
        if (energized) {
            tickEnergized(serverLevel, pos);
            tickDrainLinger();
            return;
        }
        if (jarring > 0) {
            tickJarring(serverLevel, pos);
            return;
        }
        tickDrainLinger();
        tickCounter++;
        RandomSource random = serverLevel.getRandom();
        refreshLock(serverLevel, pos);
        NodeUpkeep.refill(this, serverLevel, pos, random);
        NodeUpkeep.discharge(this, serverLevel, pos, random);
        if (NodeUpkeep.decay(this, serverLevel, pos, random)) {
            return;
        }
        NodeUpkeep.stability(this, serverLevel, pos, random);
        if (allowTypeBehavior() && !runTypeBehavior(serverLevel, pos, random)) {
            NodeBiomeSpread.tick(serverLevel, this, pos, tickCounter);
        }
    }

    private void consumeBootstrapFlag(ServerLevel serverLevel, BlockPos pos) {
        if (!naturalTaintBootstrap) {
            return;
        }
        naturalTaintBootstrap = false;
        tickDirty = true;
        boolean shouldRun = type == NodeType.TAINTED && !ThaumaturgeCommonConfig.WUSS_MODE.get();
        if (shouldRun) {
            NodeBootstrap.run(serverLevel, pos);
        }
    }

    private void tickJarring(ServerLevel serverLevel, BlockPos pos) {
        tickDirty = true;
        if (--jarring == 0) {
            NodeJarRitual.completeJar(serverLevel, pos, this);
        }
    }

    private boolean runTypeBehavior(ServerLevel serverLevel, BlockPos pos, RandomSource random) {
        if (type == NodeType.HUNGRY) {
            hunger.tick(serverLevel, pos, tickCounter);
            if (NodeHunger.eatDue(tickCounter)) {
                eatBlock(serverLevel, pos, random);
            }
            return false;
        }
        if (tickCounter % TYPE_BEHAVIOR_INTERVAL != 0) {
            return false;
        }
        return NodeTypeBehavior.tick(this, serverLevel, pos, random);
    }

    private @Nullable BlockPos hungryTarget(Level targetLevel, BlockPos pos, RandomSource random) {
        return NodeHunger.findTarget(targetLevel, pos, modifier, random);
    }

    private void eatBlock(ServerLevel serverLevel, BlockPos pos, RandomSource random) {
        BlockPos target = hungryTarget(serverLevel, pos, random);
        if (target != null) {
            serverLevel.destroyBlock(target, true);
        }
    }

    private void tickDrainLinger() {
        if (drainLinger > 0 && --drainLinger == 0) {
            drainPlayer = null;
            changed();
        }
    }

    private void refreshLock(ServerLevel serverLevel, BlockPos pos) {
        boolean due = tickCounter == 1 || tickCounter % LOCK_REFRESH_INTERVAL == 0;
        if (!due) {
            return;
        }
        BlockPos below = pos.below();
        if (!serverLevel.hasChunkAt(below) || !NodeBootstrap.columnsGenerated(serverLevel, pos)) {
            return;
        }
        int next = lockBeneath(serverLevel, below);
        if (next == lock) {
            return;
        }
        lock = next;
        refillInterval = UNSET;
        changed();
    }

    private static int lockBeneath(ServerLevel serverLevel, BlockPos below) {
        if (!(serverLevel.getBlockState(below).getBlock() instanceof BlockNodeStabilizer stabilizer)) {
            return LOCK_NONE;
        }
        if (serverLevel.hasNeighborSignal(below)) {
            return LOCK_NONE;
        }
        return stabilizer.isAdvanced() ? LOCK_ADVANCED : LOCK_BASIC;
    }

    protected int feedingIntervalFactor() {
        return switch (lock) {
            case LOCK_ADVANCED -> ADVANCED_LOCK_FEED_FACTOR;
            case LOCK_BASIC -> BASIC_LOCK_FEED_FACTOR;
            default -> 1;
        };
    }

    public int stabilizerTier() {
        return lock;
    }

    protected boolean allowDischarge() {
        return true;
    }

    protected boolean allowTypeBehavior() {
        return true;
    }

    public void markNaturalTaintBootstrap() {
        naturalTaintBootstrap = true;
        setChanged();
    }

    private void tickEnergized(ServerLevel serverLevel, BlockPos pos) {
        if (NodeEnergy.refillDue(serverLevel) && NodeEnergy.refill(this, serverLevel, pos)) {
            tickDirty = true;
        }
        centivis.accrue();
    }

    public void burstIntoOrbs(ServerLevel serverLevel, BlockPos pos) {
        if (!transferReady) {
            return;
        }
        NodeOrbs.burst(serverLevel, pos, held);
        held = AspectList.EMPTY;
        changed();
    }

    public boolean drainToWand(ServerLevel serverLevel, Player player, ItemStack wand, int remainingUse) {
        return transferReady && NodeWandTap.tap(this, serverLevel, player, wand, remainingUse);
    }

    void recordDrain(UUID player, int color) {
        drainPlayer = player;
        drainColor = color;
        drainLinger = DRAIN_LINGER_TICKS;
        changed();
    }

    public void clientTick(Level tickLevel, BlockPos pos) {
        if (!tickLevel.isClientSide() || energized || type != NodeType.HUNGRY || !allowTypeBehavior()) {
            return;
        }
        NodeHunger.predict(tickLevel, pos, modifier);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.store("Identity", UUIDUtil.CODEC, identity);
        if (behavior != null) {
            output.store("Behavior", NodeBehaviorState.CODEC, behavior);
        }
        writeNodeData(output);
    }

    @Override
    protected void writeClientData(ValueOutput output) {
        writeNodeData(output);
    }

    private void writeNodeData(ValueOutput output) {
        output.store(KEY_TYPE, NodeType.CODEC, type);
        if (modifier != null) {
            output.store(KEY_MODIFIER, NodeModifier.CODEC, modifier);
        }
        output.store(KEY_ASPECTS, AspectList.CODEC, held);
        output.store(KEY_ASPECTS_BASE, AspectList.CODEC, aspectsBase);
        if (energized) {
            output.putBoolean(KEY_ENERGIZED, true);
        }
        if (aspectsBaseOriginal != null) {
            output.store(KEY_ASPECTS_BASE_ORIGINAL, AspectList.CODEC, aspectsBaseOriginal);
        }
        if (drainPlayer != null) {
            output.store(KEY_DRAIN_PLAYER, UUIDUtil.CODEC, drainPlayer);
            output.putInt(KEY_DRAIN_COLOR, drainColor);
        }
        if (jarring > 0) {
            output.putInt(KEY_JARRING, jarring);
        }
        if (naturalTaintBootstrap) {
            output.putBoolean(KEY_BOOTSTRAP, true);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        Optional<UUID> savedIdentity = input.read("Identity", UUIDUtil.CODEC);
        legacyIdentity = savedIdentity.isEmpty();
        persistedIdentity = savedIdentity.isPresent();
        identity = savedIdentity.orElseGet(UUID::randomUUID);
        revision = REVISIONS.incrementAndGet();
        behavior = input.read("Behavior", NodeBehaviorState.CODEC).orElse(null);
        type = input.read(KEY_TYPE, NodeType.CODEC).orElse(NodeType.NORMAL);
        modifier = input.read(KEY_MODIFIER, NodeModifier.CODEC).orElse(null);
        held = input.read(KEY_ASPECTS, AspectList.CODEC).orElse(AspectList.EMPTY);
        aspectsBase = input.read(KEY_ASPECTS_BASE, AspectList.CODEC).orElse(AspectList.EMPTY);
        energized = input.getBooleanOr(KEY_ENERGIZED, false);
        aspectsBaseOriginal = input.read(KEY_ASPECTS_BASE_ORIGINAL, AspectList.CODEC).orElse(null);
        drainPlayer = input.read(KEY_DRAIN_PLAYER, UUIDUtil.CODEC).orElse(null);
        drainColor = input.getIntOr(KEY_DRAIN_COLOR, DEFAULT_DRAIN_COLOR);
        jarring = input.getIntOr(KEY_JARRING, 0);
        naturalTaintBootstrap = input.getBooleanOr(KEY_BOOTSTRAP, false);
        refillInterval = UNSET;
    }
}
