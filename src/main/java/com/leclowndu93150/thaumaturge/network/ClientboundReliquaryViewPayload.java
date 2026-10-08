package com.leclowndu93150.thaumaturge.network;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.content.eldritch.reliquary.ReliquaryView;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ClientboundReliquaryViewPayload(BlockPos pos, ReliquaryView view) implements CustomPacketPayload {
    public static final Type<ClientboundReliquaryViewPayload> TYPE = new Type<>(TTIds.rl("reliquary_view"));
    public static final StreamCodec<ByteBuf, ClientboundReliquaryViewPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            ClientboundReliquaryViewPayload::pos,
            ReliquaryView.STREAM_CODEC,
            ClientboundReliquaryViewPayload::view,
            ClientboundReliquaryViewPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
