package com.leclowndu93150.thaumaturge.network;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.content.essentia.thaumatorium.BlockEntityThaumatorium;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ServerboundThaumatoriumTogglePayload(BlockPos pos, Identifier recipeId) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ServerboundThaumatoriumTogglePayload> TYPE = new CustomPacketPayload.Type<>(TCIds.rl("thaumatorium_toggle"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundThaumatoriumTogglePayload> STREAM_CODEC = StreamCodec.composite(BlockPos.STREAM_CODEC,
            ServerboundThaumatoriumTogglePayload::pos, Identifier.STREAM_CODEC, ServerboundThaumatoriumTogglePayload::recipeId, ServerboundThaumatoriumTogglePayload::new);

    public static void handle(ServerboundThaumatoriumTogglePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player) || !(player.level() instanceof ServerLevel level)) {
                return;
            }
            if (payload.pos().distToCenterSqr(player.getX(), player.getY(), player.getZ()) > 64.0) {
                return;
            }
            if (level.getBlockEntity(payload.pos()) instanceof BlockEntityThaumatorium machine) {
                machine.toggleRecipe(level, player, payload.recipeId());
            }
        });
    }

    @Override
    public CustomPacketPayload.Type<ServerboundThaumatoriumTogglePayload> type() {
        return TYPE;
    }
}
