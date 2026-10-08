package com.leclowndu93150.thaumaturge.network;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.content.research.ResearchManager;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ServerboundUnlockResearchPayload(ResourceLocation research) implements CustomPacketPayload {
    public static final Type<ServerboundUnlockResearchPayload> TYPE = new Type<>(TTIds.rl("unlock_research"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundUnlockResearchPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ResourceLocation.STREAM_CODEC,
                    ServerboundUnlockResearchPayload::research,
                    ServerboundUnlockResearchPayload::new);

    public static void handle(ServerboundUnlockResearchPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                ResearchManager.unlockRequested(player, payload.research());
            }
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
