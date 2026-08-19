package com.leclowndu93150.thaumaturge.network;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.casters.ICaster;
import com.leclowndu93150.thaumaturge.content.casters.CasterManager;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ServerboundFocusChangePayload(String focusKey) implements CustomPacketPayload {
    public static final Type<ServerboundFocusChangePayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(TCIds.MODID, "focus_change"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundFocusChangePayload> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.STRING_UTF8, ServerboundFocusChangePayload::focusKey,
            ServerboundFocusChangePayload::new);

    public static void handle(ServerboundFocusChangePayload payload, IPayloadContext ctx) {
        Player player = ctx.player();
        ItemStack main = player.getMainHandItem();
        if (main.getItem() instanceof ICaster) {
            CasterManager.changeFocus(main, player.level(), player, payload.focusKey());
            return;
        }
        ItemStack off = player.getOffhandItem();
        if (off.getItem() instanceof ICaster) {
            CasterManager.changeFocus(off, player.level(), player, payload.focusKey());
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
