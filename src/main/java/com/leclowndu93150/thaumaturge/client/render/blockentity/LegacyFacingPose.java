package com.leclowndu93150.thaumaturge.client.render.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.core.Direction;

public final class LegacyFacingPose {
    private LegacyFacingPose() {}

    public static void apply(PoseStack poseStack, Direction facing) {
        poseStack.translate(0.5F, 0.5F, 0.5F);
        BlockFacingPose.northBased(poseStack, facing);
        poseStack.translate(0.0F, 0.0F, -0.5F);
    }
}
