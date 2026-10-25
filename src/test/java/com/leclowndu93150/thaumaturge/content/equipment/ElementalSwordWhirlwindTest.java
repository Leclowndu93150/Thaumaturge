package com.leclowndu93150.thaumaturge.content.equipment;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.leclowndu93150.thaumaturge.client.effect.ClientEffects;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class ElementalSwordWhirlwindTest {
    @Test
    void descentIsSlowed() {
        Level level = level(false);
        LivingEntity user = user(new Vec3(1.0, -0.6, 3.0));
        user.fallDistance = 12.0F;

        TTItems.ELEMENTAL_SWORD.get().onUseTick(level, user, ItemStack.EMPTY, 71999);

        verify(user).setDeltaMovement(eq(1.0), doubleThat(value -> Math.abs(value + 0.42) < 1.0E-9), eq(3.0));
        assertEquals(10.0F, user.fallDistance, 1.0E-5F);
    }

    @Test
    void upwardOvershootUsesSourceCap() {
        Level level = level(false);
        LivingEntity user = user(new Vec3(0.0, 0.6, 0.0));

        TTItems.ELEMENTAL_SWORD.get().onUseTick(level, user, ItemStack.EMPTY, 71999);

        verify(user).setDeltaMovement(0.0, 0.2, 0.0);
    }

    @Test
    void periodicWearRunsOnServerAndNotClient() {
        ItemStack stack = mock(ItemStack.class);
        LivingEntity user = user(Vec3.ZERO);
        when(user.getUsedItemHand()).thenReturn(InteractionHand.MAIN_HAND);

        try (MockedStatic<ClientEffects> clientEffects = mockStatic(ClientEffects.class)) {
            TTItems.ELEMENTAL_SWORD.get().onUseTick(level(true), user, stack, 72000);
            verify(stack, never()).hurtAndBreak(anyInt(), any(LivingEntity.class), any(EquipmentSlot.class));
        }

        TTItems.ELEMENTAL_SWORD.get().onUseTick(level(false), user, stack, 72000);
        verify(stack).hurtAndBreak(1, user, EquipmentSlot.MAINHAND);
    }

    @Test
    void clientWhirlwindDoesNotQueryOrPushRepelTargets() {
        Level level = level(true);
        LivingEntity user = user(Vec3.ZERO);
        when(user.position()).thenReturn(Vec3.ZERO);
        LivingEntity target = mock(LivingEntity.class);
        when(target.isAlive()).thenReturn(true);
        when(target.position()).thenReturn(new Vec3(1.0, 0.0, 0.0));
        when(level.getEntitiesOfClass(eq(LivingEntity.class), any(AABB.class), any()))
                .thenReturn(List.of(target));
        when(level.getEntities(eq(user), any(AABB.class))).thenReturn(List.of(target));

        try (MockedStatic<ClientEffects> clientEffects = mockStatic(ClientEffects.class)) {
            TTItems.ELEMENTAL_SWORD.get().onUseTick(level, user, ItemStack.EMPTY, 71999);
        }

        verify(level, never()).getEntitiesOfClass(eq(LivingEntity.class), any(AABB.class), any());
        verify(level, never()).getEntities(eq(user), any(AABB.class));
        verifyNoInteractions(target);
    }

    @Test
    void serverRepelUsesLivingEntityPredicateAndSourceVector() {
        Level level = level(false);
        LivingEntity user = user(Vec3.ZERO);
        when(user.position()).thenReturn(Vec3.ZERO);

        LivingEntity mount = mock(LivingEntity.class);
        when(mount.isAlive()).thenReturn(true);
        when(user.getVehicle()).thenReturn(mount);

        LivingEntity valid = mock(LivingEntity.class);
        when(valid.isAlive()).thenReturn(true);
        when(valid.position()).thenReturn(new Vec3(1.0, 0.0, 0.0));

        LivingEntity dead = mock(LivingEntity.class);
        when(dead.isAlive()).thenReturn(false);
        Player player = mock(Player.class);
        when(player.isAlive()).thenReturn(true);

        List<LivingEntity> candidates = List.of(user, mount, dead, player, valid);
        List<LivingEntity> accepted = new ArrayList<>();
        AABB field = new AABB(0.0, 0.0, 0.0, 1.0, 1.0, 1.0).inflate(2.5);
        when(level.getEntitiesOfClass(eq(LivingEntity.class), eq(field), any())).thenAnswer(invocation -> {
            Predicate<? super LivingEntity> filter = invocation.getArgument(2);
            accepted.addAll(candidates.stream().filter(filter).toList());
            return accepted;
        });

        TTItems.ELEMENTAL_SWORD.get().onUseTick(level, user, ItemStack.EMPTY, 71999);

        assertEquals(List.of(valid), accepted);
        verify(level).getEntitiesOfClass(eq(LivingEntity.class), eq(field), any());
        verify(valid).push(new Vec3(1.0 / 2.75, 0.0, 0.0));
        verify(dead, never()).push(any(Vec3.class));
        verify(player, never()).push(any(Vec3.class));
        verify(mount, never()).push(any(Vec3.class));
        verify(user, never()).push(any(Vec3.class));
    }

    @Test
    void clientSmokeUsesFlooredEntityYAndDoublePrecisionCenter() {
        Level level = level(true);
        RandomSource random = level.getRandom();
        LivingEntity user = user(Vec3.ZERO);
        when(user.getY()).thenReturn(-63.25);
        when(user.getX()).thenReturn(4.0);
        when(user.getZ()).thenReturn(7.25);
        when(user.getBbHeight()).thenReturn(1.8F);
        when(user.getBoundingBox()).thenReturn(new AABB(4.0, -50.0, 7.25, 5.0, -48.2, 8.25));
        when(user.onGround()).thenReturn(false);

        double expectedCenterY = -63.25 + (double) 1.8F * 0.5;
        try (MockedStatic<ClientEffects> clientEffects = mockStatic(ClientEffects.class)) {
            TTItems.ELEMENTAL_SWORD.get().onUseTick(level, user, ItemStack.EMPTY, 71999);

            clientEffects.verify(
                    () -> ClientEffects.smokeSpiral(
                            eq(level),
                            eq(4.0),
                            doubleThat(value -> Math.abs(value - expectedCenterY) < 1.0E-12),
                            eq(7.25),
                            eq(1.5F),
                            anyInt(),
                            eq(-66),
                            eq(14540253)),
                    times(5));
        }
        verify(random, times(5)).nextInt(360);
        verify(random, never()).nextDouble();
    }

    @Test
    void groundedSmokePuffUsesSourceDoubleAngleAndCosineSineVector() {
        Level level = level(true);
        RandomSource random = level.getRandom();
        when(random.nextDouble()).thenReturn(0.25);
        when(random.nextFloat()).thenReturn(0.25F);

        LivingEntity user = user(Vec3.ZERO);
        when(user.getY()).thenReturn(-0.25);
        when(user.getX()).thenReturn(2.0);
        when(user.getZ()).thenReturn(3.0);
        when(user.getBbHeight()).thenReturn(1.8F);
        when(user.getBoundingBox()).thenReturn(new AABB(2.0, 50.0, 3.0, 3.0, 51.8, 4.0));
        when(user.onGround()).thenReturn(true);

        try (MockedStatic<ClientEffects> clientEffects = mockStatic(ClientEffects.class)) {
            TTItems.ELEMENTAL_SWORD.get().onUseTick(level, user, ItemStack.EMPTY, 71999);

            double heading = 0.25 * Mth.TWO_PI;
            verify(level)
                    .addParticle(
                            eq(ParticleTypes.SMOKE),
                            eq(2.0),
                            doubleThat(value -> Math.abs(value - (-0.25 + 0.1)) < 1.0E-12),
                            eq(3.0),
                            doubleThat(value -> Math.abs(value - Math.cos(heading) * 0.2) < 1.0E-12),
                            eq(0.0),
                            doubleThat(value -> Math.abs(value - Math.sin(heading) * 0.2) < 1.0E-12));
        }

        verify(random, times(5)).nextInt(360);
        verify(random).nextDouble();
        verify(random, never()).nextFloat();
    }

    private static LivingEntity user(Vec3 movement) {
        LivingEntity user = mock(LivingEntity.class);
        when(user.getDeltaMovement()).thenReturn(movement);
        when(user.getBoundingBox()).thenReturn(new AABB(0.0, 0.0, 0.0, 1.0, 1.0, 1.0));
        return user;
    }

    private static Level level(boolean clientSide) {
        Level level = mock(Level.class);
        RandomSource random = mock(RandomSource.class);
        when(level.isClientSide()).thenReturn(clientSide);
        when(level.getRandom()).thenReturn(random);
        when(level.getEntitiesOfClass(eq(LivingEntity.class), any(AABB.class), any()))
                .thenReturn(List.of());
        return level;
    }
}
