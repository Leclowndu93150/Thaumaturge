package com.leclowndu93150.thaumaturge.content.equipment.runic;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.content.particle.WardFlashParticleOptions;
import com.leclowndu93150.thaumaturge.registry.TTAttachments;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = TTIds.MODID)
public final class RunicShieldingEvents {
    private static final float EFFECT_VOLUME = 0.66F;

    private RunicShieldingEvents() {}

    private static final float PITCH_BASE = 1.1F;
    private static final float PITCH_SPREAD = 0.1F;

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player entity = event.getEntity();
        if (!entity.isSpectator()) {
            tickShield(entity);
        }
    }

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent.Pre event) {
        if (event.getEntity() instanceof ServerPlayer player && hasActiveShield(player)) {
            playEffect(player);
        }
    }

    private static void tickShield(Player entity) {
        if (entity instanceof ServerPlayer serverPlayer) {
            RunicShielding.tick(serverPlayer);
        }
    }

    private static void playEffect(ServerPlayer player) {
        float pitch = PITCH_BASE + player.getRandom().nextFloat() * PITCH_SPREAD;
        player.level()
                .playSound(
                        null,
                        player.blockPosition(),
                        TTSounds.RUNICSHIELDEFFECT.get(),
                        SoundSource.PLAYERS,
                        EFFECT_VOLUME,
                        pitch);
        Vec3 center = player.position().add(0.0, player.getBbHeight() / 2.0, 0.0);
        BlockPos particleOrigin = BlockPos.containing(center);
        for (Direction face : Direction.values()) {
            player.serverLevel()
                    .sendParticles(
                            WardFlashParticleOptions.at(particleOrigin, face, center),
                            center.x,
                            center.y,
                            center.z,
                            1,
                            0.0,
                            0.0,
                            0.0,
                            0.0);
        }
    }

    private static boolean hasActiveShield(ServerPlayer player) {
        return player.getAbsorptionAmount() > 0.0F && player.getData(TTAttachments.RUNIC_SHIELD.get()).maxCharge > 0;
    }
}
