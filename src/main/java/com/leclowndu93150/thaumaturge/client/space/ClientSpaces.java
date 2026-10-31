package com.leclowndu93150.thaumaturge.client.space;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.jspecify.annotations.Nullable;

public final class ClientSpaces {
    public static final Quaternionfc IDENTITY = new Quaternionf();

    private static volatile ClientSpaceProvider provider = WorldOnlyRenderSpaces.INSTANCE;

    private ClientSpaces() {}

    public static void install(ClientSpaceProvider spaces) {
        if (provider != WorldOnlyRenderSpaces.INSTANCE) {
            throw new IllegalStateException("Client spaces already installed");
        }
        provider = spaces;
    }

    public static Vec3 renderPosition(@Nullable Level level, Vec3 position) {
        return level == null ? position : provider.renderPosition(level, position);
    }

    public static Quaternionfc renderOrientation(@Nullable Level level, Vec3 position) {
        return level == null ? IDENTITY : provider.renderOrientation(level, position);
    }

    public static void faceCamera(@Nullable Level level, Vec3 position, PoseStack poseStack, Quaternionfc rotation) {
        poseStack.mulPose(cameraFacing(level, position, rotation));
    }

    public static Quaternionf cameraFacing(@Nullable Level level, Vec3 position, Quaternionfc rotation) {
        return new Quaternionf(renderOrientation(level, position)).conjugate().mul(rotation);
    }

    public static void applyFrame(PoseStack poseStack, @Nullable Level level, Vec3 anchor, Vec3 camera) {
        Vec3 rendered = renderPosition(level, anchor);
        poseStack.translate(rendered.x - camera.x, rendered.y - camera.y, rendered.z - camera.z);
        poseStack.mulPose(new Quaternionf(renderOrientation(level, anchor)));
    }
}
