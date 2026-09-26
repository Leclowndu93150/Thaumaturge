package com.leclowndu93150.thaumaturge.network;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.content.golem.logistics.MenuGolemLogistics;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ServerboundLogisticsSearchPayload(String text) implements CustomPacketPayload {
    public static final Type<ServerboundLogisticsSearchPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(TCIds.MODID, "logistics_search"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundLogisticsSearchPayload> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.stringUtf8(MenuGolemLogistics.SEARCH_MAX_LENGTH),
            ServerboundLogisticsSearchPayload::text, ServerboundLogisticsSearchPayload::new);

    public static void handle(ServerboundLogisticsSearchPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player && player.containerMenu instanceof MenuGolemLogistics menu) {
                menu.setSearchText(payload.text());
            }
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
