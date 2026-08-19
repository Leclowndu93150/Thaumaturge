package com.leclowndu93150.thaumaturge.network;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.casters.ICaster;
import com.leclowndu93150.thaumaturge.content.casters.CasterManager;
import com.leclowndu93150.thaumaturge.content.equipment.ElementalShovelItem;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ServerboundCasterKeyPayload(int mod) implements CustomPacketPayload {
    public static final Type<ServerboundCasterKeyPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(TCIds.MODID, "caster_key"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundCasterKeyPayload> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.VAR_INT, ServerboundCasterKeyPayload::mod,
            ServerboundCasterKeyPayload::new);

    public static void handle(ServerboundCasterKeyPayload payload, IPayloadContext ctx) {
        Player player = ctx.player();
        ItemStack main = player.getMainHandItem();
        if (main.getItem() instanceof ElementalShovelItem) {
            ElementalShovelItem.cycleOrientation(main);
            return;
        }
        if (main.getItem() instanceof ICaster) {
            CasterManager.toggleMisc(main, player.level(), player, payload.mod());
            return;
        }
        ItemStack off = player.getOffhandItem();
        if (off.getItem() instanceof ICaster) {
            CasterManager.toggleMisc(off, player.level(), player, payload.mod());
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
