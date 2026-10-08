package com.leclowndu93150.thaumaturge.network;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.content.essentia.thaumatorium.BlockEntityThaumatorium;
import com.leclowndu93150.thaumaturge.content.essentia.thaumatorium.MenuThaumatorium;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ServerboundThaumatoriumTogglePayload(BlockPos pos, ResourceLocation recipeId)
        implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ServerboundThaumatoriumTogglePayload> TYPE =
            new CustomPacketPayload.Type<>(TTIds.rl("thaumatorium_toggle"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundThaumatoriumTogglePayload> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC,
                    ServerboundThaumatoriumTogglePayload::pos,
                    ResourceLocation.STREAM_CODEC,
                    ServerboundThaumatoriumTogglePayload::recipeId,
                    ServerboundThaumatoriumTogglePayload::new);

    public static void handle(ServerboundThaumatoriumTogglePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            BlockEntityThaumatorium machine = BlockMenuGuard.target(context, payload.pos(), MenuThaumatorium.class);
            if (machine != null
                    && context.player() instanceof ServerPlayer player
                    && player.level() instanceof ServerLevel level) {
                machine.toggleRecipe(level, player, payload.recipeId());
            }
        });
    }

    @Override
    public CustomPacketPayload.Type<ServerboundThaumatoriumTogglePayload> type() {
        return TYPE;
    }
}
