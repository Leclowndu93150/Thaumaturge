package com.leclowndu93150.thaumaturge.network;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.compat.curio.ThaumaturgeCuriosCompat;
import com.leclowndu93150.thaumaturge.content.equipment.bauble.BaubleEvents;
import com.leclowndu93150.thaumaturge.registry.TTAttachments;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ServerboundCloudJumpPayload() implements CustomPacketPayload {
    public static final Type<ServerboundCloudJumpPayload> TYPE = new Type<>(TTIds.rl("cloud_jump"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundCloudJumpPayload> STREAM_CODEC =
            StreamCodec.unit(new ServerboundCloudJumpPayload());

    public static void handle(ServerboundCloudJumpPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            Player player = ctx.player();
            if (!ModList.get().isLoaded(TTIds.CURIOS)
                    || !ThaumaturgeCuriosCompat.isCurioEquipped(player, TTItems.CLOUD_RING.get())) {
                return;
            }
            if (player.onGround() || player.isInWater() || BaubleEvents.hasPendingCloudJump(player)) {
                return;
            }
            player.resetFallDistance();
            player.setData(TTAttachments.CLOUD_JUMP_TIME, player.level().getGameTime());
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
