package com.leclowndu93150.thaumaturge.network;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.content.research.decon.BlockEntityDeconstructionTable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ServerboundDeconCollectPayload(BlockPos pos) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ServerboundDeconCollectPayload> TYPE = new CustomPacketPayload.Type<>(TCIds.rl("decon_collect"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundDeconCollectPayload> STREAM_CODEC = StreamCodec.composite(BlockPos.STREAM_CODEC, ServerboundDeconCollectPayload::pos,
            ServerboundDeconCollectPayload::new);

    public static void handle(ServerboundDeconCollectPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }
            if (payload.pos().distToCenterSqr(player.getX(), player.getY(), player.getZ()) > 64.0) {
                return;
            }
            if (player.level().getBlockEntity(payload.pos()) instanceof BlockEntityDeconstructionTable table) {
                table.collect(player);
            }
        });
    }

    @Override
    public CustomPacketPayload.Type<ServerboundDeconCollectPayload> type() {
        return TYPE;
    }
}
