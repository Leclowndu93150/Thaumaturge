package com.leclowndu93150.thaumaturge.client.golem;

import com.leclowndu93150.thaumaturge.api.golems.parts.GolemPartModel;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;

public interface GolemPartRenderHook {
    GolemPartRenderHook NONE = Noop.INSTANCE;

    default void preRenderObjectPart(String partName, GolemRenderState state, PoseStack poseStack, GolemPartModel.LimbSide side, float partialTick) {}

    default void postRenderObjectPart(String partName, GolemRenderState state, PoseStack poseStack, SubmitNodeCollector collector, GolemPartModel.LimbSide side) {}

    default float armRotationX(GolemRenderState state, GolemPartModel.LimbSide side, float inputRot) {
        return inputRot;
    }

    default float armRotationY(GolemRenderState state, GolemPartModel.LimbSide side, float inputRot) {
        return inputRot;
    }

    default float armRotationZ(GolemRenderState state, GolemPartModel.LimbSide side, float inputRot) {
        return inputRot;
    }

    enum Noop implements GolemPartRenderHook {
        INSTANCE
    }
}
