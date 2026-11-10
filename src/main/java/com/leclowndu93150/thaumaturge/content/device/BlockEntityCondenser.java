package com.leclowndu93150.thaumaturge.content.device;

import com.leclowndu93150.thaumaturge.api.aspect.Aspects;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaAccess;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaPull;
import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaConduit;
import com.leclowndu93150.thaumaturge.content.effect.Effects;
import com.leclowndu93150.thaumaturge.content.world.LoadedChunks;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class BlockEntityCondenser extends BlockEntity implements IEssentiaConduit {
    private static final int CAPACITY = 100;
    private static final int INTAKE_INTERVAL = 5;
    private static final int RECHECK_INTERVAL = 100;
    private static final int SUCTION = 128;
    private static final int DIRTY_CHANCE = 50;
    private static final float MAX_SCORE = 40.0F;
    private static final int CELL_VALUE = 100;
    private static final int PENALTY_PER_NEIGHBOUR = 15;
    private static final float VALUE_DIVISOR = 100.0F;
    private static final int ROOT_VALUE = CELL_VALUE - PENALTY_PER_NEIGHBOUR;
    private static final float BASE_INTERVAL = 600.0F;
    private static final float INTERVAL_PER_SCORE = 15.0F;
    private static final int MIN_INTERVAL = 5;
    private static final int BASE_COST = 4;
    private static final float FLUX_PER_CONVERSION = 1.0F;
    private static final float UNSCANNED = -1.0F;
    private static final int ABSORB_AMOUNT = 1;
    private static final int ENTRY_CLOG_WEIGHT = 1;
    private static final int CELL_CLOG_WEIGHT = 8;
    private static final int SPARK_PACE = 20;
    private static final double SPARK_SPREAD = 0.4;
    private static final float SPARK_SIZE = 0.6F;
    private static final float SPARK_COLOR_FLOOR = 0.55F;
    private static final String ESSENTIA_KEY = "essentia";
    private static final String FLUX_KEY = "flux";
    private static final Direction[] DIRECTIONS = Direction.values();
    private static final Direction[] HORIZONTALS =
            Direction.Plane.HORIZONTAL.stream().toArray(Direction[]::new);

    private int essentia;
    private int flux;
    private int ticks;
    private float score = UNSCANNED;
    private int interval;
    private int cost;
    private List<BlockPos> dirtyCandidates = List.of();

    public BlockEntityCondenser(BlockPos pos, BlockState state) {
        super(TTBlockEntities.CONDENSER.get(), pos, state);
    }

    public int interval() {
        return interval;
    }

    public int cost() {
        return cost;
    }

    public void triggerCheck() {
        if (level != null && !level.isClientSide()) {
            rescan(level);
        }
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BlockEntityCondenser condenser) {
        condenser.work(level, state);
    }

    private void work(Level level, BlockState state) {
        ticks++;
        if (score == UNSCANNED || ticks % RECHECK_INTERVAL == 0) {
            rescan(level);
        }
        if (!state.getValue(BlockStateProperties.ENABLED) || score <= 0.0F) {
            return;
        }
        if (ticks % INTAKE_INTERVAL == 0 && essentia < CAPACITY) {
            draw(level);
        }
        if (interval > 0 && ticks % interval == 0) {
            condense(level);
        }
        if (essentia > 0 && level instanceof ServerLevel serverLevel) {
            sparkle(serverLevel);
        }
    }

    private void draw(Level level) {
        List<IEssentiaConduit> sources = new ArrayList<>(HORIZONTALS.length);
        for (Direction side : HORIZONTALS) {
            IEssentiaConduit source = neighbour(level, side);
            if (source != null && !source.releasesOn(side.getOpposite())) {
                return;
            }
            sources.add(source);
        }
        for (int slot = 0; slot < HORIZONTALS.length && essentia < CAPACITY; slot++) {
            IEssentiaConduit source = sources.get(slot);
            if (source != null) {
                drawFrom(level, source, HORIZONTALS[slot]);
            }
        }
    }

    private void drawFrom(Level level, IEssentiaConduit source, Direction side) {
        Direction face = side.getOpposite();
        if (!isWilling(source, face, pullStrength(side))) {
            return;
        }
        Holder<IAspect> offered = source.heldAspect(face);
        if (offered == null) {
            return;
        }
        if (offered.is(TTAspects.VITIUM)) {
            makeDirty(level);
            return;
        }
        absorb(source.extract(offered, ABSORB_AMOUNT, face, false));
    }

    private void condense(Level level) {
        if (essentia < cost || flux >= CAPACITY || AuraHelper.getFlux(level, worldPosition) < FLUX_PER_CONVERSION) {
            return;
        }
        AuraHelper.drainFlux(level, worldPosition, FLUX_PER_CONVERSION, false);
        essentia -= cost;
        flux++;
        setChanged();
        if (level.getRandom().nextInt(DIRTY_CHANCE) == 0) {
            makeDirty(level);
        }
    }

    private void makeDirty(Level level) {
        BlockPos victim = pickClogTarget(level.getRandom());
        if (victim == null) {
            return;
        }
        BlockState clean = level.getBlockState(victim);
        level.setBlock(victim, TTBlocks.CONDENSER_LATTICE_DIRTY.get().withPropertiesOf(clean), Block.UPDATE_ALL);
        rescan(level);
    }

    private @Nullable BlockPos pickClogTarget(RandomSource random) {
        if (dirtyCandidates.isEmpty()) {
            return null;
        }
        BlockPos entry = worldPosition.above();
        int totalWeight = 0;
        for (BlockPos cell : dirtyCandidates) {
            totalWeight += cell.equals(entry) ? ENTRY_CLOG_WEIGHT : CELL_CLOG_WEIGHT;
        }
        int roll = random.nextInt(totalWeight);
        for (BlockPos cell : dirtyCandidates) {
            roll -= cell.equals(entry) ? ENTRY_CLOG_WEIGHT : CELL_CLOG_WEIGHT;
            if (roll < 0) {
                return cell;
            }
        }
        return dirtyCandidates.getLast();
    }

    private void sparkle(ServerLevel level) {
        RandomSource random = level.getRandom();
        if (dirtyCandidates.isEmpty() || random.nextInt(Math.max(1, interval / SPARK_PACE)) != 0) {
            return;
        }
        BlockPos cell = dirtyCandidates.get(random.nextInt(dirtyCandidates.size()));
        Vec3 at = Vec3.atCenterOf(cell)
                .add(
                        random.triangle(0.0, SPARK_SPREAD),
                        random.triangle(0.0, SPARK_SPREAD),
                        random.triangle(0.0, SPARK_SPREAD));
        Effects.spark(level, at)
                .color(softChannel(random), softChannel(random), softChannel(random))
                .size(SPARK_SIZE)
                .send();
    }

    private static float softChannel(RandomSource random) {
        return SPARK_COLOR_FLOOR + random.nextFloat() * (1.0F - SPARK_COLOR_FLOOR);
    }

    private void evaluate(Level level, Map<BlockPos, Integer> depth, Set<BlockPos> clean) {
        if (depth.isEmpty()) {
            markIdle();
            return;
        }
        BlockPos entry = worldPosition.above();
        Set<BlockPos> counted = clean.contains(entry) ? cleanRouteFrom(entry, clean) : Set.of();
        int total = ROOT_VALUE;
        for (BlockPos cell : counted) {
            if (!isTerminal(cell, depth)) {
                total += CELL_VALUE - PENALTY_PER_NEIGHBOUR * latticeNeighbours(level, cell);
            }
        }
        score = Math.min(MAX_SCORE, total / VALUE_DIVISOR);
        if (score <= 0.0F) {
            markIdle();
            return;
        }
        interval = Math.max(MIN_INTERVAL, Math.round(BASE_INTERVAL - INTERVAL_PER_SCORE * score));
        cost = BASE_COST + (int) Math.sqrt(depth.size());
    }

    private @Nullable IEssentiaConduit neighbour(Level level, Direction side) {
        BlockPos pos = worldPosition.relative(side);
        if (!LoadedChunks.contains(level, pos)) {
            return null;
        }
        return EssentiaAccess.conduit(level, pos, side.getOpposite());
    }

    private static boolean isWilling(IEssentiaConduit source, Direction face, int ownSuction) {
        return source.heldAmount(face) > 0
                && source.pull(face).strength() < ownSuction
                && ownSuction >= source.pullThreshold();
    }

    private void absorb(int received) {
        if (received <= 0) {
            return;
        }
        essentia += received;
        setChanged();
    }

    private void rescan(Level level) {
        Map<BlockPos, Integer> depth = floodStructure(level);
        if (depth.keySet().stream().anyMatch(cell -> sitsOnForeignCondenser(level, cell))) {
            dirtyCandidates = List.of();
            markIdle();
            return;
        }
        dirtyCandidates = depth.keySet().stream()
                .filter(cell -> isClean(level, cell))
                .collect(Collectors.toCollection(ArrayList::new));
        evaluate(level, depth, new HashSet<>(dirtyCandidates));
    }

    private Map<BlockPos, Integer> floodStructure(Level level) {
        Map<BlockPos, Integer> depth = new LinkedHashMap<>();
        BlockPos entry = worldPosition.above();
        if (!isLattice(level, entry)) {
            return depth;
        }
        depth.put(entry, 0);
        List<BlockPos> layer = List.of(entry);
        for (int step = 1; !layer.isEmpty(); step++) {
            List<BlockPos> following = new ArrayList<>();
            for (BlockPos cell : layer) {
                for (Direction direction : DIRECTIONS) {
                    BlockPos adjacent = cell.relative(direction);
                    if (inRange(adjacent) && isLattice(level, adjacent) && depth.putIfAbsent(adjacent, step) == null) {
                        following.add(adjacent);
                    }
                }
            }
            layer = following;
        }
        return depth;
    }

    private boolean sitsOnForeignCondenser(Level level, BlockPos cell) {
        BlockPos below = cell.below();
        return !below.equals(worldPosition) && level.getBlockState(below).is(TTBlocks.CONDENSER.get());
    }

    private void markIdle() {
        score = 0.0F;
        interval = 0;
    }

    private static Set<BlockPos> cleanRouteFrom(BlockPos entry, Set<BlockPos> clean) {
        Set<BlockPos> reached = new HashSet<>();
        ArrayDeque<BlockPos> pending = new ArrayDeque<>();
        reached.add(entry);
        pending.push(entry);
        while (!pending.isEmpty()) {
            BlockPos cell = pending.pop();
            for (Direction direction : DIRECTIONS) {
                BlockPos adjacent = cell.relative(direction);
                if (clean.contains(adjacent) && reached.add(adjacent)) {
                    pending.push(adjacent);
                }
            }
        }
        return reached;
    }

    private static boolean isTerminal(BlockPos cell, Map<BlockPos, Integer> depth) {
        int own = depth.get(cell);
        return Arrays.stream(DIRECTIONS)
                .map(direction -> depth.get(cell.relative(direction)))
                .noneMatch(other -> other != null && other >= own);
    }

    private static int latticeNeighbours(Level level, BlockPos cell) {
        return (int) Arrays.stream(DIRECTIONS)
                .filter(direction -> isLattice(level, cell.relative(direction)))
                .count();
    }

    private static boolean isLattice(Level level, BlockPos pos) {
        return LoadedChunks.contains(level, pos)
                && level.getBlockState(pos).getBlock() instanceof BlockCondenserLattice;
    }

    private static boolean isClean(Level level, BlockPos pos) {
        return level.getBlockState(pos).is(TTBlocks.CONDENSER_LATTICE.get());
    }

    private boolean inRange(BlockPos pos) {
        return LatticeAnchor.withinReach(worldPosition, pos);
    }

    @Override
    public boolean connectsOn(Direction face) {
        return !Direction.UP.equals(face);
    }

    @Override
    public boolean receivesOn(Direction face) {
        return face.getAxis() != Direction.Axis.Y;
    }

    @Override
    public boolean releasesOn(Direction face) {
        return Direction.DOWN.equals(face);
    }

    @Override
    public int pullThreshold() {
        return 0;
    }

    private int pullStrength(@Nullable Direction face) {
        if (Direction.DOWN.equals(face)) {
            return 0;
        }
        return essentia < CAPACITY ? SUCTION : 0;
    }

    @Override
    public EssentiaPull pull(@Nullable Direction face) {
        return EssentiaPull.any(pullStrength(face));
    }

    @Override
    public @Nullable Holder<IAspect> heldAspect(@Nullable Direction face) {
        boolean bottomOrUnspecified = face == null || face == Direction.DOWN;
        return bottomOrUnspecified && flux > 0 ? Aspects.resolve(level, TTAspects.VITIUM) : null;
    }

    @Override
    public int heldAmount(@Nullable Direction face) {
        return face == null || face == Direction.DOWN ? flux : 0;
    }

    @Override
    public int extract(@Nullable Holder<IAspect> aspect, int amount, Direction face, boolean simulate) {
        if (face != Direction.DOWN || aspect != null && !aspect.is(TTAspects.VITIUM)) {
            return 0;
        }
        int removed = Math.min(amount, flux);
        if (removed <= 0 || simulate) {
            return Math.max(removed, 0);
        }
        flux -= removed;
        setChanged();
        return removed;
    }

    @Override
    public int insert(@Nullable Holder<IAspect> aspect, int amount, Direction face, boolean simulate) {
        if (!receivesOn(face)) {
            return 0;
        }
        if (aspect != null && aspect.is(TTAspects.VITIUM)) {
            if (!simulate && level != null && !level.isClientSide()) {
                makeDirty(level);
            }
            return Math.max(0, amount);
        }
        int room = CAPACITY - essentia;
        if (room <= 0 || amount <= 0) {
            return 0;
        }
        int accepted = Math.min(amount, room);
        if (!simulate) {
            absorb(accepted);
        }
        return accepted;
    }

    @Override
    public int roomFor(Holder<IAspect> aspect, Direction face) {
        return receivesOn(face) ? Math.max(0, CAPACITY - essentia) : 0;
    }

    void releaseStoredFluxOnBreak(BlockPos pos) {
        if (level != null && !level.isClientSide() && flux > 0) {
            AuraHelper.polluteAura(level, pos, (float) flux, true);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag input, HolderLookup.Provider registries) {
        super.loadAdditional(input, registries);
        essentia = input.getInt(ESSENTIA_KEY);
        flux = input.getInt(FLUX_KEY);
    }

    @Override
    protected void saveAdditional(CompoundTag output, HolderLookup.Provider registries) {
        super.saveAdditional(output, registries);
        output.putInt(ESSENTIA_KEY, essentia);
        output.putInt(FLUX_KEY, flux);
    }
}
