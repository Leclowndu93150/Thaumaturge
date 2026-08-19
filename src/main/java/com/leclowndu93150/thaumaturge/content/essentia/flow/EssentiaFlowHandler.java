package com.leclowndu93150.thaumaturge.content.essentia.flow;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaAccess;
import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaTransport;
import com.leclowndu93150.thaumaturge.content.effect.EffectDispatch;
import com.leclowndu93150.thaumaturge.content.essentia.EssentiaTransportHelper;
import com.leclowndu93150.thaumaturge.content.essentia.tube.BlockEntityTube;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class EssentiaFlowHandler {
    private static final int CREAK_CHANCE = 100;

    private EssentiaFlowHandler() {}

    public static @Nullable IEssentiaTransport transport(Level level, BlockPos pos, Direction faceFromNeighbour) {
        return EssentiaAccess.transport(level, pos, faceFromNeighbour);
    }

    public static void recalculateSuction(Level level, BlockPos pos, BlockEntityTube tube, @Nullable ResourceKey<IAspect> filter, boolean restrict, boolean directional) {
        tube.setSuction(null, 0);
        Direction facing = tube.facing();
        Holder<IAspect> filterHolder = filter == null ? null : EssentiaTransportHelper.resolve(level, filter);
        for (Direction dir : Direction.values()) {
            if (directional && facing != null && facing.getOpposite() != dir)
                continue;
            if (!tube.isConnectable(dir))
                continue;
            IEssentiaTransport neighbour = transport(level, pos.relative(dir), dir.getOpposite());
            if (neighbour == null)
                continue;
            Holder<IAspect> neighbourSuction = neighbour.getSuctionType(dir.getOpposite());
            Holder<IAspect> tubeEssentia = tube.getEssentiaType(dir);
            int tubeEssentiaAmt = tube.getEssentiaAmount(dir);
            if (filterHolder != null && neighbourSuction != null && !filterHolder.equals(neighbourSuction))
                continue;
            if (filterHolder == null && tubeEssentiaAmt > 0 && neighbourSuction != null && tubeEssentia != null && !tubeEssentia.equals(neighbourSuction))
                continue;
            if (filterHolder != null && tubeEssentiaAmt > 0 && tubeEssentia != null && neighbourSuction != null && !tubeEssentia.equals(neighbourSuction))
                continue;
            int suck = neighbour.getSuctionAmount(dir.getOpposite());
            if (suck > 0 && suck > tube.getSuctionAmount(null) + 1) {
                Holder<IAspect> st = neighbourSuction != null ? neighbourSuction : filterHolder;
                tube.setSuction(st, restrict ? suck / 2 : suck - 1);
            }
        }
    }

    public static void equalizeWithNeighbours(Level level, BlockPos pos, BlockEntityTube tube, boolean directional) {
        if (tube.getEssentiaAmount(null) > 0)
            return;
        Direction facing = tube.facing();
        for (Direction dir : Direction.values()) {
            if (directional && facing != null && facing.getOpposite() == dir)
                continue;
            if (!tube.isConnectable(dir))
                continue;
            IEssentiaTransport neighbour = transport(level, pos.relative(dir), dir.getOpposite());
            if (neighbour == null)
                continue;
            if (!neighbour.canOutputTo(dir.getOpposite()))
                continue;
            Holder<IAspect> tubeSuction = tube.getSuctionType(null);
            Holder<IAspect> neighbourEssentia = neighbour.getEssentiaType(dir.getOpposite());
            if (tubeSuction != null && neighbourEssentia != null && !tubeSuction.equals(neighbourEssentia))
                continue;
            if (tube.getSuctionAmount(null) <= neighbour.getSuctionAmount(dir.getOpposite()))
                continue;
            if (tube.getSuctionAmount(null) < neighbour.getMinimumSuction())
                continue;
            Holder<IAspect> aspect = tubeSuction;
            if (aspect == null) {
                aspect = neighbourEssentia;
                if (aspect == null) {
                    aspect = neighbour.getEssentiaType(null);
                }
            }
            if (aspect == null)
                continue;
            int taken = neighbour.takeEssentia(aspect, 1, dir.getOpposite());
            int added = tube.addEssentia(aspect, taken, dir);
            if (added > 0) {
                spawnStreamParticle(level, pos, dir, aspect);
                if (level.getRandom().nextInt(CREAK_CHANCE) == 0) {
                    tube.broadcastCreak(level, pos);
                }
                return;
            }
        }
    }

    private static void spawnStreamParticle(Level level, BlockPos pos, Direction fromNeighbourDir, Holder<IAspect> aspect) {
        if (!(level instanceof ServerLevel server))
            return;
        if (level.getRandom().nextInt(8) != 0)
            return;
        int color = aspect.value().color();
        double sx = pos.getX() + 0.5 + fromNeighbourDir.getStepX() * 0.5;
        double sy = pos.getY() + 0.5 + fromNeighbourDir.getStepY() * 0.5;
        double sz = pos.getZ() + 0.5 + fromNeighbourDir.getStepZ() * 0.5;
        double tx = pos.getX() + 0.5;
        double ty = pos.getY() + 0.5;
        double tz = pos.getZ() + 0.5;
        EffectDispatch.spawnEssentiaStream(server, new Vec3(sx, sy, sz), new Vec3(tx, ty, tz), color, 0, server.getRandom().nextInt(8), 0.15F, 20, 0.0);
    }
}
