package com.leclowndu93150.thaumaturge.client.model.entity;

import com.leclowndu93150.thaumaturge.client.entity.PechRenderState;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

public final class PechModel extends EntityModel<PechRenderState> {
    private static final int TEX_WIDTH = 128;
    private static final int TEX_HEIGHT = 64;
    private static final float BODY_TILT = 0.3129957F;
    private static final float LOWER_PACK_TILT = 0.3013602F;
    private static final float UPPER_PACK_TILT = 0.4537856F;
    private static final float JOWL_BASE_TILT = (float) (Math.PI / 12);
    private static final float JOWL_MUMBLE_SWING = 0.34906587F;

    public final ModelPart head;
    public final ModelPart jowls;
    public final ModelPart body;
    public final ModelPart rightArm;
    public final ModelPart leftArm;
    public final ModelPart rightLeg;
    public final ModelPart leftLeg;
    public final ModelPart lowerPack;

    public PechModel(ModelPart root) {
        super(root);
        this.head = root.getChild("head");
        this.jowls = root.getChild("jowls");
        this.body = root.getChild("body");
        this.rightArm = root.getChild("right_arm");
        this.leftArm = root.getChild("left_arm");
        this.rightLeg = root.getChild("right_leg");
        this.leftLeg = root.getChild("left_leg");
        this.lowerPack = root.getChild("lower_pack");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(34, 12).addBox(-3.0F, 0.0F, 0.0F, 6, 10, 6), PartPose.offsetAndRotation(0.0F, 9.0F, -3.0F, BODY_TILT, 0.0F, 0.0F));
        root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(35, 1).mirror().addBox(-2.9F, 0.0F, 0.0F, 3, 6, 3), PartPose.offset(0.0F, 18.0F, 0.0F));
        root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(35, 1).addBox(-0.1F, 0.0F, 0.0F, 3, 6, 3), PartPose.offset(0.0F, 18.0F, 0.0F));
        root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(2, 11).addBox(-3.5F, -5.0F, -5.0F, 7, 5, 5), PartPose.offset(0.0F, 8.0F, 0.0F));
        root.addOrReplaceChild("jowls", CubeListBuilder.create().texOffs(1, 21).addBox(-4.0F, -1.0F, -6.0F, 8, 3, 5), PartPose.offset(0.0F, 8.0F, 0.0F));
        root.addOrReplaceChild("lower_pack", CubeListBuilder.create().texOffs(0, 0).addBox(-5.0F, 0.0F, 0.0F, 10, 5, 5), PartPose.offsetAndRotation(0.0F, 10.0F, 3.5F, LOWER_PACK_TILT, 0.0F, 0.0F));
        root.addOrReplaceChild("upper_pack", CubeListBuilder.create().texOffs(64, 1).addBox(-7.5F, -14.0F, 0.0F, 15, 14, 11),
                PartPose.offsetAndRotation(0.0F, 10.0F, 3.0F, UPPER_PACK_TILT, 0.0F, 0.0F));
        root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(52, 2).mirror().addBox(-2.0F, 0.0F, -1.0F, 2, 6, 2), PartPose.offset(-3.0F, 10.0F, -1.0F));
        root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(52, 2).addBox(0.0F, 0.0F, -1.0F, 2, 6, 2), PartPose.offset(3.0F, 10.0F, -1.0F));
        return LayerDefinition.create(mesh, TEX_WIDTH, TEX_HEIGHT);
    }

    @Override
    public void setupAnim(PechRenderState state) {
        super.setupAnim(state);
        float limbSwing = state.walkAnimationPos;
        float limbSwingAmount = state.walkAnimationSpeed;
        this.head.yRot = state.yRot * (float) (Math.PI / 180.0);
        this.head.xRot = state.xRot * (float) (Math.PI / 180.0);
        this.jowls.yRot = this.head.yRot;
        this.jowls.xRot = this.head.xRot + (JOWL_BASE_TILT + Mth.cos(limbSwing * 0.6662F) * limbSwingAmount * 0.25F) + JOWL_MUMBLE_SWING * Math.abs(Mth.sin(state.mumble / 8.0F));
        this.rightArm.xRot = Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * 2.0F * limbSwingAmount * 0.5F;
        this.leftArm.xRot = Mth.cos(limbSwing * 0.6662F) * 2.0F * limbSwingAmount * 0.5F;
        this.rightArm.zRot = 0.0F;
        this.leftArm.zRot = 0.0F;
        this.rightLeg.xRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
        this.leftLeg.xRot = Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * 1.4F * limbSwingAmount;
        this.rightLeg.yRot = 0.0F;
        this.leftLeg.yRot = 0.0F;
        this.lowerPack.yRot = Mth.cos(limbSwing * 0.6662F) * 2.0F * limbSwingAmount * 0.125F;
        this.lowerPack.zRot = Mth.cos(limbSwing * 0.6662F) * 2.0F * limbSwingAmount * 0.125F;
        if (state.attackTime > 0.0F) {
            float swing = 1.0F - state.attackTime;
            swing *= swing;
            swing *= swing;
            swing = 1.0F - swing;
            float f7 = Mth.sin(swing * (float) Math.PI);
            float f8 = Mth.sin(state.attackTime * (float) Math.PI) * -(this.head.xRot - 0.7F) * 0.75F;
            this.rightArm.xRot -= f7 * 1.2F + f8;
            this.rightArm.zRot = Mth.sin(state.attackTime * (float) Math.PI) * -0.4F;
        }
        this.rightArm.zRot += Mth.cos(state.ageInTicks * 0.09F) * 0.05F + 0.05F;
        this.leftArm.zRot -= Mth.cos(state.ageInTicks * 0.09F) * 0.05F + 0.05F;
        this.rightArm.xRot += Mth.sin(state.ageInTicks * 0.067F) * 0.05F;
        this.leftArm.xRot -= Mth.sin(state.ageInTicks * 0.067F) * 0.05F;
    }
}
