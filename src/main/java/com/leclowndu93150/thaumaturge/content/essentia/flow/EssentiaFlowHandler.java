package com.leclowndu93150.thaumaturge.content.essentia.flow;

import com.leclowndu93150.thaumaturge.api.aspect.Aspects;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaAccess;
import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaTransport;
import com.leclowndu93150.thaumaturge.content.effect.EffectDispatch;
import com.leclowndu93150.thaumaturge.content.essentia.tube.BlockEntityTube;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class EssentiaFlowHandler {
    private static final int HOP_LOSS = 1;
    private static final int RESTRICT_DIVISOR = 2;
    private static final int STREAM_ODDS = 8;
    private static final int CREAK_ODDS = 100;
    private static final int STREAM_TYPE_TAG = 0;
    private static final double EDGE_OFFSET = 0.5;
    private static final int TRANSFER_AMOUNT = 1;

    private EssentiaFlowHandler() {}

    public static @Nullable IEssentiaTransport transport(Level level, BlockPos pos, Direction face) {
        if (!level.hasChunkAt(pos)) {
            return null;
        }
        return EssentiaAccess.transport(level, pos, face);
    }

    public static void recalculateSuction(Level level, BlockPos pos, BlockEntityTube tube, @Nullable ResourceKey<IAspect> filter, boolean restrict, boolean directional) {
        tube.setSuction(null, 0);
        Direction facing = directional ? tube.flowSide() : null;
        Holder<IAspect> filterHolder = filter == null ? null : Aspects.resolve(level, filter);
        Holder<IAspect> held = tube.getEssentiaAmount(null) > 0 ? tube.getEssentiaType(null) : null;
        int strongest = 0;
        Holder<IAspect> strongestType = null;
        for (Direction direction : Direction.values()) {
            if (facing != null && direction != facing.getOpposite() || !tube.isConnectable(direction)) {
                continue;
            }
            Direction touching = direction.getOpposite();
            IEssentiaTransport neighbour = transport(level, pos.relative(direction), touching);
            if (neighbour == null || !neighbour.isConnectable(touching)) {
                continue;
            }
            int offered = neighbour.getSuctionAmount(touching);
            if (offered <= 0 || offered <= strongest + HOP_LOSS) {
                continue;
            }
            Holder<IAspect> offeredType = neighbour.getSuctionType(touching);
            if (offeredType != null && (filter != null && !offeredType.is(filter) || held != null && !offeredType.equals(held))) {
                continue;
            }
            strongest = restrict ? offered / RESTRICT_DIVISOR : offered - HOP_LOSS;
            strongestType = offeredType != null ? offeredType : filterHolder;
        }
        if (strongest > 0) {
            tube.setSuction(strongestType, strongest);
        }
    }

    public static void equalizeWithNeighbours(Level level, BlockPos pos, BlockEntityTube tube, boolean directional) {
        int suction = tube.getSuctionAmount(null);
        if (suction <= 0 || tube.getEssentiaAmount(null) > 0) {
            return;
        }
        Holder<IAspect> wanted = tube.getSuctionType(null);
        Direction facing = directional ? tube.flowSide() : null;
        for (Direction direction : Direction.values()) {
            if (facing != null && direction == facing.getOpposite() || !tube.isConnectable(direction)) {
                continue;
            }
            Direction touching = direction.getOpposite();
            IEssentiaTransport neighbour = transport(level, pos.relative(direction), touching);
            if (neighbour == null || !neighbour.isConnectable(touching) || !neighbour.canOutputTo(touching) || neighbour.getSuctionAmount(touching) >= suction
                    || suction < neighbour.getMinimumSuction()) {
                continue;
            }
            Holder<IAspect> offered = neighbour.getEssentiaType(touching);
            if (wanted != null && offered != null && !wanted.equals(offered)) {
                continue;
            }
            Holder<IAspect> aspect = wanted != null ? wanted : offered;
            if (aspect == null || neighbour.getEssentiaAmount(touching) < TRANSFER_AMOUNT || neighbour.takeEssentia(aspect, TRANSFER_AMOUNT, touching) != TRANSFER_AMOUNT) {
                continue;
            }
            if (tube.addEssentia(aspect, TRANSFER_AMOUNT, direction) < TRANSFER_AMOUNT) {
                neighbour.addEssentia(aspect, TRANSFER_AMOUNT, touching);
                return;
            }
            announceTransfer(level, pos, tube, direction, aspect);
            return;
        }
    }

    private static void announceTransfer(Level level, BlockPos pos, BlockEntityTube tube, Direction direction, Holder<IAspect> aspect) {
        if (!(level instanceof ServerLevel server)) {
            return;
        }
        RandomSource random = server.getRandom();
        if (random.nextInt(STREAM_ODDS) == 0) {
            Vec3 center = Vec3.atCenterOf(pos);
            Vec3 edge = center.add(direction.getStepX() * EDGE_OFFSET, direction.getStepY() * EDGE_OFFSET, direction.getStepZ() * EDGE_OFFSET);
            EffectDispatch.spawnEssentiaStream(server, edge, center, aspect.value().color(), STREAM_TYPE_TAG);
        }
        if (random.nextInt(CREAK_ODDS) == 0) {
            tube.broadcastCreak(server, pos);
        }
    }
}
