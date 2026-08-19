package com.leclowndu93150.thaumaturge.content.effect;

import com.leclowndu93150.thaumaturge.content.particle.VisSparkleParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.WispParticleOptions;
import com.leclowndu93150.thaumaturge.network.effect.ClientboundSpawnParticlePayload;
import com.leclowndu93150.thaumaturge.network.effect.ClientboundStreamEffectPayload;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

public final class EffectDispatch {
    private static final double DEFAULT_RADIUS = 64.0;

    private EffectDispatch() {}

    public static void spawnEssentiaStream(ServerLevel level, Vec3 from, Vec3 to, int color, int typeTag) {
        spawnEssentiaStream(level, from, to, color, typeTag, level.getRandom().nextInt(8), 0.15F, 20, 0.0);
    }

    public static void spawnEssentiaStream(ServerLevel level, Vec3 from, Vec3 to, int color, int typeTag, int count, float scale, int extend, double my) {
        EssentiaSourceEffectTracker.dispatch(level, from, to, color, typeTag, count, scale, extend, my);
    }

    static void broadcastEssentiaStream(ServerLevel level, Vec3 from, Vec3 to, int color, int typeTag, int count, float scale, int extend, double my) {
        ClientboundStreamEffectPayload payload = ClientboundStreamEffectPayload.essentia(from.x, from.y, from.z, to.x, to.y, to.z, color, count, scale, extend, my);
        PacketDistributor.sendToPlayersNear(level, null, from.x, from.y, from.z, DEFAULT_RADIUS, payload);
    }

    public static void spawnVisSparkle(ServerLevel level, Vec3 origin, Vec3 target) {
        VisSparkleParticleOptions data = new VisSparkleParticleOptions(target.x, target.y, target.z);
        broadcast(level, data, origin);
    }

    public static void spawnVisSparkle(ServerLevel level, Vec3 origin, Vec3 target, int color) {
        VisSparkleParticleOptions data = new VisSparkleParticleOptions(target.x, target.y, target.z, color);
        broadcast(level, data, origin);
    }

    public static void spawnArc(ServerLevel level, Vec3 from, Vec3 to, int color, float gravityHint) {
        ClientboundStreamEffectPayload payload = ClientboundStreamEffectPayload.arc(from.x, from.y, from.z, to.x, to.y, to.z, color, gravityHint);
        PacketDistributor.sendToPlayersNear(level, null, from.x, from.y, from.z, DEFAULT_RADIUS, payload);
    }

    public static void spawnBolt(ServerLevel level, Vec3 from, Vec3 to, int color, float width) {
        ClientboundStreamEffectPayload payload = ClientboundStreamEffectPayload.bolt(from.x, from.y, from.z, to.x, to.y, to.z, color, width);
        PacketDistributor.sendToPlayersNear(level, null, from.x, from.y, from.z, DEFAULT_RADIUS, payload);
    }

    public static void spawnWisp(ServerLevel level, Vec3 origin) {
        spawnWisp(level, origin, WispParticleOptions.NO_ENTITY);
    }

    public static void spawnWisp(ServerLevel level, Vec3 origin, int entityId) {
        WispParticleOptions data = new WispParticleOptions(entityId);
        broadcast(level, data, origin);
    }

    private static void broadcast(ServerLevel level, ParticleOptions options, Vec3 origin) {
        PacketDistributor.sendToPlayersNear(level, null, origin.x, origin.y, origin.z, DEFAULT_RADIUS, new ClientboundSpawnParticlePayload(options, origin.x, origin.y, origin.z));
    }
}
