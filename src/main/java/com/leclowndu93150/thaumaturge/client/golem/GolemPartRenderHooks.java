package com.leclowndu93150.thaumaturge.client.golem;

import com.leclowndu93150.thaumaturge.api.golems.parts.GolemPartModel;
import com.leclowndu93150.thaumaturge.client.render.ItemRenderHelper;
import com.leclowndu93150.thaumaturge.registry.TTGolemParts;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.IdentityHashMap;
import java.util.Map;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;

public final class GolemPartRenderHooks {
    private static final String OUTER_SUFFIX = "_outer";
    private static final Map<GolemPartModel, GolemPartRenderHook> HOOKS = new IdentityHashMap<>();

    private GolemPartRenderHooks() {}

    public static GolemPartRenderHook hookFor(GolemPartModel model) {
        if (HOOKS.isEmpty()) {
            registerDefaults();
        }
        return HOOKS.getOrDefault(model, GolemPartRenderHook.NONE);
    }

    private static void registerDefaults() {
        HOOKS.put(TTGolemParts.LEGS_ROLLER.get().model(), new WheelHook());
        HOOKS.put(TTGolemParts.ARMS_CLAWS.get().model(), new ClawsHook());
        HOOKS.put(TTGolemParts.ARMS_BREAKERS.get().model(), new BreakersHook());
        HOOKS.put(TTGolemParts.ARMS_DARTS.get().model(), new DartsHook());
        HOOKS.put(TTGolemParts.ADDON_HAULER.get().model(), new HaulerHook());
        for (GolemPartModel model : TTGolemParts.ADDON_ARMORED.get().models()) {
            if (model.attachPoint() == GolemPartModel.AttachPoint.ARMS) HOOKS.put(model, new PauldronHook());
        }
    }

    static final class PauldronHook implements GolemPartRenderHook {
        @Override
        public void preRenderObjectPart(
                String partName,
                GolemRenderState state,
                PoseStack poseStack,
                GolemPartModel.LimbSide side,
                float partialTick) {
            if (side == GolemPartModel.LimbSide.LEFT && partName.endsWith(OUTER_SUFFIX))
                poseStack.mulPose(Axis.YP.rotationDegrees(180));
        }
    }

    static final class WheelHook implements GolemPartRenderHook {
        @Override
        public void preRenderObjectPart(
                String partName,
                GolemRenderState state,
                PoseStack poseStack,
                GolemPartModel.LimbSide side,
                float partialTick) {
            if (partName.equals("wheel")) {
                poseStack.translate(0.0, -2.5 / 16.0, 0.0);
                poseStack.mulPose(Axis.XN.rotationDegrees(state.wheelRotation));
            }
        }
    }

    static final class ClawsHook implements GolemPartRenderHook {
        @Override
        public void preRenderObjectPart(
                String partName,
                GolemRenderState state,
                PoseStack poseStack,
                GolemPartModel.LimbSide side,
                float partialTick) {
            if (partName.startsWith("claw")) {
                float open = state.attackTime * 4.1F;
                open = open * open;
                poseStack.translate(0.0, -1.5 / 16.0, 0.0);
                poseStack.mulPose((partName.endsWith("1") ? Axis.XP : Axis.XN).rotationDegrees(open));
            }
        }
    }

    static final class BreakersHook implements GolemPartRenderHook {
        @Override
        public void preRenderObjectPart(
                String partName,
                GolemRenderState state,
                PoseStack poseStack,
                GolemPartModel.LimbSide side,
                float partialTick) {
            if (partName.equals("grinder")) {
                poseStack.translate(0.0, -1.0 / 16.0, 0.0);
                float angle =
                        (state.ageInTicks) / 2.0F + state.grinderRot + (side == GolemPartModel.LimbSide.LEFT ? 22 : 0);
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
        public void postRenderObjectPart(
                String partName,
                GolemRenderState state,
                PoseStack poseStack,
                MultiBufferSource buffers,
                GolemPartModel.LimbSide side) {
            if (!state.haulingItem || !partName.equals("cargo")) {
                return;
            }
            poseStack.pushPose();
            poseStack.translate(0, 3.0F / 16, 6.5F / 16);
            poseStack.scale(0.45F, 0.45F, 0.45F);
            ItemRenderHelper.render(
                    state.haulerItem,
                    ItemDisplayContext.FIXED,
                    poseStack,
                    buffers,
                    state.lightCoords,
                    OverlayTexture.NO_OVERLAY,
                    0);
            poseStack.popPose();
        }
    }
}
