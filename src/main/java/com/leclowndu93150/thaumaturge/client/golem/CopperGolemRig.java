package com.leclowndu93150.thaumaturge.client.golem;

import com.leclowndu93150.thaumaturge.api.client.golems.GolemAccessoryAnchor;
import com.leclowndu93150.thaumaturge.api.golems.accessory.GolemAccessory;
import com.leclowndu93150.thaumaturge.api.golems.parts.GolemPartModel;
import com.leclowndu93150.thaumaturge.registry.TTGolemParts;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.client.model.animal.golem.CopperGolemModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.CopperGolemRenderState;
import net.minecraft.util.Mth;

public final class CopperGolemRig extends CopperGolemModel {
    public static final float SCALE = 0.6F;
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart antenna;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;

    public CopperGolemRig(ModelPart vanillaRoot) {
        super(splitAntenna(vanillaRoot));
        body = root().getChild("body");
        head = body.getChild("head");
        antenna = head.getChild("antenna");
        rightArm = body.getChild("right_arm");
        leftArm = body.getChild("left_arm");
        rightLeg = root().getChild("right_leg");
        leftLeg = root().getChild("left_leg");
    }

    private static ModelPart splitAntenna(ModelPart root) {
        ModelPart body = root.getChild("body");
        ModelPart head = body.getChild("head");
        List<ModelPart.Cube> shell = new ArrayList<>();
        List<ModelPart.Cube> antenna = new ArrayList<>();
        head.visit(new PoseStack(), (pose, path, index, cube) -> {
            if (path.isEmpty()) {
                (cube.maxY <= -5.0F ? antenna : shell).add(cube);
            }
        });
        ModelPart newHead = copy(head, shell, Map.of("antenna", new ModelPart(antenna, Map.of())));
        ModelPart newBody = copy(body, cubes(body), Map.of("head", newHead, "right_arm", body.getChild("right_arm"), "left_arm", body.getChild("left_arm")));
        return copy(root, cubes(root), Map.of("body", newBody, "right_leg", root.getChild("right_leg"), "left_leg", root.getChild("left_leg")));
    }

    private static List<ModelPart.Cube> cubes(ModelPart part) {
        List<ModelPart.Cube> cubes = new ArrayList<>();
        part.visit(new PoseStack(), (pose, path, index, cube) -> {
            if (path.isEmpty()) {
                cubes.add(cube);
            }
        });
        return cubes;
    }

    private static ModelPart copy(ModelPart original, List<ModelPart.Cube> cubes, Map<String, ModelPart> children) {
        ModelPart copy = new ModelPart(cubes, children);
        copy.setInitialPose(original.getInitialPose());
        copy.resetPose();
        return copy;
    }

    @Override
    public void setupAnim(CopperGolemRenderState renderState) {
        super.setupAnim(renderState);
        if (!(renderState instanceof GolemRenderState state) || state.props == null) {
            return;
        }
        boolean walking = state.props.legs() != TTGolemParts.LEGS_ROLLER.get() && state.props.legs() != TTGolemParts.LEGS_FLYER.get();
        rightLeg.visible = walking;
        leftLeg.visible = walking;
        antenna.visible = state.props.material().antenna() && state.accessories.stream().noneMatch(accessory -> accessory.group() == GolemAccessory.Group.HAT);
        if (!walking) {
            body.xRot *= 0.2F;
            body.zRot *= 0.2F;
        }
        if (state.attackTime > 0.0F) {
            rightArm.xRot -= Mth.sin(state.attackTime * Mth.PI) * 1.8F;
        }
        if (state.combat && state.props.arms() == TTGolemParts.ARMS_DARTS.get()) {
            rightArm.xRot = leftArm.xRot = -Mth.HALF_PI + state.xRot * Mth.DEG_TO_RAD;
            rightArm.yRot = leftArm.yRot = 0.0F;
            rightArm.zRot = leftArm.zRot = 0.0F;
        }
    }

    public void translateToAnchor(PoseStack pose, GolemAccessoryAnchor anchor) {
        root().translateAndRotate(pose);
        body.translateAndRotate(pose);
        if (anchor == GolemAccessoryAnchor.HEAD) {
            head.translateAndRotate(pose);
        }
        pose.scale(-1.0F, -1.0F, 1.0F);
    }

    public void translateToLimb(PoseStack pose, GolemPartModel.AttachPoint point, GolemPartModel.LimbSide side) {
        root().translateAndRotate(pose);
        boolean right = side == GolemPartModel.LimbSide.RIGHT;
        if (point == GolemPartModel.AttachPoint.ARMS) {
            body.translateAndRotate(pose);
            (right ? rightArm : leftArm).translateAndRotate(pose);
        } else {
            (right ? rightLeg : leftLeg).translateAndRotate(pose);
        }
        pose.scale(-1.0F, -1.0F, 1.0F);
        pose.translate((right ? 1 : -1) * (point == GolemPartModel.AttachPoint.ARMS ? 1.5F : 2.0F) / 16.0F, (point == GolemPartModel.AttachPoint.ARMS ? -7.0F : -4.0F) / 16.0F, 0.0F);
    }
}
