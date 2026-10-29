package com.leclowndu93150.thaumaturge.content.equipment;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.*;

import com.leclowndu93150.thaumaturge.api.items.RechargeAccess;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class TravellerBootsMovementTest {
    @Test
    void passengerDoesNotReceiveGroundBoost() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        Player player = mock(Player.class);
        player.zza = 1.0F;
        when(player.getAbilities()).thenReturn(new Abilities());
        when(player.isPassenger()).thenReturn(true);
        when(player.onGround()).thenReturn(true);

        try (MockedStatic<RechargeAccess> rechargeAccess = mockStatic(RechargeAccess.class)) {
            rechargeAccess.when(() -> RechargeAccess.storedVis(stack)).thenReturn(1);

            TravellerBootsItem.clientMovementTick(player, stack);
        }

        verify(player, never()).moveRelative(anyFloat(), any(Vec3.class));
    }

    @Test
    void unmountedGroundedPlayerStillReceivesGroundBoost() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        Player player = mock(Player.class);
        player.zza = 1.0F;
        when(player.getAbilities()).thenReturn(new Abilities());
        when(player.isPassenger()).thenReturn(false);
        when(player.onGround()).thenReturn(true);

        try (MockedStatic<RechargeAccess> rechargeAccess = mockStatic(RechargeAccess.class)) {
            rechargeAccess.when(() -> RechargeAccess.storedVis(stack)).thenReturn(1);

            TravellerBootsItem.clientMovementTick(player, stack);
        }

        verify(player).moveRelative(0.05F, new Vec3(0.0, 0.0, 1.0));
    }

    @Test
    void chargedBootsAddAirControlInAnyHorizontalDirection() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        Player player = mock(Player.class);
        player.zza = -1.0F;
        player.xxa = 0.5F;
        when(player.getAbilities()).thenReturn(new Abilities());
        when(player.isPassenger()).thenReturn(false);
        when(player.onGround()).thenReturn(false);
        when(player.isInWater()).thenReturn(false);

        try (MockedStatic<RechargeAccess> rechargeAccess = mockStatic(RechargeAccess.class)) {
            rechargeAccess.when(() -> RechargeAccess.storedVis(stack)).thenReturn(1);

            TravellerBootsItem.clientMovementTick(player, stack);
        }

        verify(player).moveRelative(0.05F - 0.02F, new Vec3(0.5, 0.0, -1.0));
    }

    @Test
    void sneakingSuppressesBootsJumpAndStepEffects() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        Player player = mock(Player.class);
        when(player.getAbilities()).thenReturn(new Abilities());
        when(player.isShiftKeyDown()).thenReturn(true);
        player.zza = 0.0F;

        try (MockedStatic<RechargeAccess> rechargeAccess = mockStatic(RechargeAccess.class)) {
            rechargeAccess.when(() -> RechargeAccess.storedVis(stack)).thenReturn(1);

            assertFalse(TravellerBootsItem.isActive(player, stack));
            assertFalse(TravellerBootsItem.isStepActive(player, stack));
        }
    }
}
