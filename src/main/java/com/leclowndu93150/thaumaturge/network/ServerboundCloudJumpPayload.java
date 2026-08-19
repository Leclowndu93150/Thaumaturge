package com.leclowndu93150.thaumaturge.network;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.compat.curio.ThaumaturgeCuriosCompat;
import com.leclowndu93150.thaumaturge.registry.TCAttachments;
import com.leclowndu93150.thaumaturge.registry.TCItems;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ServerboundCloudJumpPayload() implements CustomPacketPayload {
    public static final Type<ServerboundCloudJumpPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(TCIds.MODID, "cloud_jump"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundCloudJumpPayload> STREAM_CODEC = StreamCodec.unit(new ServerboundCloudJumpPayload());

    public static void handle(ServerboundCloudJumpPayload payload, IPayloadContext ctx) {
        if (ModList.get().isLoaded(TCIds.CURIOS) && ThaumaturgeCuriosCompat.isCurioEquipped(ctx.player(), TCItems.CLOUD_RING.get())) {
            ctx.player().resetFallDistance();
            ctx.player().setData(TCAttachments.CLOUD_JUMP_TIME, ctx.player().level().getGameTime());
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
