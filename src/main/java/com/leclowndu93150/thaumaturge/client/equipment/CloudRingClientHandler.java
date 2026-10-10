package com.leclowndu93150.thaumaturge.client.equipment;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.compat.curio.ThaumaturgeCuriosCompat;
import com.leclowndu93150.thaumaturge.network.ServerboundCloudJumpPayload;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.common.CommonHooks;

@EventBusSubscriber(modid = TTIds.MODID, value = Dist.CLIENT)
public final class CloudRingClientHandler {
    private static final int MIN_AIRBORNE_TICKS = 2;
    private static final double HALF = 0.5;
    private static final int PUFF_COUNT = 7;
    private static final double PUFF_HEIGHT = 0.1;
    private static final double PUFF_SPREAD = 0.06;
    private static final double PUFF_SINK = 0.02;
    private static final float SOUND_VOLUME = 0.12F;
    private static final float SOUND_PITCH_BASE = 1.0F;
    private static final float SOUND_PITCH_NOISE = 0.05F;
    private static final double JUMP_VELOCITY = 0.75;
    private static final double JUMP_BOOST_STEP = 0.1;
    private static final double SPRINT_IMPULSE = 0.2;

    private static boolean extraJumpSpent;
    private static boolean jumpHeld;
    private static int airborneTicks;

    private CloudRingClientHandler() {}

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null || !ModList.get().isLoaded(TTIds.CURIOS) || !ThaumaturgeCuriosCompat.isCurioEquipped(player, TTItems.CLOUD_RING.get())) {
            reset();
            return;
        }
        boolean down = minecraft.options.keyJump.isDown();
        boolean pressed = down && !jumpHeld;
        jumpHeld = down;
        if (player.onGround() || player.isInWater()) {
            extraJumpSpent = false;
            airborneTicks = 0;
            return;
        }
        airborneTicks++;
        if (pressed && !extraJumpSpent && airborneTicks >= MIN_AIRBORNE_TICKS) {
            extraJumpSpent = true;
            performExtraJump(player);
        }
    }

    private static void reset() {
        extraJumpSpent = false;
        jumpHeld = false;
        airborneTicks = 0;
    }

    private static void performExtraJump(LocalPlayer player) {
        Vec3 motion = player.getDeltaMovement();
        Vec3 launched = new Vec3(motion.x, launchSpeed(player), motion.z);
        if (player.isSprinting()) {
            float heading = player.getYRot() * Mth.DEG_TO_RAD;
            launched = launched.add(-Mth.sin(heading) * SPRINT_IMPULSE, 0.0, Mth.cos(heading) * SPRINT_IMPULSE);
        }
        player.setDeltaMovement(launched);
        player.resetFallDistance();
        CommonHooks.onLivingJump(player);
        ClientPacketDistributor.sendToServer(new ServerboundCloudJumpPayload());
        Level level = player.level();
        RandomSource random = player.getRandom();
        puff(level, player, random);
        float pitch = SOUND_PITCH_BASE + (float) random.nextGaussian() * SOUND_PITCH_NOISE;
        level.playLocalSound(player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, SOUND_VOLUME, pitch, false);
    }

    private static double launchSpeed(LocalPlayer player) {
        MobEffectInstance boost = player.getEffect(MobEffects.JUMP_BOOST);
        return boost == null ? JUMP_VELOCITY : JUMP_VELOCITY + JUMP_BOOST_STEP * (boost.getAmplifier() + 1);
    }

    private static void puff(Level level, LocalPlayer player, RandomSource random) {
        double feet = player.getY() + PUFF_HEIGHT;
        for (int i = 0; i < PUFF_COUNT; i++) {
            double x = player.getX() + (random.nextDouble() - HALF) * player.getBbWidth();
            double z = player.getZ() + (random.nextDouble() - HALF) * player.getBbWidth();
            double vx = (random.nextDouble() - HALF) * PUFF_SPREAD;
            double vz = (random.nextDouble() - HALF) * PUFF_SPREAD;
            level.addParticle(ParticleTypes.CLOUD, x, feet, z, vx, -PUFF_SINK * random.nextDouble(), vz);
        }
    }
}
