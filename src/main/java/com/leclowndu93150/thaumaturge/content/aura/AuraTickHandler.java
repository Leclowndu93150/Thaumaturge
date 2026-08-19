package com.leclowndu93150.thaumaturge.content.aura;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.registry.TCAttachments;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.MoonPhase;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.jspecify.annotations.Nullable;

@EventBusSubscriber(modid = TCIds.MODID)
public final class AuraTickHandler {
    private static final int TICK_INTERVAL = 20;

    private static final float[] PHASE_VIS_TABLE = new float[]{0.25F, 0.15F, 0.1F, 0.05F, 0.0F, 0.05F, 0.1F, 0.15F};
    private static final float[] PHASE_MAX_TABLE = new float[]{0.15F, 0.05F, 0.0F, -0.05F, -0.15F, -0.05F, 0.0F, 0.05F};
    private static final float BASE_FLUX_RATE = 0.25F;

    private static final float TRANSFER_CAP = 1.0F;
    private static final float VIS_EQUALIZE_RATIO = 0.75F;
    private static final float FLUX_SPREAD_MIN = 5.0F;
    private static final float FLUX_SPREAD_BASE_DIVISOR = 10.0F;
    private static final float FLUX_EQUALIZE_RATIO = 1.75F;
    private static final float OVERCHARGE_RATIO = 1.25F;
    private static final float LOW_VIS_RATIO = 0.1F;
    private static final float DEGRADE_CHANCE = 0.1F;
    private static final float RIFT_FLUX_RATIO = 0.75F;
    private static final float RIFT_CHANCE_DIVISOR = 5000.0F;

    private AuraTickHandler() {}

    private record MoonFactors(float vis, float flux, float max) {
        static MoonFactors of(ServerLevel level) {
            MoonPhase moonPhase = level.environmentAttributes().getValue(EnvironmentAttributes.MOON_PHASE, Vec3.ZERO);
            float vis = PHASE_VIS_TABLE[moonPhase.index()];
            return new MoonFactors(vis, BASE_FLUX_RATE - vis, 1.0F + PHASE_MAX_TABLE[moonPhase.index()]);
        }
    }

    private record Sink(AuraData data, LevelChunk chunk) {
    }

    private record Neighbours(@Nullable Sink visSink, @Nullable Sink fluxSink) {
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        for (ServerLevel level : server.getAllLevels()) {
            if (level.getGameTime() % TICK_INTERVAL == 0 && level.tickRateManager().runsNormally()) {
                tickLevel(level);
            }
        }
    }

    private static void tickLevel(ServerLevel level) {
        MoonFactors factors = MoonFactors.of(level);
        RandomSource rand = level.getRandom();
        Set<ChunkPos> loaded = AuraManager.loadedChunksSnapshot(level);
        for (ChunkPos pos : loaded) {
            LevelChunk chunk = level.getChunkSource().getChunkNow(pos.x(), pos.z());
            if (chunk == null) {
                continue;
            }
            AuraData data = chunk.getData(TCAttachments.AURA.get());
            if (data.getBase() == 0) {
                continue;
            }
            data.setChunkPos(pos);
            processAuraChunk(level, chunk, data, factors, rand);
        }
    }

    private static void processAuraChunk(ServerLevel level, LevelChunk chunk, AuraData aura, MoonFactors factors, RandomSource rand) {
        Neighbours neighbours = scanNeighbours(level, aura, factors, rand);
        float base = aura.getBase() * factors.max();
        float vis = aura.getVis();
        float flux = aura.getFlux();
        boolean dirty = false;

        Sink visSink = neighbours.visSink();
        if (visSink != null) {
            float sinkVis = visSink.data().getVis();
            if (sinkVis < vis && sinkVis / vis < VIS_EQUALIZE_RATIO) {
                float transfer = Math.min(vis - sinkVis, TRANSFER_CAP);
                vis -= transfer;
                visSink.data().setVis(sinkVis + transfer);
                visSink.chunk().markUnsaved();
                dirty = true;
            }
        }

        Sink fluxSink = neighbours.fluxSink();
        if (fluxSink != null) {
            float sinkFlux = fluxSink.data().getFlux();
            if (flux > Math.max(FLUX_SPREAD_MIN, aura.getBase() / FLUX_SPREAD_BASE_DIVISOR) && sinkFlux < flux / FLUX_EQUALIZE_RATIO) {
                float transfer = Math.min(flux - sinkFlux, TRANSFER_CAP);
                flux -= transfer;
                fluxSink.data().setFlux(sinkFlux + transfer);
                fluxSink.chunk().markUnsaved();
                dirty = true;
            }
        }

        if (vis + flux < base) {
            vis += Math.min(base - (vis + flux), factors.vis());
            dirty = true;
        } else if (vis > base * OVERCHARGE_RATIO && rand.nextFloat() < DEGRADE_CHANCE) {
            flux += factors.flux();
            vis -= factors.flux();
            dirty = true;
        } else if (vis <= base * LOW_VIS_RATIO && vis >= flux && rand.nextFloat() < DEGRADE_CHANCE) {
            flux += factors.flux();
            dirty = true;
        }

        if (dirty) {
            aura.setVis(vis);
            aura.setFlux(flux);
            chunk.markUnsaved();
        }

        if (flux > base * RIFT_FLUX_RATIO && rand.nextFloat() < flux / RIFT_CHANCE_DIVISOR) {
            ChunkPos pos = aura.getChunkPos();
            AuraManager.queueRiftTrigger(level, new BlockPos(pos.x() * 16, 0, pos.z() * 16));
        }
    }

    private static Neighbours scanNeighbours(ServerLevel level, AuraData aura, MoonFactors factors, RandomSource rand) {
        Direction[] directions = new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};
        for (int i = directions.length - 1; i > 0; i--) {
            int j = rand.nextInt(i + 1);
            Direction tmp = directions[i];
            directions[i] = directions[j];
            directions[j] = tmp;
        }
        Sink visSink = null;
        Sink fluxSink = null;
        float lowestVis = Float.MAX_VALUE;
        float lowestFlux = Float.MAX_VALUE;
        for (Direction dir : directions) {
            int nx = aura.getChunkPos().x() + dir.getStepX();
            int nz = aura.getChunkPos().z() + dir.getStepZ();
            LevelChunk neighbourChunk = level.getChunkSource().getChunkNow(nx, nz);
            if (neighbourChunk == null) {
                continue;
            }
            AuraData neighbour = neighbourChunk.getData(TCAttachments.AURA.get());
            if (neighbour.getBase() == 0) {
                continue;
            }
            neighbour.setChunkPos(new ChunkPos(nx, nz));
            if ((visSink == null || lowestVis > neighbour.getVis()) && neighbour.getVis() + neighbour.getFlux() < neighbour.getBase() * factors.max()) {
                visSink = new Sink(neighbour, neighbourChunk);
                lowestVis = neighbour.getVis();
            }
            if (fluxSink == null || lowestFlux > neighbour.getFlux()) {
                fluxSink = new Sink(neighbour, neighbourChunk);
                lowestFlux = neighbour.getFlux();
            }
        }
        return new Neighbours(visSink, fluxSink);
    }
}
