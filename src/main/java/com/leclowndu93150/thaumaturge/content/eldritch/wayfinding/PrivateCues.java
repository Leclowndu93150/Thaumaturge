package com.leclowndu93150.thaumaturge.content.eldritch.wayfinding;

import com.leclowndu93150.thaumaturge.network.effect.ClientboundSpawnParticlePayload;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

final class PrivateCues {
    private PrivateCues() {}

    static void chime(ServerPlayer player, Vec3 at, SoundEvent sound, float volume, float pitch) {
        player.connection.send(new ClientboundSoundPacket(
                BuiltInRegistries.SOUND_EVENT.wrapAsHolder(sound),
                SoundSource.PLAYERS,
                at.x,
                at.y,
                at.z,
                volume,
                pitch,
                player.getRandom().nextLong()));
    }

    static void particle(ServerPlayer player, ParticleOptions options, Vec3 at, Vec3 velocity) {
        PacketDistributor.sendToPlayer(
                player,
                new ClientboundSpawnParticlePayload(options, at.x, at.y, at.z, velocity.x, velocity.y, velocity.z));
    }
}
