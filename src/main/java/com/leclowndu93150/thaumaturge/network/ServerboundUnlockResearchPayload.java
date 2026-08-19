package com.leclowndu93150.thaumaturge.network;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.content.research.ResearchManager;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ServerboundUnlockResearchPayload(Identifier research) implements CustomPacketPayload {
    public static final Type<ServerboundUnlockResearchPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(TCIds.MODID, "unlock_research"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundUnlockResearchPayload> STREAM_CODEC = StreamCodec.composite(Identifier.STREAM_CODEC, ServerboundUnlockResearchPayload::research,
            ServerboundUnlockResearchPayload::new);

    public static void handle(ServerboundUnlockResearchPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                ResearchManager.unlock(player, payload.research());
            }
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
