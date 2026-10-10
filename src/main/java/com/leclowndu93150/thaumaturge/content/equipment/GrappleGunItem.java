package com.leclowndu93150.thaumaturge.content.equipment;

import com.leclowndu93150.thaumaturge.api.items.ChargeDisplay;
import com.leclowndu93150.thaumaturge.api.items.ChargeProfile;
import com.leclowndu93150.thaumaturge.api.items.RechargeAccess;
import com.leclowndu93150.thaumaturge.content.entity.projectile.EntityGrapple;
import com.leclowndu93150.thaumaturge.registry.TTAttachments;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import com.leclowndu93150.thaumaturge.registry.TTEntities;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class GrappleGunItem extends Item {
    private static final int CHARGE_CAPACITY = 100;
    private static final int CHARGE_COST = 1;
    private static final int NO_GRAPPLE = -1;
    private static final float LAUNCH_SPEED = 1.5F;
    private static final float LAUNCH_INACCURACY = 0.0F;
    private static final float RIGHT_YAW_OFFSET_DEGREES = 90.0F;
    private static final float PITCH_OFFSET = -3.0F;
    private static final float FIRE_VOLUME = 2.4F;
    private static final float FIRE_PITCH_BASE = 0.7F;
    private static final float FIRE_PITCH_SPREAD = 0.15F;
    private static final double FORWARD_REACH = 0.9;
    private static final double SIDE_SHIFT = 0.4;
    private static final LaunchParameters LAUNCH = new LaunchParameters(PITCH_OFFSET, LAUNCH_SPEED, LAUNCH_INACCURACY);

    public GrappleGunItem(Properties properties) {
        super(properties.component(TTDataComponents.RECHARGEABLE.get(), new ChargeProfile(CHARGE_CAPACITY, ChargeDisplay.ALWAYS)));
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level instanceof ServerLevel server) {
            float pitch = FIRE_PITCH_BASE + server.getRandom().nextFloat() * FIRE_PITCH_SPREAD;
            server.playSound(null, player.getX(), player.getY(), player.getZ(), TTSounds.ICE.get(), SoundSource.PLAYERS, FIRE_VOLUME, pitch);
            ItemStack stack = player.getItemInHand(hand);
            if (RechargeAccess.getCharge(stack) >= CHARGE_COST) {
                fire(server, player, hand, stack);
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, @Nullable EquipmentSlot slot) {
        super.inventoryTick(stack, level, entity, slot);
        if (LoadedState.isLoaded(stack) && !hasLiveGrapple(level, entity)) {
            LoadedState.clear(stack);
        }
    }

    private static boolean hasLiveGrapple(ServerLevel level, Entity owner) {
        int id = owner.getData(TTAttachments.GRAPPLE_ID.get());
        return id != NO_GRAPPLE && level.getEntity(id) instanceof EntityGrapple grapple && grapple.isAlive();
    }

    private static void fire(Level level, Player player, InteractionHand hand, ItemStack stack) {
        Vec3 spawn = spawnPoint(player, hand);
        EntityGrapple grapple = new EntityGrapple(TTEntities.GRAPPLE.get(), level, player, hand);
        grapple.setPos(spawn.x, grapple.getY(), spawn.z);
        launch(grapple, player, LAUNCH);
        if (level.addFreshEntity(grapple)) {
            onSpawned(stack, player);
        }
    }

    private static Vec3 spawnPoint(Player player, InteractionHand hand) {
        HumanoidArm arm = hand == InteractionHand.MAIN_HAND ? player.getMainArm() : player.getMainArm().getOpposite();
        float yaw = player.getYRot();
        float sideYaw = arm == HumanoidArm.RIGHT ? yaw + RIGHT_YAW_OFFSET_DEGREES : yaw - RIGHT_YAW_OFFSET_DEGREES;
        Vec3 forward = horizontalHeading(yaw).scale(FORWARD_REACH);
        Vec3 side = horizontalHeading(sideYaw).scale(SIDE_SHIFT);
        return player.position().add(forward).add(side);
    }

    private static Vec3 horizontalHeading(float yawDegrees) {
        float radians = yawDegrees * Mth.DEG_TO_RAD;
        return new Vec3(-Mth.sin(radians), 0.0, Mth.cos(radians));
    }

    private static void launch(EntityGrapple grapple, Player player, LaunchParameters parameters) {
        grapple.shootFromRotation(player, player.getXRot(), player.getYRot(), parameters.pitchOffset(), parameters.speed(), parameters.inaccuracy());
    }

    private static void onSpawned(ItemStack stack, Player player) {
        RechargeAccess.consumeCharge(stack, player, CHARGE_COST);
        LoadedState.set(stack);
    }

    private record LaunchParameters(float pitchOffset, float speed, float inaccuracy) {
    }

    private static final class LoadedState {
        private LoadedState() {}

        static boolean isLoaded(ItemStack stack) {
            return stack.getOrDefault(TTDataComponents.GRAPPLE_LOADED.get(), false);
        }

        static void set(ItemStack stack) {
            stack.set(TTDataComponents.GRAPPLE_LOADED.get(), true);
        }

        static void clear(ItemStack stack) {
            stack.remove(TTDataComponents.GRAPPLE_LOADED.get());
        }
    }
}
