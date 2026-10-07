package com.leclowndu93150.thaumaturge.network;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.spell.Spell;
import com.leclowndu93150.thaumaturge.content.spell.manipulator.BlockEntityFocalManipulator;
import com.leclowndu93150.thaumaturge.content.spell.manipulator.MenuFocalManipulator;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ServerboundSpellDraftPayload(BlockPos pos, String name, Spell spell) implements CustomPacketPayload {
    public static final Type<ServerboundSpellDraftPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(TTIds.MODID, "spell_draft"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundSpellDraftPayload> STREAM_CODEC = StreamCodec.composite(BlockPos.STREAM_CODEC, ServerboundSpellDraftPayload::pos,
            ByteBufCodecs.stringUtf8(BlockEntityFocalManipulator.MAX_NAME_LENGTH), ServerboundSpellDraftPayload::name, Spell.STREAM_CODEC, ServerboundSpellDraftPayload::spell,
            ServerboundSpellDraftPayload::new);

    private static final int MAX_NODES = 64;
    private static final int MAX_DEPTH = 24;

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ServerboundSpellDraftPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            Player player = ctx.player();
            if (!(player.containerMenu instanceof MenuFocalManipulator menu) || !menu.pos().equals(payload.pos) || !menu.stillValid(player)) {
                return;
            }
            if (payload.spell.nodes().size() > MAX_NODES || payload.spell.root().depth() > MAX_DEPTH) {
                return;
            }
            menu.table().ifPresent(table -> table.acceptDraft(payload.spell, payload.name));
        });
    }
}
