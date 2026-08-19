package com.leclowndu93150.thaumaturge.client.equipment;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.compat.curio.ThaumaturgeCuriosCompat;
import com.leclowndu93150.thaumaturge.network.ServerboundCloudJumpPayload;
import com.leclowndu93150.thaumaturge.registry.TCItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.common.CommonHooks;

@EventBusSubscriber(modid = TCIds.MODID, value = Dist.CLIENT)
public final class CloudRingClientHandler {
    private static final double JUMP_VELOCITY = 0.75;
    private static final float JUMP_BOOST_BONUS = 0.1F;
    private static final float SPRINT_BOOST = 0.2F;
    private static final float SOUND_VOLUME = 0.1F;
    private static final int POOF_COUNT = 8;

    private static final int MIN_AIRBORNE_TICKS = 2;

    private static boolean airJumpUsed;
    private static boolean jumpWasDown;
    private static int airborneTicks;

    private CloudRingClientHandler() {}

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || !ModList.get().isLoaded(TCIds.CURIOS) || !ThaumaturgeCuriosCompat.isCurioEquipped(player, TCItems.CLOUD_RING.get())) {
            airJumpUsed = false;
            jumpWasDown = false;
            airborneTicks = 0;
            return;
        }
        boolean jumpDown = Minecraft.getInstance().options.keyJump.isDown();
        boolean jumpPressed = jumpDown && !jumpWasDown;
        jumpWasDown = jumpDown;
        if (player.onGround() || player.isInWater()) {
            airJumpUsed = false;
            airborneTicks = 0;
            return;
        }
        airborneTicks++;
        if (jumpPressed && !airJumpUsed && airborneTicks >= MIN_AIRBORNE_TICKS) {
            airJump(player);
            airJumpUsed = true;
        }
    }

    private static void airJump(LocalPlayer player) {
        for (int i = 0; i < POOF_COUNT; i++) {
            player.level().addParticle(ParticleTypes.POOF, player.getX() + (player.getRandom().nextFloat() - 0.5) * player.getBbWidth(), player.getY() + 0.5,
                    player.getZ() + (player.getRandom().nextFloat() - 0.5) * player.getBbWidth(), 0.0, 0.0, 0.0);
        }
        player.level().playLocalSound(player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, SOUND_VOLUME,
                1.0F + (float) player.getRandom().nextGaussian() * 0.05F, false);
        double velocityY = JUMP_VELOCITY;
        if (player.hasEffect(MobEffects.JUMP_BOOST)) {
            velocityY += (player.getEffect(MobEffects.JUMP_BOOST).getAmplifier() + 1) * JUMP_BOOST_BONUS;
        }
        Vec3 movement = player.getDeltaMovement();
        double velocityX = movement.x;
        double velocityZ = movement.z;
        if (player.isSprinting()) {
            float yaw = player.getYRot() * Mth.DEG_TO_RAD;
            velocityX -= Mth.sin(yaw) * SPRINT_BOOST;
            velocityZ += Mth.cos(yaw) * SPRINT_BOOST;
        }
        player.setDeltaMovement(velocityX, velocityY, velocityZ);
        player.resetFallDistance();
        ClientPacketDistributor.sendToServer(new ServerboundCloudJumpPayload());
        CommonHooks.onLivingJump(player);
    }
}
