package dev.overgrown.thaumaturge.spell.networking;

import dev.overgrown.thaumaturge.spell.utils.SpellHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public final class SpellCastPacket {

    public static final Identifier ID = new Identifier("thaumaturge", "spell_cast");

    public enum KeyType { PRIMARY, SECONDARY, TERNARY }

    @Environment(EnvType.CLIENT)
    public static void send(KeyType keyType) {
        var mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.getNetworkHandler() == null) return;
        if (!ClientPlayNetworking.canSend(ID)) return;

        PacketByteBuf out = PacketByteBufs.create();
        out.writeEnumConstant(keyType);
        ClientPlayNetworking.send(ID, out);
    }

    public static void registerServer() {
        ServerPlayNetworking.registerGlobalReceiver(ID, SpellCastPacket::handle);
    }

    private static void handle(MinecraftServer server, ServerPlayerEntity player,
                               ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender response) {
        KeyType keyType = buf.readEnumConstant(KeyType.class);
        server.execute(() -> {
            if (player.isRemoved() || player.isSpectator()) return;
            SpellHandler.tryCastSpell(player, keyType);
        });
    }

}