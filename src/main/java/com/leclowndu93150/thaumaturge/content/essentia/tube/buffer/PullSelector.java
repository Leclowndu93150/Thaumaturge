package com.leclowndu93150.thaumaturge.content.essentia.tube.buffer;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaTransport;
import com.leclowndu93150.thaumaturge.content.essentia.flow.EssentiaFlowHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

public final class PullSelector {
    private static final Direction[] DIRECTIONS = Direction.values();
    private static final int MIN_UNITS = 1;

    private final BufferSides sides;
    private final BufferSuctionPolicy policy;

    public PullSelector(BufferSides sides, BufferSuctionPolicy policy) {
        this.sides = sides;
        this.policy = policy;
    }

    public @Nullable PullCandidate select(Level level, BlockPos pos, int firstSideOrdinal) {
        for (int i = firstSideOrdinal; i < DIRECTIONS.length; i++) {
            Direction side = DIRECTIONS[i];
            if (!sides.isOpen(side)) {
                continue;
            }
            Direction back = side.getOpposite();
            IEssentiaTransport peer = EssentiaFlowHandler.transport(level, pos.relative(side), back);
            if (peer == null || !peer.isConnectable(back)) {
                continue;
            }
            Holder<IAspect> offered = peer.getEssentiaType(back);
            int offer = policy.offered(sides.choke(side));
            if (offered != null && peer.getEssentiaAmount(back) >= MIN_UNITS && peer.canOutputTo(back) && peer.getSuctionAmount(back) < offer && offer >= peer.getMinimumSuction()) {
                return new PullCandidate(side, peer, offered);
            }
        }
        return null;
    }
}
