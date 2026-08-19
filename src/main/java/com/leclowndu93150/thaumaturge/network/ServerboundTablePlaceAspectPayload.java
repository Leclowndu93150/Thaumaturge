package com.leclowndu93150.thaumaturge.network;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.content.research.note.HexGrid;
import com.leclowndu93150.thaumaturge.content.research.table.BlockEntityResearchTable;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ServerboundTablePlaceAspectPayload(BlockPos pos, int q, int r, Optional<Identifier> aspect) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ServerboundTablePlaceAspectPayload> TYPE = new CustomPacketPayload.Type<>(TCIds.rl("table_place_aspect"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundTablePlaceAspectPayload> STREAM_CODEC = StreamCodec.composite(BlockPos.STREAM_CODEC, ServerboundTablePlaceAspectPayload::pos,
            ByteBufCodecs.VAR_INT, ServerboundTablePlaceAspectPayload::q, ByteBufCodecs.VAR_INT, ServerboundTablePlaceAspectPayload::r, ByteBufCodecs.optional(Identifier.STREAM_CODEC),
            ServerboundTablePlaceAspectPayload::aspect, ServerboundTablePlaceAspectPayload::new);

    public static void handle(ServerboundTablePlaceAspectPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }
            if (payload.pos().distToCenterSqr(player.getX(), player.getY(), player.getZ()) > 64.0) {
                return;
            }
            if (!(player.level().getBlockEntity(payload.pos()) instanceof BlockEntityResearchTable table)) {
                return;
            }
            Holder<IAspect> holder = payload.aspect().flatMap(id -> player.registryAccess().lookupOrThrow(IAspect.REGISTRY_KEY).get(ResourceKey.create(IAspect.REGISTRY_KEY, id)))
                    .map(reference -> (Holder<IAspect>) reference).orElse(null);
            table.placeAspect(player, new HexGrid.Hex(payload.q(), payload.r()), holder);
        });
    }

    @Override
    public CustomPacketPayload.Type<ServerboundTablePlaceAspectPayload> type() {
        return TYPE;
    }
}
