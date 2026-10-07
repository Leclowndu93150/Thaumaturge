package com.leclowndu93150.thaumaturge.network;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.content.research.table.BlockEntityResearchTable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jspecify.annotations.Nullable;

public record ServerboundTableCombinePayload(
        BlockPos pos, ResourceLocation first, ResourceLocation second, boolean bonusFirst, boolean bonusSecond)
        implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ServerboundTableCombinePayload> TYPE =
            new CustomPacketPayload.Type<>(TTIds.rl("table_combine"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundTableCombinePayload> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC,
                    ServerboundTableCombinePayload::pos,
                    ResourceLocation.STREAM_CODEC,
                    ServerboundTableCombinePayload::first,
                    ResourceLocation.STREAM_CODEC,
                    ServerboundTableCombinePayload::second,
                    ByteBufCodecs.BOOL,
                    ServerboundTableCombinePayload::bonusFirst,
                    ByteBufCodecs.BOOL,
                    ServerboundTableCombinePayload::bonusSecond,
                    ServerboundTableCombinePayload::new);

    public static void handle(ServerboundTableCombinePayload payload, IPayloadContext context) {
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
            Holder<IAspect> first = resolve(player, payload.first());
            Holder<IAspect> second = resolve(player, payload.second());
            if (first != null && second != null) {
                table.combineAspects(player, first, second, payload.bonusFirst(), payload.bonusSecond());
            }
        });
    }

    private static @Nullable Holder<IAspect> resolve(ServerPlayer player, ResourceLocation id) {
        return player.registryAccess()
                .lookupOrThrow(IAspect.REGISTRY_KEY)
                .get(ResourceKey.create(IAspect.REGISTRY_KEY, id))
                .map(reference -> (Holder<IAspect>) reference)
                .orElse(null);
    }

    @Override
    public CustomPacketPayload.Type<ServerboundTableCombinePayload> type() {
        return TYPE;
    }
}
