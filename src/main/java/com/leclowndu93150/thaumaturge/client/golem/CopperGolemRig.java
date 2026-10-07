package com.leclowndu93150.thaumaturge.client.golem;

import com.leclowndu93150.thaumaturge.api.client.golems.GolemAccessoryAnchor;
import com.leclowndu93150.thaumaturge.api.golems.accessory.GolemAccessory;
import com.leclowndu93150.thaumaturge.api.golems.parts.GolemPartModel;
import com.leclowndu93150.thaumaturge.content.golem.EntityThaumaturgeGolem;
import com.leclowndu93150.thaumaturge.content.golem.GolemProperties;
import com.leclowndu93150.thaumaturge.registry.TTGolemParts;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.AnimationState;

public final class CopperGolemRig extends HierarchicalModel<EntityThaumaturgeGolem> {
    public static final float SCALE = 0.6F;
    private final ModelPart root;
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart antenna;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;
    private final AnimationState idle = new AnimationState();

    public CopperGolemRig(ModelPart root) {
        this.root = root;
        root.setInitialPose(PartPose.offset(0, 24, 0));
        root.resetPose();
        body = root.getChild("body");
        head = body.getChild("head");
        antenna = head.getChild("antenna");
        rightArm = body.getChild("right_arm");
        leftArm = body.getChild("left_arm");
        rightLeg = root.getChild("right_leg");
        leftLeg = root.getChild("left_leg");
        idle.start(0);
    }

    @Override
    public ModelPart root() {
        return root;
    }

    @Override
    public void setupAnim(
            EntityThaumaturgeGolem entity,
            float limbSwing,
            float limbSwingAmount,
            float ageInTicks,
            float headYaw,
            float headPitch) {
        GolemRenderState state = new GolemRenderState();
        state.props = (GolemProperties) entity.getProperties();
        state.walkPos = limbSwing;
        state.walkSpeed = limbSwingAmount;
        state.ageInTicks = ageInTicks;
        state.headYawDelta = headYaw;
        state.pitch = headPitch;
        state.holdingItem = !entity.getMainHandItem().isEmpty();
        state.attackTime = entity.getAttackAnim(0);
        state.combat = entity.isInCombat();
        state.accessories = entity.getAccessories();
        setupAnim(state);
    }

    public void setupAnim(GolemRenderState state) {
        root.getAllParts().forEach(ModelPart::resetPose);
        head.xRot = state.pitch * Mth.DEG_TO_RAD;
        head.yRot = state.headYawDelta * Mth.DEG_TO_RAD;
        animateWalk(
                state.holdingItem
                        ? CopperGolemAnimations.COPPER_GOLEM_WALK_ITEM
                        : CopperGolemAnimations.COPPER_GOLEM_WALK,
                state.walkPos,
                state.walkSpeed,
                2.0F,
                2.5F);
        if (state.holdingItem) {
            rightArm.xRot = Math.min(rightArm.xRot, -0.87266463F);
            leftArm.xRot = Math.min(leftArm.xRot, -0.87266463F);
            rightArm.yRot = Math.min(rightArm.yRot, -0.1134464F);
            leftArm.yRot = Math.max(leftArm.yRot, 0.1134464F);
            rightArm.zRot = Math.min(rightArm.zRot, -0.064577185F);
            leftArm.zRot = Math.max(leftArm.zRot, 0.064577185F);
        }
        animate(idle, CopperGolemAnimations.COPPER_GOLEM_IDLE, state.ageInTicks);
        boolean walking = state.props.getLegs() != TTGolemParts.LEGS_ROLLER.get()
                && state.props.getLegs() != TTGolemParts.LEGS_FLYER.get();
        rightLeg.visible = leftLeg.visible = walking;
        antenna.visible = state.props.getMaterial().antenna()
                && state.accessories.stream().noneMatch(accessory -> accessory.group() == GolemAccessory.Group.HAT);
        if (!walking) {
            body.xRot *= 0.2F;
            body.zRot *= 0.2F;
        }
        if (state.attackTime > 0.0F) {
            rightArm.xRot -= Mth.sin(state.attackTime * Mth.PI) * 1.8F;
        }
        if (state.combat && state.props.getArms() == TTGolemParts.ARMS_DARTS.get()) {
            rightArm.xRot = leftArm.xRot = -Mth.HALF_PI + state.pitch * Mth.DEG_TO_RAD;
            rightArm.yRot = leftArm.yRot = rightArm.zRot = leftArm.zRot = 0;
        }
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition root = meshDefinition.getRoot();
        PartDefinition body = root.addOrReplaceChild(
                "body",
                CubeListBuilder.create()
                        .texOffs(0, 15)
                        .addBox(-4.0F, -6.0F, -3.0F, 8.0F, 6.0F, 6.0F, CubeDeformation.NONE),
                PartPose.offset(0.0F, -5.0F, 0.0F));
        PartDefinition head = body.addOrReplaceChild(
                "head",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-4, -5, -5, 8, 5, 10, new CubeDeformation(0.015F))
                        .texOffs(56, 0)
                        .addBox(-1, -2, -6, 2, 3, 2),
                PartPose.offset(0, -6, 0));
        head.addOrReplaceChild(
                "antenna",
                CubeListBuilder.create()
                        .texOffs(37, 8)
                        .addBox(-1, -9, -1, 2, 4, 2, new CubeDeformation(-0.015F))
                        .texOffs(37, 0)
                        .addBox(-2, -13, -2, 4, 4, 4, new CubeDeformation(-0.015F)),
                PartPose.ZERO);
        body.addOrReplaceChild(
                "right_arm",
                CubeListBuilder.create()
                        .texOffs(36, 16)
                        .addBox(-3.0F, -1.0F, -2.0F, 3.0F, 10.0F, 4.0F, CubeDeformation.NONE),
                PartPose.offset(-4.0F, -6.0F, 0.0F));
        body.addOrReplaceChild(
                "left_arm",
                CubeListBuilder.create()
                        .texOffs(50, 16)
                        .addBox(0.0F, -1.0F, -2.0F, 3.0F, 10.0F, 4.0F, CubeDeformation.NONE),
                PartPose.offset(4.0F, -6.0F, 0.0F));
        root.addOrReplaceChild(
                "right_leg",
                CubeListBuilder.create()
                        .texOffs(0, 27)
                        .addBox(-4.0F, 0.0F, -2.0F, 4.0F, 5.0F, 4.0F, CubeDeformation.NONE),
                PartPose.offset(0.0F, -5.0F, 0.0F));
        root.addOrReplaceChild(
                "left_leg",
                CubeListBuilder.create()
                        .texOffs(16, 27)
                        .addBox(0.0F, 0.0F, -2.0F, 4.0F, 5.0F, 4.0F, CubeDeformation.NONE),
                PartPose.offset(0.0F, -5.0F, 0.0F));
        return LayerDefinition.create(meshDefinition, 64, 64);
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
        pose.translate(
                (right ? 1 : -1) * (point == GolemPartModel.AttachPoint.ARMS ? 1.5F : 2.0F) / 16.0F,
                (point == GolemPartModel.AttachPoint.ARMS ? -7.0F : -4.0F) / 16.0F,
                0.0F);
    }
}
