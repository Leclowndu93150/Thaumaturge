package com.leclowndu93150.thaumaturge.content.essentia.tube.buffer;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaTransport;
import com.leclowndu93150.thaumaturge.content.essentia.flow.EssentiaFlowHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

public final class StrongerPullCheck {
    private static final Direction[] DIRECTIONS = Direction.values();

    private final BufferSides sides;
    private final BufferSuctionPolicy policy;

    public StrongerPullCheck(BufferSides sides, BufferSuctionPolicy policy) {
        this.sides = sides;
        this.policy = policy;
    }

    public TakeVerdict verdict(Level level, BlockPos pos, Holder<IAspect> aspect, Direction askingSide) {
        int askerPull = pullOn(level, pos, askingSide, null);
        for (Direction rivalSide : DIRECTIONS) {
            if (rivalSide == askingSide || !sides.isOpen(rivalSide)) {
                continue;
            }
            int rivalPull = pullOn(level, pos, rivalSide, aspect);
            int ownPull = policy.offered(sides.choke(rivalSide));
            if (rivalPull > askerPull && rivalPull > ownPull) {
                return TakeVerdict.OUTBID;
            }
        }
        return TakeVerdict.GRANTED;
    }

    private static int pullOn(Level level, BlockPos pos, Direction side, @Nullable Holder<IAspect> aspect) {
        Direction facingBack = side.getOpposite();
        IEssentiaTransport neighbour = EssentiaFlowHandler.transport(level, pos.relative(side), facingBack);
        if (neighbour == null || !neighbour.isConnectable(facingBack)) {
            return 0;
        }
        Holder<IAspect> wanted = neighbour.getSuctionType(facingBack);
        if (aspect != null && wanted != null && !wanted.equals(aspect)) {
            return 0;
        }
        return neighbour.getSuctionAmount(facingBack);
    }
}
