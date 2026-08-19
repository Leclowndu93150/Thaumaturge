package com.leclowndu93150.thaumaturge.client.golem;

import com.leclowndu93150.thaumaturge.api.golems.parts.GolemPartModel;
import com.leclowndu93150.thaumaturge.registry.TCGolemParts;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.IdentityHashMap;
import java.util.Map;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.texture.OverlayTexture;

public final class GolemPartRenderHooks {
    private static final Map<GolemPartModel, GolemPartRenderHook> HOOKS = new IdentityHashMap<>();

    private GolemPartRenderHooks() {}

    public static GolemPartRenderHook hookFor(GolemPartModel model) {
        if (HOOKS.isEmpty()) {
            registerDefaults();
        }
        return HOOKS.getOrDefault(model, GolemPartRenderHook.NONE);
    }

    private static void registerDefaults() {
        HOOKS.put(TCGolemParts.LEGS_ROLLER.get().model(), new WheelHook());
        HOOKS.put(TCGolemParts.ARMS_CLAWS.get().model(), new ClawsHook());
        HOOKS.put(TCGolemParts.ARMS_BREAKERS.get().model(), new BreakersHook());
        HOOKS.put(TCGolemParts.ARMS_DARTS.get().model(), new DartsHook());
        HOOKS.put(TCGolemParts.ADDON_HAULER.get().model(), new HaulerHook());
    }

    static final class WheelHook implements GolemPartRenderHook {
        @Override
        public void preRenderObjectPart(String partName, GolemRenderState state, PoseStack poseStack, GolemPartModel.LimbSide side, float partialTick) {
            if (partName.equals("wheel")) {
                poseStack.translate(0.0, -0.375, 0.0);
                poseStack.mulPose(Axis.XN.rotationDegrees(state.wheelRotation));
            }
        }
    }

    static final class ClawsHook implements GolemPartRenderHook {
        @Override
        public void preRenderObjectPart(String partName, GolemRenderState state, PoseStack poseStack, GolemPartModel.LimbSide side, float partialTick) {
            if (partName.startsWith("claw")) {
                float open = state.attackTime * 4.1F;
                open = open * open;
                poseStack.translate(0.0, -0.2, 0.0);
                poseStack.mulPose((partName.endsWith("1") ? Axis.XP : Axis.XN).rotationDegrees(open));
            }
        }
    }

    static final class BreakersHook implements GolemPartRenderHook {
        @Override
        public void preRenderObjectPart(String partName, GolemRenderState state, PoseStack poseStack, GolemPartModel.LimbSide side, float partialTick) {
            if (partName.equals("grinder")) {
                poseStack.translate(0.0, -0.34, 0.0);
                float angle = (state.ageInTicks) / 2.0F + state.grinderRot + (side == GolemPartModel.LimbSide.LEFT ? 22 : 0);
                poseStack.mulPose((side == GolemPartModel.LimbSide.LEFT ? Axis.XN : Axis.XP).rotationDegrees(angle));
            }
        }
    }

    static final class DartsHook implements GolemPartRenderHook {
        @Override
        public float armRotationX(GolemRenderState state, GolemPartModel.LimbSide side, float inputRot) {
            return state.combat ? 90.0F - state.pitch + inputRot / 10.0F : inputRot;
        }

        @Override
        public float armRotationY(GolemRenderState state, GolemPartModel.LimbSide side, float inputRot) {
            return state.combat ? inputRot / 10.0F : inputRot;
        }

        @Override
        public float armRotationZ(GolemRenderState state, GolemPartModel.LimbSide side, float inputRot) {
            return state.combat ? inputRot / 10.0F : inputRot;
        }
    }

    static final class HaulerHook implements GolemPartRenderHook {
        @Override
        public void postRenderObjectPart(String partName, GolemRenderState state, PoseStack poseStack, SubmitNodeCollector collector, GolemPartModel.LimbSide side) {
            if (!state.haulingItem) {
                return;
            }
            poseStack.pushPose();
            poseStack.scale(0.375F, 0.375F, 0.375F);
            poseStack.translate(0.0F, 0.33F, 0.825F);
            if (!state.haulerItemIsBlock) {
                poseStack.translate(0.0F, 0.0F, -0.25F);
            }
            state.haulerItem.submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
            poseStack.popPose();
        }
    }
}
