package com.leclowndu93150.thaumaturge.client.model.gear;

import java.util.EnumSet;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.ArmorStandRenderState;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;

public final class RobeArmorModel extends AbstractTTArmorModel {
    private static final int TEXTURE_WIDTH = 128;
    private static final int TEXTURE_HEIGHT = 128;
    private static final float DEGREES_TO_RADIANS = (float) (Math.PI / 180.0);
    private static final float TAIL_REST = 6.0F * DEGREES_TO_RADIANS;
    private static final float HEM_REST = 4.0F * DEGREES_TO_RADIANS;
    private static final float LEG_PUSH = 1.25F;
    private static final float CROUCH_LIFT = 0.5F;
    private static final float TRAILING_LIFT = 0.65F;

    private final ModelPart rightTail;
    private final ModelPart rightHem;
    private final ModelPart leftTail;
    private final ModelPart leftHem;

    public RobeArmorModel(ModelPart root) {
        super(root);
        rightTail = body.getChild("tail_right_upper");
        rightHem = rightTail.getChild("tail_right_lower");
        leftTail = body.getChild("tail_left_upper");
        leftHem = leftTail.getChild("tail_left_lower");
    }

    @Override
    public void setupAnim(HumanoidRenderState state) {
        super.setupAnim(state);
        if (state instanceof ArmorStandRenderState) {
            return;
        }
        float speed = Mth.clamp(state.walkAnimationSpeed, 0.0F, 1.0F);
        float stride = Math.abs(Mth.cos(state.walkAnimationPos * 0.6662F)) * speed;
        float crouch = state.isCrouching ? 1.0F : 0.0F;
        float flutter = Mth.sin(state.ageInTicks * 0.16F) * (0.012F + speed * 0.028F);
        float trailing = state.isFallFlying || state.isVisuallySwimming ? TRAILING_LIFT : 0.0F;
        animateTail(rightTail, rightHem, rightLeg, stride, crouch, flutter, trailing);
        animateTail(leftTail, leftHem, leftLeg, stride, crouch, -flutter, trailing);
    }

    private void animateTail(ModelPart tail, ModelPart hem, ModelPart leg, float stride, float crouch, float flutter, float trailing) {
        float backLeg = Math.max(0.0F, leg.xRot - body.xRot);
        tail.xRot = TAIL_REST + backLeg * LEG_PUSH + stride * 0.1F + flutter + crouch * CROUCH_LIFT + trailing;
        hem.xRot = HEM_REST + stride * 0.12F + flutter * 1.5F;
    }

    public static LayerDefinition createHead() {
        MeshDefinition mesh = createMesh();
        PartDefinition root = mesh.getRoot();
        PartDefinition head = root.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(1, 1).addBox(-4.75F, -8.75F, -4.5F, 9.5F, 9.0F, 9.0F).texOffs(1, 20).addBox(-5.0F, -7.375F, -5.0F, 10.0F, 1.0F, 10.0F).texOffs(42, 20)
                        .addBox(-1.0F, -7.875F, -5.25F, 2.0F, 2.0F, 0.5F, EnumSet.of(Direction.NORTH, Direction.EAST, Direction.WEST, Direction.DOWN, Direction.UP)).texOffs(48, 20)
                        .addBox(-5.125F, -6.5F, -4.25F, 0.5F, 6.5F, 6.0F).texOffs(62, 20).addBox(4.625F, -6.5F, -4.25F, 0.5F, 6.5F, 6.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        head.addOrReplaceChild("hood_peak_upper", CubeListBuilder.create().texOffs(71, 59).addBox(-3.5F, -0.25F, -0.5F, 7.0F, 5.5F, 1.5F),
                PartPose.offsetAndRotation(0.0F, -8.5F, 4.5F, 0.3490659F, 0.0F, 0.0F));
        head.addOrReplaceChild("hood_peak_lower", CubeListBuilder.create().texOffs(89, 59).addBox(-2.5F, 0.5F, 0.0F, 5.0F, 4.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -5.0F, 5.0F, 0.5934119F, 0.0F, 0.0F));
        return LayerDefinition.create(mesh, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }

    public static LayerDefinition createChest() {
        MeshDefinition mesh = createMesh();
        PartDefinition root = mesh.getRoot();
        PartDefinition body = root.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(39, 1).addBox(-4.5F, 0.25F, -2.5F, 9.0F, 10.5F, 5.0F).texOffs(68, 1).addBox(-4.75F, 10.375F, -2.75F, 9.5F, 1.5F, 5.5F).texOffs(82, 20)
                        .addBox(-2.5F, 1.25F, -3.375F, 5.0F, 7.0F, 0.5F, EnumSet.of(Direction.NORTH, Direction.EAST, Direction.WEST, Direction.DOWN, Direction.UP)).texOffs(94, 20)
                        .addBox(-4.25F, -0.25F, 2.5625F, 8.5F, 10.5F, 0.5F, EnumSet.of(Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.DOWN, Direction.UP)).texOffs(104, 47)
                        .addBox(-1.0F, 10.125F, -3.0F, 2.0F, 2.0F, 0.5F, EnumSet.of(Direction.NORTH, Direction.EAST, Direction.WEST, Direction.DOWN, Direction.UP)).texOffs(110, 47)
                        .addBox(-4.375F, 11.75F, -3.375F, 2.5F, 3.0F, 1.5F).texOffs(119, 47).addBox(1.875F, 11.75F, -3.375F, 2.5F, 3.0F, 1.5F),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        body.addOrReplaceChild("sash",
                CubeListBuilder.create().texOffs(76, 20).addBox(-1.0F, -7.0F, -0.25F, 2.0F, 13.5F, 0.5F, EnumSet.of(Direction.NORTH, Direction.EAST, Direction.WEST, Direction.DOWN, Direction.UP)),
                PartPose.offsetAndRotation(0.0F, 6.0F, -2.625F, 0.0F, 0.0F, -0.6108652F));
        body.addOrReplaceChild("back_scroll", CubeListBuilder.create().texOffs(102, 59).addBox(-3.75F, -1.375F, -0.875F, 7.5F, 2.5F, 2.5F),
                PartPose.offsetAndRotation(0.0F, 9.5F, 4.0F, 0.0F, 0.0F, -0.1919862F));
        body.addOrReplaceChild("void_tome", CubeListBuilder.create().texOffs(1, 72).addBox(1.0F, 0.0F, 3.1875F, 5.0F, 7.0F, 2.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.7679449F));
        PartDefinition tailRightUpper = body.addOrReplaceChild("tail_right_upper", CubeListBuilder.create().texOffs(1, 59).addBox(-1.5F, 0.0F, -0.125F, 3.0F, 7.0F, 0.5F),
                PartPose.offsetAndRotation(-2.25F, 11.25F, 3.0F, 0.1047198F, 0.0F, 0.0F));
        tailRightUpper.addOrReplaceChild("tail_right_lower", CubeListBuilder.create().texOffs(9, 59).addBox(-1.25F, -0.125F, -0.0625F, 2.5F, 6.0F, 0.5F),
                PartPose.offsetAndRotation(0.0F, 7.0F, 0.0F, 0.0698132F, 0.0F, 0.0F));
        PartDefinition tailLeftUpper = body.addOrReplaceChild("tail_left_upper", CubeListBuilder.create().texOffs(16, 59).addBox(-1.5F, 0.0F, -0.125F, 3.0F, 7.0F, 0.5F),
                PartPose.offsetAndRotation(2.25F, 11.25F, 3.0F, 0.1047198F, 0.0F, 0.0F));
        tailLeftUpper.addOrReplaceChild("tail_left_lower", CubeListBuilder.create().texOffs(24, 59).addBox(-1.25F, -0.125F, -0.0625F, 2.5F, 6.0F, 0.5F),
                PartPose.offsetAndRotation(0.0F, 7.0F, 0.0F, 0.0698132F, 0.0F, 0.0F));
        root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(86, 35).addBox(-3.75F, -2.375F, -2.75F, 5.0F, 3.5F, 5.5F).texOffs(108, 35)
                .addBox(-3.375F, 1.25F, -2.375F, 4.5F, 6.0F, 4.5F).texOffs(20, 47).addBox(-3.5F, 9.0625F, -2.625F, 5.0F, 1.0F, 5.0F).texOffs(62, 47).addBox(-3.5F, 7.3125F, -2.625F, 5.0F, 1.0F, 5.0F),
                PartPose.offset(-5.0F, 2.0F, 0.0F));
        PartDefinition leftArm = root.addOrReplaceChild("left_arm",
                CubeListBuilder.create().texOffs(1, 35).addBox(-1.375F, -3.0F, -2.875F, 5.5F, 4.0F, 6.0F).texOffs(25, 35).addBox(3.875F, -3.5F, -2.75F, 0.5F, 3.5F, 5.5F).texOffs(1, 47)
                        .addBox(-1.125F, 1.25F, -2.375F, 4.5F, 6.0F, 4.5F).texOffs(41, 47).addBox(-1.5F, 9.0625F, -2.625F, 5.0F, 1.0F, 5.0F).texOffs(83, 47)
                        .addBox(-1.5F, 7.3125F, -2.625F, 5.0F, 1.0F, 5.0F),
                PartPose.offset(5.0F, 2.0F, 0.0F));
        leftArm.addOrReplaceChild("pauldron_lame_left0", CubeListBuilder.create().texOffs(38, 35).addBox(3.375F, -1.0F, -3.5F, 1.0F, 3.5F, 7.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.4363323F));
        leftArm.addOrReplaceChild("pauldron_lame_left1", CubeListBuilder.create().texOffs(55, 35).addBox(2.5F, 1.0F, -3.25F, 1.0F, 3.5F, 6.5F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.4363323F));
        leftArm.addOrReplaceChild("pauldron_lame_left2", CubeListBuilder.create().texOffs(71, 35).addBox(1.625F, 3.0F, -3.0F, 1.0F, 3.5F, 6.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.4363323F));
        return LayerDefinition.create(mesh, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }

    public static LayerDefinition createLegs() {
        MeshDefinition mesh = createMesh();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("right_leg",
                CubeListBuilder.create().texOffs(31, 59).addBox(-1.975F, 5.0F, -2.4375F, 3.5F, 4.5F, 0.5F, EnumSet.of(Direction.NORTH, Direction.EAST, Direction.WEST, Direction.DOWN, Direction.UP))
                        .texOffs(49, 59).addBox(-2.725F, 0.0F, -2.25F, 0.5F, 7.0F, 4.5F),
                PartPose.offset(-1.9F, 12.0F, 0.0F));
        root.addOrReplaceChild("left_leg",
                CubeListBuilder.create().texOffs(40, 59).addBox(-1.525F, 5.0F, -2.4375F, 3.5F, 4.5F, 0.5F, EnumSet.of(Direction.NORTH, Direction.EAST, Direction.WEST, Direction.DOWN, Direction.UP))
                        .texOffs(60, 59).addBox(2.225F, 0.0F, -2.25F, 0.5F, 7.0F, 4.5F),
                PartPose.offset(1.9F, 12.0F, 0.0F));
        return LayerDefinition.create(mesh, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }

    private static MeshDefinition createMesh() {
        MeshDefinition mesh = KnightArmorModel.emptyMesh();
        PartDefinition body = mesh.getRoot().getChild("body");
        for (String side : new String[]{"right", "left"}) {
            PartDefinition tail = KnightArmorModel.emptyChild(body, "tail_" + side + "_upper");
            KnightArmorModel.emptyChild(tail, "tail_" + side + "_lower");
        }
        return mesh;
    }
}
