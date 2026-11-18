package com.leclowndu93150.thaumaturge.client.item;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

class WandHandRendererTest {
    @Test
    void settledBeamPointsAlongViewForwardForEitherHand() {
        for (float side : new float[] {-1, 1}) {
            Matrix4f view = new Matrix4f()
                    .translate(side * 0.6F, -0.5F, -0.7F)
                    .rotateY(side * 0.7F)
                    .rotateX(3.0F)
                    .scale(0.4F, 0.44F, 0.48F);
            Vector3f modelTipAxis = new Vector3f(0, -1, 0);
            assertEquals(modelTipAxis, WandHandRenderer.beamAimRotation(view, 0).transform(new Vector3f(modelTipAxis)));

            Vector3f settledAxis = view.transformDirection(
                            WandHandRenderer.beamAimRotation(view, 1).transform(new Vector3f(modelTipAxis)))
                    .normalize();
            assertEquals(0, settledAxis.x, 1e-5F);
            assertEquals(0, settledAxis.y, 1e-5F);
            assertEquals(-1, settledAxis.z, 1e-5F);
        }
    }

    @Test
    void usePoseTransitionsSmoothlyIntoItsSettledAngle() {
        assertEquals(0.0F, WandHandRenderer.useSettle(0.0F));
        assertEquals(0.5F, WandHandRenderer.useSettle(2.5F));
        assertEquals(1.0F, WandHandRenderer.useSettle(5.0F));
        assertEquals(1.0F, WandHandRenderer.useSettle(20.0F));
        assertEquals(0.0F, WandHandRenderer.useSettle(-1.0F));
    }
}
