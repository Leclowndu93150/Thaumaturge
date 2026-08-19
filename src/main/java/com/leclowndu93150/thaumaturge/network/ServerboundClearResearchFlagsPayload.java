package com.leclowndu93150.thaumaturge.network;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.capability.IPlayerKnowledge;
import com.leclowndu93150.thaumaturge.api.capability.KnowledgeAccess;
import com.leclowndu93150.thaumaturge.api.capability.ResearchFlag;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ServerboundClearResearchFlagsPayload(Identifier research, List<ResearchFlag> flags) implements CustomPacketPayload {
    public static final Type<ServerboundClearResearchFlagsPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(TCIds.MODID, "clear_research_flags"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundClearResearchFlagsPayload> STREAM_CODEC = StreamCodec.composite(Identifier.STREAM_CODEC,
            ServerboundClearResearchFlagsPayload::research, ResearchFlag.STREAM_CODEC.apply(ByteBufCodecs.list()), ServerboundClearResearchFlagsPayload::flags,
            ServerboundClearResearchFlagsPayload::new);

    public static void handle(ServerboundClearResearchFlagsPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player))
                return;
            IPlayerKnowledge knowledge = KnowledgeAccess.of(player);
            if (!knowledge.isResearchKnown(payload.research()))
                return;
            boolean changed = false;
            for (ResearchFlag flag : payload.flags()) {
                if (knowledge.clearResearchFlag(payload.research(), flag)) {
                    changed = true;
                }
            }
            if (changed)
                knowledge.sync(player);
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
