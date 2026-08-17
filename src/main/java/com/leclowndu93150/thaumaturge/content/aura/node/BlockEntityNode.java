package com.leclowndu93150.thaumaturge.content.aura.node;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.IAspectContainer;
import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.api.capability.KnowledgeAccess;
import com.leclowndu93150.thaumaturge.api.nodes.NodeModifier;
import com.leclowndu93150.thaumaturge.api.nodes.NodeType;
import com.leclowndu93150.thaumaturge.api.taint.TaintApi;
import com.leclowndu93150.thaumaturge.config.ThaumaturgeCommonConfig;
import com.leclowndu93150.thaumaturge.content.aspect.EntityAspects;
import com.leclowndu93150.thaumaturge.content.effect.Effects;
import com.leclowndu93150.thaumaturge.content.entity.EntityBrainyZombie;
import com.leclowndu93150.thaumaturge.content.wands.EntityAspectOrb;
import com.leclowndu93150.thaumaturge.content.wands.WandChargingEvents;
import com.leclowndu93150.thaumaturge.content.wands.WandEconomy;
import com.leclowndu93150.thaumaturge.content.wands.WandParts;
import com.leclowndu93150.thaumaturge.content.wands.WandVisHelper;
import com.leclowndu93150.thaumaturge.data.worldgen.biome.TCBiomes;
import com.leclowndu93150.thaumaturge.registry.TCBlockEntities;
import com.leclowndu93150.thaumaturge.registry.TCBlockTags;
import com.leclowndu93150.thaumaturge.registry.TCEntities;
import com.leclowndu93150.thaumaturge.registry.TCWandParts;
import com.leclowndu93150.thaumaturge.serialization.TCNbt;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.commands.FillBiomeCommand;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
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
    private static final int HUNGRY_REACH = 16;
    private static final double HUNGRY_RAY_START_OFFSET = 0.25;
    private static final double HUNGRY_PULL_RANGE = 15.0;
    private static final double HUNGRY_EAT_RANGE_SQ = 2.0;
    private static final float HUNGRY_MAX_HARDNESS = 5.0F;
    private static final int DARK_SPAWN_PLAYER_RANGE = 24;
    private static final int DARK_SPAWN_CAP = 3;
    private static final float PURE_FLUX_CLEANSE = 0.25F;
    private static final int NODE_DRAIN_INTERVAL = 5;
    private static final int ORB_BURST_MAX_PER_ASPECT = 10;
    private static final float ZAP_WIDTH = 0.3F;
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
    private static final int FADING_CURE_MAGIC = 69;
    private static final ResourceLocation RESEARCH_NODE_TAPPER_1 = TCIds.rl("node_tapper_1");
    private static final ResourceLocation RESEARCH_NODE_TAPPER_2 = TCIds.rl("node_tapper_2");
    private static final ResourceLocation RESEARCH_NODE_PRESERVE = TCIds.rl("node_preserve");
    private static final int MAX_DECOMPOSE_DEPTH = 8;

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
        this(TCBlockEntities.NODE.get(), pos, state);
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
    public boolean doesContainerAccept(Holder<IAspect> aspect) {
        return true;
    }

    public AspectList getAspectsBase() {
        return aspectsBase;
    }

    public int getNodeVisBase(Holder<IAspect> aspect) {
        return aspectsBase.amountOf(aspect);
    }

    @Override
    public int addToContainer(Holder<IAspect> aspect, int amount) {
        int capped = Math.min(amount, Math.max(0, aspectsBase.amountOf(aspect) - aspects.amountOf(aspect)));
        if (capped > 0) {
            aspects = aspects.add(aspect, capped);
            syncContents();
        }
        return amount - capped;
    }

    @Override
    public boolean takeFromContainer(Holder<IAspect> aspect, int amount) {
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
    public boolean doesContainerContainAmount(Holder<IAspect> aspect, int amount) {
        return aspects.amountOf(aspect) >= amount;
    }

    @Override
    public int containerContains(Holder<IAspect> aspect) {
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
            aspects = decomposeToPrimals(aspects);
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

    private static final int ENERGIZED_REFILL_INTERVAL = 20;
    private static final int ENERGIZED_REFILL_BASE_DIVISOR = 20;
    private static final int CV_BUFFER_CAP_SECONDS = 10;
    private static final int TICKS_PER_SECOND = 20;

    private final Map<ResourceLocation, Integer> cvAllowance = new HashMap<>();
    private final Map<ResourceLocation, Integer> cvCredit = new HashMap<>();

    private void accrueCentivis() {
        for (AspectInstance entry : aspectsBase.entries()) {
            ResourceLocation id = entry.aspect().unwrapKey().orElseThrow().location();
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
        ResourceLocation id = aspect.unwrapKey().orElseThrow().location();
        int stored = aspects.amountOf(aspect) * WandEconomy.CENTIVIS_PER_VIS + cvCredit.getOrDefault(id, 0);
        return Math.min(cvAllowance.getOrDefault(id, 0), stored);
    }

    public int drainCentivis(Holder<IAspect> aspect, int amount) {
        if (!energized || amount <= 0) {
            return 0;
        }
        ResourceLocation id = aspect.unwrapKey().orElseThrow().location();
        int allowance = Math.min(cvAllowance.getOrDefault(id, 0), amount);
        if (allowance <= 0) {
            return 0;
        }
        int credit = cvCredit.getOrDefault(id, 0);
        while (credit < allowance && takeFromContainer(aspect, 1)) {
            credit += WandEconomy.CENTIVIS_PER_VIS;
        }
        int taken = Math.min(allowance, credit);
        cvCredit.put(id, credit - taken);
        cvAllowance.put(id, cvAllowance.getOrDefault(id, 0) - taken);
        return taken;
    }

    public void serverTick(Level tickLevel, BlockPos pos) {
        if (!(tickLevel instanceof ServerLevel serverLevel)) {
            return;
        }
        if (energized) {
            boolean changed = false;
            if (tickLevel.getGameTime() % ENERGIZED_REFILL_INTERVAL == 0) {
                float visPerPoint = ThaumaturgeCommonConfig.ENERGIZED_NODE_VIS_PER_POINT
                        .get()
                        .floatValue();
                boolean fluxFed = nodeType == NodeType.TAINTED;
                for (AspectInstance entry : aspectsBase.entries()) {
                    int points = Math.max(1, entry.amount() / ENERGIZED_REFILL_BASE_DIVISOR);
                    for (int i = 0; i < points; i++) {
                        if (aspects.amountOf(entry.aspect()) >= entry.amount()) {
                            break;
                        }
                        float drained = fluxFed
                                ? AuraHelper.drainFlux(serverLevel, pos, visPerPoint, false)
                                : AuraHelper.drainVis(serverLevel, pos, visPerPoint, false);
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
        float drained = nodeType == NodeType.TAINTED
                ? AuraHelper.drainFlux(serverLevel, pos, FEED_RAW_VIS_PER_POINT, false)
                : AuraHelper.drainVis(serverLevel, pos, FEED_RAW_VIS_PER_POINT, false);
        if (drained >= FEED_RAW_VIS_PER_POINT - 0.01F) {
            addToContainer(target, 1);
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
            nodeType = NodeType.HUNGRY;
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
            nodeType = NodeType.TAINTED;
            nodeChange();
            return;
        }
        boolean healthy = flux < base * BRIGHTEN_FLUX_LIMIT
                && AuraHelper.getVis(serverLevel, pos) >= base * BRIGHTEN_FILL_FRACTION
                && serverLevel.getBiome(pos).is(TCBiomes.MAGICAL_FOREST);
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
            serverLevel.removeBlock(pos, false);
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

    private void checkLock(ServerLevel serverLevel, BlockPos pos) {
        if (count > 1 && count % BEHAVIOR_INTERVAL != 0) {
            return;
        }
        int oldLock = lock;
        lock = 0;
        BlockPos below = pos.below();
        if (!serverLevel.hasNeighborSignal(below)
                && serverLevel.getBlockState(below).getBlock() instanceof BlockNodeStabilizer stabilizer) {
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
        if (aspects.amountOf(stolen) < aspectsBase.amountOf(stolen) && other.takeFromContainer(stolen, 1)) {
            addToContainer(stolen, 1);
            moved = true;
        } else if (other.takeFromContainer(stolen, 1)) {
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
            Effects.arcBolt(serverLevel, Vec3.atCenterOf(otherPos))
                    .to(Vec3.atCenterOf(pos))
                    .width(ZAP_WIDTH)
                    .send();
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
                if (primal != null && takeFromContainer(primal, 1)) {
                    ResourceKey<IAspect> key = primal.unwrapKey().orElseThrow();
                    serverLevel.addFreshEntity(new EntityAspectOrb(
                            serverLevel, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, key, 1));
                    return true;
                }
            } else if (random.nextInt(UNSTABLE_CURE_ROLL / lock) == UNSTABLE_CURE_MAGIC) {
                nodeType = NodeType.NORMAL;
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
        if (count % BIOME_SPREAD_INTERVAL != 0 || !allowTypeBehavior() || serverLevel.dimension() != Level.OVERWORLD) {
            return;
        }
        if (nodeType == NodeType.DARK) {
            spreadBiomeColumn(serverLevel, pos, DARK_BIOME_SPREAD_RANGE, TCBiomes.EERIE);
        } else if (nodeType == NodeType.PURE && nearSilverwood(serverLevel, pos)) {
            spreadBiomeColumn(serverLevel, pos, PURE_BIOME_SPREAD_RANGE, TCBiomes.MAGICAL_FOREST);
        }
    }

    private static boolean nearSilverwood(ServerLevel serverLevel, BlockPos pos) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    cursor.setWithOffset(pos, dx, dy, dz);
                    if (serverLevel.getBlockState(cursor).is(TCBlockTags.SILVERWOOD_LOGS)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static void spreadBiomeColumn(
            ServerLevel serverLevel, BlockPos origin, int range, ResourceKey<Biome> biomeKey) {
        RandomSource random = serverLevel.getRandom();
        int x = origin.getX() + random.nextInt(range) - random.nextInt(range);
        int z = origin.getZ() + random.nextInt(range) - random.nextInt(range);
        BlockPos sample = new BlockPos(x, origin.getY(), z);
        if (!serverLevel.hasChunkAt(sample) || serverLevel.getBiome(sample).is(biomeKey)) {
            return;
        }
        Holder<Biome> biome =
                serverLevel.registryAccess().lookupOrThrow(Registries.BIOME).getOrThrow(biomeKey);
        FillBiomeCommand.fill(
                serverLevel,
                new BlockPos(x, serverLevel.getMinBuildHeight(), z),
                new BlockPos(x, serverLevel.getMaxBuildHeight(), z),
                biome);
    }

    private boolean handleTypeBehavior(ServerLevel serverLevel, BlockPos pos, boolean change) {
        if (count % BEHAVIOR_INTERVAL != 0 || !allowTypeBehavior()) {
            return change;
        }
        RandomSource random = serverLevel.getRandom();
        switch (nodeType) {
            case TAINTED -> {
                BlockPos target = pos.offset(
                        random.nextInt(5) - random.nextInt(5),
                        random.nextInt(5) - random.nextInt(5),
                        random.nextInt(5) - random.nextInt(5));
                if (random.nextBoolean()) {
                    TaintApi.spreadFibres(serverLevel, target, false);
                }
            }
            case PURE -> AuraHelper.drainFlux(serverLevel, pos, PURE_FLUX_CLEANSE, false);
            case DARK -> spawnDarkGuard(serverLevel, pos, random);
            case HUNGRY -> eatBlock(serverLevel, pos, random);
            default -> {}
        }
        return change;
    }

    private void spawnDarkGuard(ServerLevel serverLevel, BlockPos pos, RandomSource random) {
        if (!random.nextBoolean()) {
            return;
        }
        Player player = serverLevel.getNearestPlayer(
                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, DARK_SPAWN_PLAYER_RANGE, false);
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
        EntityBrainyZombie zombie = TCEntities.BRAINY_ZOMBIE.get().create(serverLevel);
        if (zombie == null) {
            return;
        }
        zombie.moveTo(x, y, z, random.nextFloat() * 360.0F, 0.0F);
        if (zombie.checkSpawnRules(serverLevel, MobSpawnType.EVENT)) {
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
        List<Entity> targets = serverLevel.getEntitiesOfClass(Entity.class, new AABB(pos).inflate(HUNGRY_PULL_RANGE));
        for (Entity target : targets) {
            if (target instanceof Player player && (player.isCreative() || player.isSpectator())) {
                continue;
            }
            if (!target.isAlive()) {
                continue;
            }
            double distanceSq = target.distanceToSqr(center);
            if (distanceSq < HUNGRY_EAT_RANGE_SQ) {
                target.hurt(serverLevel.damageSources().fellOutOfWorld(), 1.0F);
                if (!target.isAlive() && target instanceof LivingEntity living) {
                    devour(serverLevel, living);
                    change = true;
                }
            }
            Vec3 delta = center.subtract(target.position()).scale(1.0 / HUNGRY_PULL_RANGE);
            double length = delta.length();
            double power = 1.0 - length;
            if (power > 0.0) {
                power *= power;
                Vec3 pull = delta.normalize();
                target.setDeltaMovement(target.getDeltaMovement()
                        .add(pull.x * power * 0.15, pull.y * power * 0.25, pull.z * power * 0.15));
            }
        }
        return change;
    }

    private void devour(ServerLevel serverLevel, LivingEntity living) {
        AspectList entityAspects = EntityAspects.of(living);
        if (entityAspects.isEmpty()) {
            return;
        }
        Map<ResourceKey<IAspect>, Integer> primals = WandChargingEvents.reduceToPrimals(entityAspects);
        if (primals.isEmpty()) {
            return;
        }
        RandomSource random = serverLevel.getRandom();
        List<ResourceKey<IAspect>> keys = new ArrayList<>(primals.keySet());
        ResourceKey<IAspect> chosen = keys.get(random.nextInt(keys.size()));
        Holder<IAspect> holder =
                serverLevel.registryAccess().lookupOrThrow(IAspect.REGISTRY_KEY).getOrThrow(chosen);
        if (aspects.amountOf(holder) < aspectsBase.amountOf(holder)) {
            addToContainer(holder, 1);
        } else if (random.nextInt(1 + aspectsBase.amountOf(holder) * 2) < primals.get(chosen)) {
            aspectsBase = raiseBase(aspectsBase, holder, 1);
        }
        nodeChange();
    }

    private void eatBlock(ServerLevel serverLevel, BlockPos pos, RandomSource random) {
        int tx = pos.getX() + random.nextInt(HUNGRY_REACH) - random.nextInt(HUNGRY_REACH);
        int ty = pos.getY() + random.nextInt(HUNGRY_REACH) - random.nextInt(HUNGRY_REACH);
        int tz = pos.getZ() + random.nextInt(HUNGRY_REACH) - random.nextInt(HUNGRY_REACH);
        Vec3 to = new Vec3(tx + 0.5, ty + 0.5, tz + 0.5);
        BlockHitResult hit = hungryNodeClip(serverLevel, pos, to);
        if (hit.getType() != HitResult.Type.BLOCK) {
            return;
        }
        BlockPos target = hit.getBlockPos();
        if (target.equals(pos) || target.distSqr(pos) > 256.0) {
            return;
        }
        BlockState state = serverLevel.getBlockState(target);
        float hardness = state.getDestroySpeed(serverLevel, target);
        if (!state.isAir() && hardness >= 0.0F && hardness < HUNGRY_MAX_HARDNESS) {
            serverLevel.destroyBlock(target, true);
        }
    }

    /**
     * Clips outward from just beyond the node's outline. Starting at the block center makes the
     * node hit its own selection shape, preventing hungry nodes from ever reaching another block.
     */
    private static BlockHitResult hungryNodeClip(Level level, BlockPos pos, Vec3 to) {
        Vec3 center = Vec3.atCenterOf(pos);
        Vec3 direction = to.subtract(center);
        if (direction.lengthSqr() < 1.0E-7) {
            return BlockHitResult.miss(to, Direction.UP, pos);
        }
        Vec3 from = center.add(direction.normalize().scale(HUNGRY_RAY_START_OFFSET));
        return level.clip(
                new ClipContext(from, to, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, CollisionContext.empty()));
    }

    public void burstIntoOrbs(ServerLevel serverLevel, BlockPos pos) {
        Map<ResourceKey<IAspect>, Integer> primals = WandChargingEvents.reduceToPrimals(aspects);
        RandomSource random = serverLevel.getRandom();
        for (Map.Entry<ResourceKey<IAspect>, Integer> entry : primals.entrySet()) {
            int remaining = entry.getValue();
            while (remaining > 0) {
                int value = Math.min(remaining, 1 + random.nextInt(ORB_BURST_MAX_PER_ASPECT));
                remaining -= value;
                serverLevel.addFreshEntity(new EntityAspectOrb(
                        serverLevel, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, entry.getKey(), value));
            }
        }
        aspects = AspectList.EMPTY;
    }

    public boolean drainToWand(ServerLevel serverLevel, Player player, ItemStack wandStack, int useTicks) {
        if (energized) {
            return false;
        }
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
        boolean starterWand = parts.rod() == TCWandParts.ROD_WOOD.get() && parts.cap() == TCWandParts.CAP_IRON.get();
        boolean preserve = !player.isShiftKeyDown()
                && !starterWand
                && KnowledgeAccess.of(player).isResearchKnown(RESEARCH_NODE_PRESERVE);
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
        takeFromContainer(chosen, moved);
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
        int tx = pos.getX() + random.nextInt(HUNGRY_REACH) - random.nextInt(HUNGRY_REACH);
        int ty = pos.getY() + random.nextInt(HUNGRY_REACH) - random.nextInt(HUNGRY_REACH);
        int tz = pos.getZ() + random.nextInt(HUNGRY_REACH) - random.nextInt(HUNGRY_REACH);
        Vec3 from = Vec3.atCenterOf(pos);
        Vec3 to = new Vec3(tx + 0.5, ty + 0.5, tz + 0.5);
        BlockHitResult hit = hungryNodeClip(clientLevel, pos, to);
        if (hit.getType() != HitResult.Type.BLOCK) {
            return;
        }
        BlockPos target = hit.getBlockPos();
        if (target.equals(pos) || target.distSqr(pos) > 256.0) {
            return;
        }
        BlockState state = clientLevel.getBlockState(target);
        if (state.isAir()) {
            return;
        }
        Vec3 pull = from.subtract(Vec3.atCenterOf(target)).normalize().scale(0.3);
        for (int i = 0; i < 3; i++) {
            clientLevel.addParticle(
                    new BlockParticleOption(ParticleTypes.BLOCK, state),
                    target.getX() + random.nextFloat(),
                    target.getY() + random.nextFloat(),
                    target.getZ() + random.nextFloat(),
                    pull.x,
                    pull.y + 0.05,
                    pull.z);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag output, HolderLookup.Provider registries) {
        super.saveAdditional(output, registries);
        TCNbt.store(output, "Type", NodeType.CODEC, registries, nodeType);
        if (nodeModifier != null) {
            TCNbt.store(output, "Modifier", NodeModifier.CODEC, registries, nodeModifier);
        }
        TCNbt.store(output, "Aspects", AspectList.CODEC, registries, aspects);
        TCNbt.store(output, "AspectsBase", AspectList.CODEC, registries, aspectsBase);
        if (energized) {
            output.putBoolean("Energized", true);
        }
        if (aspectsBaseOriginal != null) {
            TCNbt.store(output, "AspectsBaseOriginal", AspectList.CODEC, registries, aspectsBaseOriginal);
        }
        if (drainPlayer != null) {
            TCNbt.store(output, "DrainPlayer", UUIDUtil.CODEC, registries, drainPlayer);
            output.putInt("DrainColor", drainColor);
        }
        if (jarringTicks > 0) {
            output.putInt("Jarring", jarringTicks);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag input, HolderLookup.Provider registries) {
        super.loadAdditional(input, registries);
        nodeType = TCNbt.read(input, "Type", NodeType.CODEC, registries).orElse(NodeType.NORMAL);
        energized = input.getBoolean("Energized");
        nodeModifier =
                TCNbt.read(input, "Modifier", NodeModifier.CODEC, registries).orElse(null);
        aspects = TCNbt.read(input, "Aspects", AspectList.CODEC, registries).orElse(AspectList.EMPTY);
        aspectsBase =
                TCNbt.read(input, "AspectsBase", AspectList.CODEC, registries).orElse(AspectList.EMPTY);
        aspectsBaseOriginal = TCNbt.read(input, "AspectsBaseOriginal", AspectList.CODEC, registries)
                .orElse(null);
        drainPlayer =
                TCNbt.read(input, "DrainPlayer", UUIDUtil.CODEC, registries).orElse(null);
        drainColor = input.contains("DrainColor") ? input.getInt("DrainColor") : 0xFFFFFF;
        jarringTicks = input.getInt("Jarring");
        regeneration = -1;
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag nbt = super.getUpdateTag(registries);
        CompoundTag out = new CompoundTag();
        saveAdditional(out, registries);
        nbt.merge(out);
        return nbt;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
