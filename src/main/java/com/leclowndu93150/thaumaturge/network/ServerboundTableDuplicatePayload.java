package com.leclowndu93150.thaumaturge.network;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.content.research.table.BlockEntityResearchTable;
import com.leclowndu93150.thaumaturge.content.research.table.MenuResearchTable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ServerboundTableDuplicatePayload(BlockPos pos) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ServerboundTableDuplicatePayload> TYPE =
            new CustomPacketPayload.Type<>(TTIds.rl("table_duplicate"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundTableDuplicatePayload> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC,
                    ServerboundTableDuplicatePayload::pos,
                    ServerboundTableDuplicatePayload::new);

    public static void handle(ServerboundTableDuplicatePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            BlockEntityResearchTable table = BlockMenuGuard.target(context, payload.pos(), MenuResearchTable.class);
            if (table != null) {
                table.duplicateNote((ServerPlayer) context.player());
            }
        });
    }

    @Override
    public CustomPacketPayload.Type<ServerboundTableDuplicatePayload> type() {
        return TYPE;
    }
}
