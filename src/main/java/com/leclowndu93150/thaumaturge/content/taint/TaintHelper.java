package com.leclowndu93150.thaumaturge.content.taint;

import com.leclowndu93150.thaumaturge.api.taint.ITaintBlock;
import com.leclowndu93150.thaumaturge.config.ThaumaturgeCommonConfig;
import com.leclowndu93150.thaumaturge.content.entity.AbstractTaintSeed;
import com.leclowndu93150.thaumaturge.content.taint.block.BlockTaintFibre;
import com.leclowndu93150.thaumaturge.content.taint.ecology.TaintBiomeManager;
import com.leclowndu93150.thaumaturge.content.taint.ecology.TaintBlooms;
import com.leclowndu93150.thaumaturge.content.taint.ecology.TaintEcology;
import com.leclowndu93150.thaumaturge.content.taint.flux.PhysicalFlux;
import com.leclowndu93150.thaumaturge.content.taint.spread.FibreGrowth;
import com.leclowndu93150.thaumaturge.content.taint.spread.TaintFrontier;
import com.leclowndu93150.thaumaturge.content.taint.spread.TaintSeedRegistry;
import com.leclowndu93150.thaumaturge.content.taint.spread.TaintSprouting;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jspecify.annotations.Nullable;

public final class TaintHelper {
    private static final double FRINGE_FRACTION = 0.8;
    private static final double SEED_PRESENCE_REACH = 1.0;

    private TaintHelper() {}

    public static double spreadArea() {
        return ThaumaturgeCommonConfig.TAINT_SPREAD_AREA.get();
    }

    public static double fringeDistance() {
        return spreadArea() * FRINGE_FRACTION;
    }

    public static void registerSeedAnchor(ServerLevel level, BlockPos pos) {
        TaintSeedRegistry.get(level).addSeed(pos);
    }

    public static void unregisterSeedAnchor(ServerLevel level, BlockPos pos) {
        TaintSeedRegistry.get(level).removeSeed(pos);
    }

    public static boolean isWithinSeedInfluence(Level level, BlockPos pos) {
        if (!(level instanceof ServerLevel server)) {
            return false;
        }
        TaintSeedRegistry registry = TaintSeedRegistry.get(server);
        double reachSq = spreadArea() * spreadArea();
        List<BlockPos> stale = null;
        boolean found = false;
        for (BlockPos anchor : registry.all()) {
            if (anchor.distSqr(pos) > reachSq || !server.areEntitiesLoaded(ChunkPos.pack(anchor))) {
                continue;
            }
            if (hasLivingSeed(server, anchor)) {
                found = true;
                break;
            }
            stale = markStale(stale, anchor);
        }
        if (stale != null) {
            stale.forEach(registry::removeSeed);
        }
        return found;
    }

    public static boolean isEcologicallySustained(ServerLevel level, BlockPos pos) {
        return TaintBiomeManager.isTainted(level, pos) || isWithinSeedInfluence(level, pos);
    }

    public static boolean isOnSeedFringe(Level level, BlockPos pos) {
        if (!(level instanceof ServerLevel server)) {
            return false;
        }
        double inner = fringeDistance();
        double outer = spreadArea();
        return TaintSeedRegistry.get(server).isAtEdge(pos, inner * inner, outer * outer);
    }

    public static boolean isTaintBlock(BlockState state) {
        return state.getBlock() instanceof ITaintBlock;
    }

    public static boolean hasSturdyNeighbour(LevelReader level, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            if (facesSturdily(level, pos, direction)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isRootless(LevelReader level, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            if (!isTaintBlock(level.getBlockState(pos.relative(direction))) && facesSturdily(level, pos, direction)) {
                return false;
            }
        }
        return true;
    }

    public static boolean facesSturdily(LevelReader level, BlockPos pos, Direction direction) {
        BlockPos neighbour = pos.relative(direction);
        return level.getBlockState(neighbour).isFaceSturdy(level, neighbour, direction.getOpposite());
    }

    public static int countAdjacentTaint(LevelReader level, BlockPos pos) {
        int count = 0;
        for (Direction direction : Direction.values()) {
            if (isTaintBlock(level.getBlockState(pos.relative(direction)))) {
                count++;
            }
        }
        return count;
    }

    public static boolean placeFibreFromFlux(ServerLevel level, BlockPos pos) {
        if (!ThaumaturgeCommonConfig.TAINT_FROM_FLUX.get() || ThaumaturgeCommonConfig.WUSS_MODE.get() || TaintBlooms.isProtected(level, pos)) {
            return false;
        }
        level.setBlock(pos, BlockTaintFibre.stateForWorld(level, pos), Block.UPDATE_ALL);
        return true;
    }

    public static void attemptFibreGrowth(ServerLevel level, BlockPos origin, boolean force) {
        FibreGrowth.attempt(level, origin, force);
    }

    public static boolean canHostFoothold(BlockState state) {
        if (PhysicalFlux.isPhysicalFlux(state)) {
            return true;
        }
        return state.canBeReplaced() && state.getFluidState().isEmpty();
    }

    public static void establishFoothold(ServerLevel level, BlockPos pos, float pressure, int spreadAttempts) {
        if (canHostFoothold(level.getBlockState(pos))) {
            level.setBlock(pos, BlockTaintFibre.stateForWorld(level, pos), Block.UPDATE_ALL);
        }
        TaintEcology.addPressure(level, pos, pressure);
        for (int attempt = 0; attempt < spreadAttempts; attempt++) {
            FibreGrowth.attempt(level, pos, true);
        }
    }

    public static boolean trySpreadTaintedBiome(ServerLevel level, BlockPos pos, RandomSource random) {
        return TaintFrontier.tryAdvance(level, pos, random);
    }

    public static boolean trySpawnSatelliteSeed(ServerLevel level, BlockPos origin, RandomSource random) {
        return TaintSprouting.trySatellite(level, origin, random);
    }

    private static boolean hasLivingSeed(ServerLevel level, BlockPos anchor) {
        return !level.getEntitiesOfClass(AbstractTaintSeed.class, new AABB(anchor).inflate(SEED_PRESENCE_REACH), Entity::isAlive).isEmpty();
    }

    private static List<BlockPos> markStale(@Nullable List<BlockPos> stale, BlockPos anchor) {
        List<BlockPos> list = stale == null ? new ArrayList<>() : stale;
        list.add(anchor);
        return list;
    }
}
