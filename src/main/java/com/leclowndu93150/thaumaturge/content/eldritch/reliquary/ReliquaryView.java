package com.leclowndu93150.thaumaturge.content.eldritch.reliquary;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;

public enum ReliquaryView {
    CLAIMABLE,
    CLAIMED,
    INELIGIBLE,
    WARDED;

    public static final StreamCodec<ByteBuf, ReliquaryView> STREAM_CODEC = ByteBufCodecs.idMapper(
            ByIdMap.continuous(ReliquaryView::ordinal, values(), ByIdMap.OutOfBoundsStrategy.ZERO),
            ReliquaryView::ordinal);

    public boolean showsReward() {
        return this == CLAIMABLE || this == WARDED;
    }
}
