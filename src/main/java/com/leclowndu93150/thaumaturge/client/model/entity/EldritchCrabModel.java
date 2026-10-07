package com.leclowndu93150.thaumaturge.client.model.entity;

import com.leclowndu93150.thaumaturge.client.entity.EldritchCrabRenderState;
import java.util.EnumSet;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;

public final class EldritchCrabModel extends EntityModel<EldritchCrabRenderState> {
    private static final int TEXTURE_WIDTH = 128;
    private static final int TEXTURE_HEIGHT = 128;

    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart jaw;
    private final ModelPart abdomen;
    private final ModelPart shell;
    private final ModelPart bare;
    private final ModelPart leftArm;
    private final ModelPart leftClaw;
    private final ModelPart leftPincer;
    private final ModelPart leftFrontUpper;
    private final ModelPart leftFrontLower;
    private final ModelPart leftRearUpper;
    private final ModelPart leftRearLower;
    private final ModelPart rightArm;
    private final ModelPart rightClaw;
    private final ModelPart rightPincer;
    private final ModelPart rightFrontUpper;
    private final ModelPart rightFrontLower;
    private final ModelPart rightRearUpper;
    private final ModelPart rightRearLower;

    public EldritchCrabModel(ModelPart root) {
        super(root);
        body = root.getChild("root").getChild("body");
        head = root.getChild("root").getChild("body").getChild("head");
        jaw = root.getChild("root").getChild("body").getChild("head").getChild("jaw");
        abdomen = root.getChild("root").getChild("body").getChild("abdomen");
        shell = root.getChild("root").getChild("body").getChild("abdomen").getChild("shell");
        bare = root.getChild("root").getChild("body").getChild("abdomen").getChild("bare");
        leftArm = root.getChild("root").getChild("body").getChild("left_arm");
        leftClaw = root.getChild("root").getChild("body").getChild("left_arm").getChild("left_claw");
        leftPincer = root.getChild("root").getChild("body").getChild("left_arm").getChild("left_claw").getChild("left_pincer");
        leftFrontUpper = root.getChild("root").getChild("body").getChild("left_front_upper");
        leftFrontLower = root.getChild("root").getChild("body").getChild("left_front_upper").getChild("left_front_lower");
        leftRearUpper = root.getChild("root").getChild("body").getChild("left_rear_upper");
        leftRearLower = root.getChild("root").getChild("body").getChild("left_rear_upper").getChild("left_rear_lower");
        rightArm = root.getChild("root").getChild("body").getChild("right_arm");
        rightClaw = root.getChild("root").getChild("body").getChild("right_arm").getChild("right_claw");
        rightPincer = root.getChild("root").getChild("body").getChild("right_arm").getChild("right_claw").getChild("right_pincer");
        rightFrontUpper = root.getChild("root").getChild("body").getChild("right_front_upper");
        rightFrontLower = root.getChild("root").getChild("body").getChild("right_front_upper").getChild("right_front_lower");
        rightRearUpper = root.getChild("root").getChild("body").getChild("right_rear_upper");
        rightRearLower = root.getChild("root").getChild("body").getChild("right_rear_upper").getChild("right_rear_lower");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition meshRoot = mesh.getRoot();
        PartDefinition root = meshRoot.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));
        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(1, 1).addBox(-3.5F, -2.875F, -6.0F, 7.0F, 4.5F, 7.0F), PartPose.offset(0.0F, -5.0F, 0.0F));
        PartDefinition head = body.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(30, 1).addBox(-2.5F, -2.5F, -2.0F, 5.0F, 4.0F, 2.5F).texOffs(46, 1).addBox(-2.0F, -2.0F, -0.5F, 4.0F, 3.0F, 2.0F), PartPose.offset(0.0F, 0.0F, -7.0F));
        head.addOrReplaceChild("jaw", CubeListBuilder.create().texOffs(59, 1).addBox(-1.5F, 0.25F, -2.0F, 3.0F, 1.0F, 2.0F), PartPose.offset(0.0F, 1.0F, -0.5F));
        PartDefinition abdomen = body.addOrReplaceChild("abdomen", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 1.0F));
        abdomen.addOrReplaceChild("shell",
                CubeListBuilder.create().texOffs(70, 1).addBox(-4.5F, -5.0F, 0.0F, 9.0F, 7.0F, 8.0F).texOffs(1, 17)
                        .addBox(-3.5F, -7.0F, 0.5F, 7.0F, 2.0F, 7.0F, EnumSet.of(Direction.DOWN, Direction.WEST, Direction.NORTH, Direction.EAST, Direction.SOUTH)).texOffs(30, 17)
                        .addBox(-4.75F, 1.5F, -1.25F, 9.5F, 1.0F, 1.5F),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        abdomen.addOrReplaceChild("bare", CubeListBuilder.create().texOffs(53, 17).addBox(-3.5F, -4.5F, 0.0F, 7.0F, 6.0F, 7.0F).texOffs(82, 17).addBox(-2.5F, -5.5F, 1.0F, 5.0F, 1.0F, 5.0F,
                EnumSet.of(Direction.DOWN, Direction.WEST, Direction.NORTH, Direction.EAST, Direction.SOUTH)), PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition leftFrontUpper = body.addOrReplaceChild("left_front_upper", CubeListBuilder.create().texOffs(103, 17).addBox(-1.25F, -1.25F, -1.25F, 2.5F, 2.5F, 2.5F).texOffs(1, 31)
                .addBox(-0.5F, -0.75F, -0.75F, 5.5F, 1.5F, 1.5F).texOffs(16, 31).addBox(3.75F, -1.25F, -1.25F, 2.5F, 2.5F, 2.5F),
                PartPose.offsetAndRotation(3.0F, 0.5F, -4.5F, 0.0F, 0.4363323F, 0.1745329F));
        leftFrontUpper.addOrReplaceChild("left_front_lower",
                CubeListBuilder.create().texOffs(27, 31).addBox(-0.5F, -0.5F, -0.5F, 1.0F, 3.5F, 1.0F).texOffs(32, 31).addBox(-0.75F, 2.5F, -1.0F, 1.5F, 1.0F, 2.0F),
                PartPose.offsetAndRotation(5.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.1745329F));
        PartDefinition leftRearUpper = body.addOrReplaceChild("left_rear_upper", CubeListBuilder.create().texOffs(40, 31).addBox(-1.25F, -1.25F, -1.25F, 2.5F, 2.5F, 2.5F).texOffs(51, 31)
                .addBox(-0.5F, -0.75F, -0.75F, 5.5F, 1.5F, 1.5F).texOffs(66, 31).addBox(3.75F, -1.25F, -1.25F, 2.5F, 2.5F, 2.5F),
                PartPose.offsetAndRotation(3.0F, 0.5F, -0.5F, 0.0F, -0.4363323F, 0.1745329F));
        leftRearUpper.addOrReplaceChild("left_rear_lower",
                CubeListBuilder.create().texOffs(77, 31).addBox(-0.5F, -0.5F, -0.5F, 1.0F, 3.5F, 1.0F).texOffs(82, 31).addBox(-0.75F, 2.5F, -1.0F, 1.5F, 1.0F, 2.0F),
                PartPose.offsetAndRotation(5.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.1745329F));
        PartDefinition leftArm = body.addOrReplaceChild("left_arm",
                CubeListBuilder.create().texOffs(90, 31).addBox(-1.25F, -1.25F, -1.25F, 2.5F, 2.5F, 2.5F).texOffs(101, 31).addBox(-0.75F, -0.75F, -4.5F, 1.5F, 1.5F, 5.0F),
                PartPose.offsetAndRotation(3.0F, -1.0F, -5.0F, 0.0F, -0.4886922F, 0.0F));
        PartDefinition leftClaw = leftArm.addOrReplaceChild("left_claw", CubeListBuilder.create().texOffs(1, 39).addBox(-2.0F, -1.0F, -2.0F, 4.0F, 2.5F, 3.0F).texOffs(16, 39)
                .addBox(-2.0F, -2.5F, -6.0F, 4.0F, 2.5F, 4.0F).texOffs(33, 39).addBox(-1.0F, -1.5F, -7.0F, 2.0F, 2.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 0.0F, -4.0F, 0.0F, 0.4886922F, 0.0F));
        leftClaw.addOrReplaceChild("left_pincer", CubeListBuilder.create().texOffs(40, 39).addBox(-1.5F, 0.0F, -4.5F, 3.0F, 1.0F, 4.5F),
                PartPose.offsetAndRotation(0.0F, 1.0F, -1.5F, 0.2094395F, 0.0F, 0.0F));
        PartDefinition rightFrontUpper = body.addOrReplaceChild("right_front_upper", CubeListBuilder.create().texOffs(56, 39).addBox(-1.25F, -1.25F, -1.25F, 2.5F, 2.5F, 2.5F).texOffs(67, 39)
                .addBox(-5.0F, -0.75F, -0.75F, 5.5F, 1.5F, 1.5F).texOffs(82, 39).addBox(-6.25F, -1.25F, -1.25F, 2.5F, 2.5F, 2.5F),
                PartPose.offsetAndRotation(-3.0F, 0.5F, -4.5F, 0.0F, -0.4363323F, -0.1745329F));
        rightFrontUpper.addOrReplaceChild("right_front_lower",
                CubeListBuilder.create().texOffs(93, 39).addBox(-0.5F, -0.5F, -0.5F, 1.0F, 3.5F, 1.0F).texOffs(98, 39).addBox(-0.75F, 2.5F, -1.0F, 1.5F, 1.0F, 2.0F),
                PartPose.offsetAndRotation(-5.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.1745329F));
        PartDefinition rightRearUpper = body.addOrReplaceChild("right_rear_upper", CubeListBuilder.create().texOffs(106, 39).addBox(-1.25F, -1.25F, -1.25F, 2.5F, 2.5F, 2.5F).texOffs(1, 46)
                .addBox(-5.0F, -0.75F, -0.75F, 5.5F, 1.5F, 1.5F).texOffs(16, 46).addBox(-6.25F, -1.25F, -1.25F, 2.5F, 2.5F, 2.5F),
                PartPose.offsetAndRotation(-3.0F, 0.5F, -0.5F, 0.0F, 0.4363323F, -0.1745329F));
        rightRearUpper.addOrReplaceChild("right_rear_lower",
                CubeListBuilder.create().texOffs(27, 46).addBox(-0.5F, -0.5F, -0.5F, 1.0F, 3.5F, 1.0F).texOffs(32, 46).addBox(-0.75F, 2.5F, -1.0F, 1.5F, 1.0F, 2.0F),
                PartPose.offsetAndRotation(-5.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.1745329F));
        PartDefinition rightArm = body.addOrReplaceChild("right_arm",
                CubeListBuilder.create().texOffs(40, 46).addBox(-1.25F, -1.25F, -1.25F, 2.5F, 2.5F, 2.5F).texOffs(51, 46).addBox(-0.75F, -0.75F, -4.5F, 1.5F, 1.5F, 5.0F),
                PartPose.offsetAndRotation(-3.0F, -1.0F, -5.0F, 0.0F, 0.4886922F, 0.0F));
        PartDefinition rightClaw = rightArm.addOrReplaceChild("right_claw", CubeListBuilder.create().texOffs(65, 46).addBox(-2.0F, -1.0F, -2.0F, 4.0F, 2.5F, 3.0F).texOffs(80, 46)
                .addBox(-2.0F, -2.5F, -6.0F, 4.0F, 2.5F, 4.0F).texOffs(97, 46).addBox(-1.0F, -1.5F, -7.0F, 2.0F, 2.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 0.0F, -4.0F, 0.0F, -0.4886922F, 0.0F));
        rightClaw.addOrReplaceChild("right_pincer", CubeListBuilder.create().texOffs(104, 46).addBox(-1.5F, 0.0F, -4.5F, 3.0F, 1.0F, 4.5F),
                PartPose.offsetAndRotation(0.0F, 1.0F, -1.5F, 0.2094395F, 0.0F, 0.0F));
        return LayerDefinition.create(mesh, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }

    @Override
    public void setupAnim(EldritchCrabRenderState state) {
        super.setupAnim(state);
        shell.visible = state.helm;
        bare.visible = !state.helm;
        float death = Mth.clamp(state.deathTime / 20.0F, 0.0F, 1.0F);
        float alive = 1.0F - death;
        float phase = state.ageInTicks * Mth.PI / 20.0F;
        float latch = state.latched * alive;
        float air = state.airborne * (1.0F - state.latched) * alive;
        float movement = Mth.clamp(state.walkAnimationSpeed, 0.0F, 1.0F) * (1.0F - state.airborne) * (1.0F - state.latched) * alive;
        float attack = Mth.sin(Mth.clamp(state.pinch, 0.0F, 1.0F) * Mth.PI) * alive;
        float flinch = Mth.sin(state.hurtTime * Mth.PI / 10.0F) * alive;
        float shellBreak = Mth.sin(state.shellBreakTime * Mth.PI / 8.0F) * alive;
        body.xRot -= (air * 14.0F - latch * 8.0F) * Mth.DEG_TO_RAD;
        abdomen.yRot += Mth.sin(phase) * 1.5F * alive * Mth.DEG_TO_RAD;
        abdomen.xRot -= shellBreak * 4.0F * Mth.DEG_TO_RAD;
        head.xRot += (attack * 4.0F + air * 8.0F + flinch * 3.0F) * Mth.DEG_TO_RAD;
        head.yRot += (Mth.sin(phase) * 2.0F + Mth.clamp(state.yRot, -20.0F, 20.0F) * 0.3F) * alive * Mth.DEG_TO_RAD;
        jaw.xRot -= (Mth.sin(phase * 2.0F) * 3.0F * alive + attack * 12.0F) * Mth.DEG_TO_RAD;
        animateClaw(leftArm, leftClaw, leftPincer, 1.0F, phase, attack, air, latch, death, flinch);
        animateClaw(rightArm, rightClaw, rightPincer, -1.0F, phase, attack, air, latch, death, flinch);
        float gait = state.walkAnimationPos * 0.9F;
        animateLeg(leftFrontUpper, leftFrontLower, 1.0F, gait, movement, air, latch, death);
        animateLeg(rightRearUpper, rightRearLower, -1.0F, gait, movement, air, latch, death);
        animateLeg(rightFrontUpper, rightFrontLower, -1.0F, gait + Mth.PI, movement, air, latch, death);
        animateLeg(leftRearUpper, leftRearLower, 1.0F, gait + Mth.PI, movement, air, latch, death);
    }

    private static void animateClaw(ModelPart arm, ModelPart claw, ModelPart pincer, float side, float phase, float attack, float air, float latch, float death, float flinch) {
        float alive = 1.0F - death;
        arm.xRot -= (attack * 7.0F + air * 15.0F - latch * 30.0F - flinch * 3.0F) * Mth.DEG_TO_RAD;
        arm.yRot += side * (attack * 10.0F - air * 9.0F + latch * 16.0F) * Mth.DEG_TO_RAD;
        claw.yRot += side * Mth.sin(phase + side * 0.4F) * 2.0F * alive * Mth.DEG_TO_RAD;
        float resting = Mth.sin(phase + side * 0.5F) * 4.0F * (1.0F - latch) + latch * (9.0F + Mth.sin(phase * 2.0F) * 8.0F);
        pincer.xRot -= (attack * 17.0F + resting * (1.0F - attack) * alive + death * 6.0F) * Mth.DEG_TO_RAD;
    }

    private static void animateLeg(ModelPart upper, ModelPart lower, float side, float phase, float movement, float air, float latch, float death) {
        float lift = Math.max(0.0F, Mth.sin(phase)) * 10.0F * movement;
        float sway = Mth.cos(phase) * 14.0F * movement;
        upper.yRot += side * sway * Mth.DEG_TO_RAD;
        upper.zRot -= side * (lift + air * 30.0F - latch * 30.0F - death * 25.0F) * Mth.DEG_TO_RAD;
        lower.zRot -= side * (-lift - air * 65.0F - latch * 5.0F + death * 50.0F) * Mth.DEG_TO_RAD;
    }
}
