package com.leclowndu93150.thaumaturge.network;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.content.golem.GolemProperties;
import com.leclowndu93150.thaumaturge.content.golem.press.BlockEntityGolemBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ServerboundGolemPressPayload(BlockPos pos, GolemProperties props, boolean craft) implements CustomPacketPayload {
    public static final Type<ServerboundGolemPressPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(TCIds.MODID, "golem_press"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundGolemPressPayload> STREAM_CODEC = StreamCodec.composite(BlockPos.STREAM_CODEC, ServerboundGolemPressPayload::pos,
            GolemProperties.STREAM_CODEC, ServerboundGolemPressPayload::props, ByteBufCodecs.BOOL, ServerboundGolemPressPayload::craft, ServerboundGolemPressPayload::new);

    public static void handle(ServerboundGolemPressPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }
            if (!(player.level().getBlockEntity(payload.pos()) instanceof BlockEntityGolemBuilder builder)) {
                return;
            }
            if (payload.craft()) {
                builder.startCraft(payload.props(), player);
            } else {
                boolean[] stuff = builder.checkCraft(payload.props());
                byte[] bytes = new byte[stuff.length];
                for (int i = 0; i < stuff.length; i++) {
                    bytes[i] = (byte) (stuff[i] ? 1 : 0);
                }
                PacketDistributor.sendToPlayer(player, new ClientboundGolemPressStuffPayload(payload.pos(), bytes));
            }
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
