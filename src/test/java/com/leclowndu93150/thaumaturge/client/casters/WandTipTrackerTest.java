package com.leclowndu93150.thaumaturge.client.casters;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

class WandTipTrackerTest {
    @Test
    void wandAndWorldProjectToTheSamePointAcrossCameraAnglesAndUsePoses() {
        Matrix4f worldProjection = new Matrix4f()
                .perspective((float) Math.toRadians(95), 1.6F, 0.05F, 128)
                .rotateZ(0.04F);
        Matrix4f handProjection = new Matrix4f().perspective((float) Math.toRadians(70), 1.6F, 0.05F, 128);
        Matrix4f inverseWorldProjection = worldProjection.invert(new Matrix4f());

        for (float yaw : new float[] {0, 90, 180, 270}) {
            for (float pitch : new float[] {-89, 0, 89}) {
                Quaternionf rotation = new Quaternionf()
                        .rotationYXZ((float) Math.toRadians(180 - yaw), (float) Math.toRadians(-pitch), 0);
                Matrix4f view = new Matrix4f().rotation(rotation.conjugate(new Quaternionf()));
                for (float tilt : new float[] {0, 17, 34}) {
                    Matrix4f handPose = view.invert(new Matrix4f())
                            .translate(0.4F, -0.5F, -0.7F)
                            .rotateX((float) Math.toRadians(tilt))
                            .scale(0.4F);
                    Vector3f screenTip = new Matrix4f(handProjection)
                            .mul(view)
                            .mul(handPose)
                            .transformProject(new Vector3f(0, -3F / 16, 0));

                    Vector3f worldTip = WandTipTracker.unproject(inverseWorldProjection, rotation, screenTip);
                    Vector3f reprojected =
                            new Matrix4f(worldProjection).mul(view).transformProject(worldTip);
                    assertEquals(screenTip.x, reprojected.x, 1e-5F);
                    assertEquals(screenTip.y, reprojected.y, 1e-5F);
                    assertEquals(screenTip.z, reprojected.z, 1e-5F);
                }
            }
        }
    }
}
