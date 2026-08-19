package com.leclowndu93150.thaumaturge.network;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.content.research.table.BlockEntityResearchTable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ServerboundTableDuplicatePayload(BlockPos pos) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ServerboundTableDuplicatePayload> TYPE = new CustomPacketPayload.Type<>(TCIds.rl("table_duplicate"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundTableDuplicatePayload> STREAM_CODEC = StreamCodec.composite(BlockPos.STREAM_CODEC, ServerboundTableDuplicatePayload::pos,
            ServerboundTableDuplicatePayload::new);

    public static void handle(ServerboundTableDuplicatePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }
            if (payload.pos().distToCenterSqr(player.getX(), player.getY(), player.getZ()) > 64.0) {
                return;
            }
            if (player.level().getBlockEntity(payload.pos()) instanceof BlockEntityResearchTable table) {
                table.duplicateNote(player);
            }
        });
    }

    @Override
    public CustomPacketPayload.Type<ServerboundTableDuplicatePayload> type() {
        return TYPE;
    }
}
