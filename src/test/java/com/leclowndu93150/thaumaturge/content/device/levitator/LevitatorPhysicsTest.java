package com.leclowndu93150.thaumaturge.content.device.levitator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class LevitatorPhysicsTest {
    @Test
    void horizontalFlightCountersTheRidersGravity() {
        Entity rider = mock(Entity.class);
        when(rider.getDeltaMovement()).thenReturn(Vec3.ZERO);
        when(rider.getGravity()).thenReturn(0.08);
        when(rider.onGround()).thenReturn(false);

        BlockEntityLevitator.push(rider, Direction.EAST);

        verify(rider).setDeltaMovement(0.1, 0.08, 0.0);
    }

    @Test
    void crouchingPlayerOnUpwardLevitatorDampensOnlyDownwardMotion() {
        Player rider = mock(Player.class);
        when(rider.getDeltaMovement()).thenReturn(new Vec3(0.2, -0.2, -0.1));
        when(rider.isShiftKeyDown()).thenReturn(true);

        BlockEntityLevitator.push(rider, Direction.UP);

        ArgumentCaptor<Double> x = ArgumentCaptor.forClass(Double.class);
        ArgumentCaptor<Double> y = ArgumentCaptor.forClass(Double.class);
        ArgumentCaptor<Double> z = ArgumentCaptor.forClass(Double.class);
        verify(rider).setDeltaMovement(x.capture(), y.capture(), z.capture());
        assertEquals(0.2, x.getValue(), 1.0E-9);
        assertEquals(-0.18, y.getValue(), 1.0E-9);
        assertEquals(-0.1, z.getValue(), 1.0E-9);
    }
}
