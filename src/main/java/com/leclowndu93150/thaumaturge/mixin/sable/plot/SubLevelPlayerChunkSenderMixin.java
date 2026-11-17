package com.leclowndu93150.thaumaturge.mixin.sable.plot;

import com.leclowndu93150.thaumaturge.content.warding.WardHandler;
import com.leclowndu93150.thaumaturge.network.ClientboundWardChunkPayload;
import dev.ryanhcode.sable.sublevel.plot.SubLevelPlayerChunkSender;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.lighting.LevelLightEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SubLevelPlayerChunkSender.class)
public class SubLevelPlayerChunkSenderMixin {
    @Inject(method = "sendChunk", at = @At("TAIL"))
    private static void thaumaturge$syncWards(
            Consumer<Packet<? super ClientGamePacketListener>> send,
            LevelLightEngine lighting,
            LevelChunk chunk,
            CallbackInfo ci) {
        // Sable bypasses ChunkWatchEvent.Sent. A full snapshot also clears reused plot coordinates.
        ClientboundWardChunkPayload payload = WardHandler.chunkPayload(chunk);
        if (payload == null) {
            payload = new ClientboundWardChunkPayload(chunk.getPos(), List.of());
        }
        send.accept(new ClientboundCustomPayloadPacket(payload));
    }
}
