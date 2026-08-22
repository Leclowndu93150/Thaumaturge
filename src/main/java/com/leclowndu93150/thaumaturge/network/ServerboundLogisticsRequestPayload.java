package com.leclowndu93150.thaumaturge.network;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.content.golem.logistics.MenuGolemLogistics;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ServerboundLogisticsRequestPayload(ItemStack stack, int amount) implements CustomPacketPayload {
    public static final Type<ServerboundLogisticsRequestPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(TCIds.MODID, "logistics_request"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundLogisticsRequestPayload> STREAM_CODEC = StreamCodec.composite(ItemStack.STREAM_CODEC, ServerboundLogisticsRequestPayload::stack,
            ByteBufCodecs.VAR_INT, ServerboundLogisticsRequestPayload::amount, ServerboundLogisticsRequestPayload::new);

    public static void handle(ServerboundLogisticsRequestPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player && player.containerMenu instanceof MenuGolemLogistics menu) {
                menu.request(payload.stack(), payload.amount());
            }
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
