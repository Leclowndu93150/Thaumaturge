package com.leclowndu93150.thaumaturge.content.aura;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.content.aura.pressure.FluxPressureEvents;
import com.leclowndu93150.thaumaturge.content.taint.flux.PhysicalFluxAuraFloor;
import com.leclowndu93150.thaumaturge.content.taint.flux.PhysicalFluxOutbreaks;
import com.leclowndu93150.thaumaturge.registry.TTAttachments;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import org.jspecify.annotations.Nullable;

@EventBusSubscriber(modid = TTIds.MODID)
public final class AuraTickHandler {
    private static final int TICKS_PER_SECOND = 20;
    private static final int NEIGHBOUR_COUNT = 4;
    private static final MoonProfile[] MOON_PROFILES = {
        new MoonProfile(0.25F, 0.00F, 1.15F),
        new MoonProfile(0.15F, 0.10F, 1.05F),
        new MoonProfile(0.10F, 0.15F, 1.00F),
        new MoonProfile(0.05F, 0.20F, 0.95F),
        new MoonProfile(0.00F, 0.25F, 0.85F),
        new MoonProfile(0.05F, 0.20F, 0.95F),
        new MoonProfile(0.10F, 0.15F, 1.00F),
        new MoonProfile(0.15F, 0.10F, 1.05F)
    };
    private static final float FLOOR_RAISE_PER_SECOND = 0.5F;
    private static final float TRANSFER_PER_SECOND = 1.0F;
    private static final float VIS_TRANSFER_RATIO = 0.75F;
    private static final float FLUX_TRANSFER_DIVISOR = 1.75F;
    private static final float FLUX_TRANSFER_MIN_FLUX = 5.0F;
    private static final float FLUX_TRANSFER_BASE_DIVISOR = 10.0F;
    private static final float OVERFLOW_VIS_RATIO = 1.25F;
    private static final float DEPLETED_VIS_RATIO = 0.1F;
    private static final float DEGRADATION_CHANCE = 0.1F;
    private static final float EVENT_FLUX_RATIO = 0.75F;
    private static final float RIFT_CHANCE_DIVISOR = 5000.0F;
    private static final float PRESSURE_CHANCE_DIVISOR = 100.0F;

    private AuraTickHandler() {}

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (event.getLevel() instanceof ServerLevel level) {
            tickLevel(level);
        }
    }

    private static void tickLevel(ServerLevel level) {
        if (level.getGameTime() % TICKS_PER_SECOND != 0
                || !level.tickRateManager().runsNormally()) {
            return;
        }
        int phase = level.getMoonPhase();
        MoonProfile profile = MOON_PROFILES[Math.floorMod(phase, MOON_PROFILES.length)];
        RandomSource random = level.getRandom();
        for (ChunkPos pos : AuraManager.loadedChunksSnapshot(level)) {
            LevelChunk chunk = level.getChunkSource().getChunkNow(pos.x, pos.z);
            if (chunk != null) {
                balanceChunk(level, chunk, pos, profile, random);
            }
        }
    }

    private static void balanceChunk(
            ServerLevel level, LevelChunk chunk, ChunkPos pos, MoonProfile profile, RandomSource random) {
        AuraData data = chunk.getData(TTAttachments.AURA.get());
        AuraGenHandler.initializeIfNeeded(level, chunk, data);
        PhysicalFluxOutbreaks.tryOutbreak(level, chunk, random);
        data.setChunkPos(pos);
        if (data.getBase() == 0) {
            return;
        }
        List<AuraData> neighbours = adjacentAuras(level, pos);
        AuraData visReceiver = pickVisReceiver(neighbours, profile.capacity(), random);
        AuraData fluxReceiver = pickFluxReceiver(neighbours, random);

        float originalVis = data.getVis();
        float originalFlux = data.getFlux();
        Balance balance = new Balance(originalVis, originalFlux);
        float scaledBase = data.getBase() * profile.capacity();

        float floor = PhysicalFluxAuraFloor.target(chunk, data.getBase());
        if (balance.flux < floor) {
            balance.flux += Math.min(FLOOR_RAISE_PER_SECOND, floor - balance.flux);
        }
        if (visReceiver != null) {
            shareVis(level, balance, visReceiver);
        }
        if (fluxReceiver != null) {
            shareFlux(level, balance, fluxReceiver, data.getBase());
        }
        settle(balance, profile, scaledBase, random);

        data.setVis(balance.vis);
        data.setFlux(balance.flux);
        if (data.getVis() != originalVis || data.getFlux() != originalFlux) {
            chunk.setUnsaved(true);
        }
        rollEvents(level, pos, data.getFlux(), scaledBase, random);
    }

    private static void shareVis(ServerLevel level, Balance balance, AuraData receiver) {
        float theirs = receiver.getVis();
        if (theirs >= balance.vis || theirs / balance.vis >= VIS_TRANSFER_RATIO) {
            return;
        }
        float moved = Math.min(balance.vis - theirs, TRANSFER_PER_SECOND);
        receiver.setVis(theirs + moved);
        balance.vis -= moved;
        AuraManager.markChunkDirty(level, receiver.getChunkPos());
    }

    private static void shareFlux(ServerLevel level, Balance balance, AuraData receiver, int base) {
        float threshold = Math.max(FLUX_TRANSFER_MIN_FLUX, base / FLUX_TRANSFER_BASE_DIVISOR);
        if (balance.flux <= threshold || receiver.getFlux() >= balance.flux / FLUX_TRANSFER_DIVISOR) {
            return;
        }
        float moved = Math.min(balance.flux - receiver.getFlux(), TRANSFER_PER_SECOND);
        receiver.setFlux(receiver.getFlux() + moved);
        balance.flux -= moved;
        AuraManager.markChunkDirty(level, receiver.getChunkPos());
    }

    private static void settle(Balance balance, MoonProfile profile, float scaledBase, RandomSource random) {
        float total = balance.vis + balance.flux;
        if (total < scaledBase) {
            balance.vis += Math.min(scaledBase - total, profile.visRegeneration());
            return;
        }
        if (balance.vis > OVERFLOW_VIS_RATIO * scaledBase) {
            if (random.nextFloat() < DEGRADATION_CHANCE) {
                balance.flux += profile.fluxStep();
                balance.vis -= profile.fluxStep();
            }
            return;
        }
        boolean depleted = balance.vis <= DEPLETED_VIS_RATIO * scaledBase && balance.vis >= balance.flux;
        if (depleted && random.nextFloat() < DEGRADATION_CHANCE) {
            balance.flux += profile.fluxStep();
        }
    }

    private static List<AuraData> adjacentAuras(ServerLevel level, ChunkPos pos) {
        return Direction.Plane.HORIZONTAL.stream()
                .map(direction -> AuraManager.chunkAt(
                        level, new ChunkPos(pos.x + direction.getStepX(), pos.z + direction.getStepZ())))
                .filter(neighbour -> neighbour != null && neighbour.getBase() != 0)
                .collect(Collectors.toCollection(() -> new ArrayList<>(NEIGHBOUR_COUNT)));
    }

    private static void rollEvents(ServerLevel level, ChunkPos pos, float flux, float scaledBase, RandomSource random) {
        if (flux <= EVENT_FLUX_RATIO * scaledBase) {
            return;
        }
        if (random.nextFloat() < flux / RIFT_CHANCE_DIVISOR) {
            AuraManager.queueRiftTrigger(level, new BlockPos(pos.getMinBlockX(), 0, pos.getMinBlockZ()));
            return;
        }
        if (random.nextFloat() < flux / (PRESSURE_CHANCE_DIVISOR * Math.max(1.0F, scaledBase))) {
            FluxPressureEvents.queue(level, pos);
        }
    }

    private static @Nullable AuraData pickVisReceiver(
            List<AuraData> neighbours, float capacityMultiplier, RandomSource random) {
        List<AuraData> roomy = neighbours.stream()
                .filter(neighbour ->
                        neighbour.getVis() + neighbour.getFlux() < neighbour.getBase() * capacityMultiplier)
                .collect(Collectors.toCollection(ArrayList::new));
        return pickLowest(roomy, AuraData::getVis, random);
    }

    private static @Nullable AuraData pickFluxReceiver(List<AuraData> neighbours, RandomSource random) {
        return pickLowest(new ArrayList<>(neighbours), AuraData::getFlux, random);
    }

    private static @Nullable AuraData pickLowest(
            List<AuraData> candidates, ToDoubleFunction<AuraData> pool, RandomSource random) {
        Util.shuffle(candidates, random);
        return candidates.stream().min(Comparator.comparingDouble(pool)).orElse(null);
    }

    private record MoonProfile(float visRegeneration, float fluxStep, float capacity) {}

    private static final class Balance {
        private float vis;
        private float flux;

        private Balance(float vis, float flux) {
            this.vis = vis;
            this.flux = flux;
        }
    }
}
