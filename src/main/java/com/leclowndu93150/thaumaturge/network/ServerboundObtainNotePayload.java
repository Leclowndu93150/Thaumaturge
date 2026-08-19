package com.leclowndu93150.thaumaturge.network;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.content.research.note.ResearchNotes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ServerboundObtainNotePayload(Identifier entry, int ordinal) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ServerboundObtainNotePayload> TYPE = new CustomPacketPayload.Type<>(TCIds.rl("obtain_note"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundObtainNotePayload> STREAM_CODEC = StreamCodec.composite(Identifier.STREAM_CODEC, ServerboundObtainNotePayload::entry,
            ByteBufCodecs.VAR_INT, ServerboundObtainNotePayload::ordinal, ServerboundObtainNotePayload::new);

    public static void handle(ServerboundObtainNotePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                ResearchNotes.obtainNote(player, payload.entry(), payload.ordinal());
            }
        });
    }

    @Override
    public CustomPacketPayload.Type<ServerboundObtainNotePayload> type() {
        return TYPE;
    }
}
