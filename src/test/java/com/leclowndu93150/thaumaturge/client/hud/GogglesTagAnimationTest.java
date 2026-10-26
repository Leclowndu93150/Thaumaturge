package com.leclowndu93150.thaumaturge.client.hud;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Method;
import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

final class GogglesTagAnimationTest {
    @BeforeEach
    void resetAnimation() throws ReflectiveOperationException {
        resetOverlayAnimation();
    }

    private static void resetOverlayAnimation() throws ReflectiveOperationException {
        Method reset = GogglesWorldOverlay.class.getDeclaredMethod("resetAnimation");
        reset.setAccessible(true);
        reset.invoke(null);
    }

    @Test
    void animationUsesTenTickCubicEaseAndStableClockSamples() {
        BlockPos target = new BlockPos(2, 3, 4);

        assertEquals(0.0F, GogglesWorldOverlay.advanceAnimation(target, 100.0D), 0.000001F);
        assertEquals(0.0F, GogglesWorldOverlay.advanceAnimation(target, 100.0D), 0.000001F);
        assertEquals(0.2625F, GogglesWorldOverlay.advanceAnimation(target, 105.0D), 0.000001F);
        assertEquals(
                0.3F * (1.0F - 0.45F * 0.45F * 0.45F), GogglesWorldOverlay.advanceAnimation(target, 105.5D), 0.000001F);
        assertEquals(0.3F, GogglesWorldOverlay.advanceAnimation(target, 110.0D), 0.000001F);
        assertEquals(0.3F, GogglesWorldOverlay.advanceAnimation(target, 112.0D), 0.000001F);
    }

    @Test
    void targetChangeAndResetStartANewAnimation() throws ReflectiveOperationException {
        BlockPos firstTarget = new BlockPos(2, 3, 4);
        BlockPos secondTarget = new BlockPos(5, 6, 7);

        GogglesWorldOverlay.advanceAnimation(firstTarget, 100.0D);
        assertEquals(0.0F, GogglesWorldOverlay.advanceAnimation(secondTarget, 105.0D), 0.000001F);
        assertEquals(0.2625F, GogglesWorldOverlay.advanceAnimation(secondTarget, 110.0D), 0.000001F);

        resetOverlayAnimation();
        assertEquals(0.0F, GogglesWorldOverlay.advanceAnimation(secondTarget, 200.0D), 0.000001F);
    }
}
