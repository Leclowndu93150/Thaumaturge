package com.leclowndu93150.thaumaturge.client.render.blockentity;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.List;
import net.minecraft.core.Direction;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

class ResearchTableInkwellPortTest {
    @Test
    void existingFacingTransformMatchesTheNativePoseForEveryHorizontalDirection() {
        for (Direction facing : List.of(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST)) {
            PoseStack source = new PoseStack();
            BlockFacingPose.northBased(source, facing);
            source.mulPose(Axis.YP.rotationDegrees(90.0F));
            source.mulPose(Axis.XP.rotationDegrees(180.0F));

            PoseStack nativePose = new PoseStack();
            nativePose.mulPose(Axis.XP.rotationDegrees(180.0F));
            float yaw =
                    switch (facing) {
                        case NORTH -> 270.0F;
                        case SOUTH -> 90.0F;
                        case WEST -> 180.0F;
                        default -> 0.0F;
                    };
            nativePose.mulPose(Axis.YP.rotationDegrees(yaw));

            for (float[] point : List.of(
                    new float[] {1.0F, 0.0F, 0.0F}, new float[] {0.0F, 1.0F, 0.0F}, new float[] {0.0F, 0.0F, 1.0F})) {
                Vector3f sourceResult =
                        source.last().pose().transformPosition(point[0], point[1], point[2], new Vector3f());
                Vector3f nativeResult =
                        nativePose.last().pose().transformPosition(point[0], point[1], point[2], new Vector3f());
                assertEquals(sourceResult.x, nativeResult.x, 1.0E-6F, facing.toString());
                assertEquals(sourceResult.y, nativeResult.y, 1.0E-6F, facing.toString());
                assertEquals(sourceResult.z, nativeResult.z, 1.0E-6F, facing.toString());
            }
        }
    }
}
