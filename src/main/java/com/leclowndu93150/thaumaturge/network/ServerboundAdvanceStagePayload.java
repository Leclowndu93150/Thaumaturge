package com.leclowndu93150.thaumaturge.network;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.content.research.ResearchManager;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ServerboundAdvanceStagePayload(Identifier research) implements CustomPacketPayload {
    public static final Type<ServerboundAdvanceStagePayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(TCIds.MODID, "advance_stage"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundAdvanceStagePayload> STREAM_CODEC = StreamCodec.composite(Identifier.STREAM_CODEC, ServerboundAdvanceStagePayload::research,
            ServerboundAdvanceStagePayload::new);

    public static void handle(ServerboundAdvanceStagePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                ResearchManager.advanceStage(player, payload.research());
            }
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
