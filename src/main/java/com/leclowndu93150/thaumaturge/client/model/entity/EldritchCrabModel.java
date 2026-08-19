package com.leclowndu93150.thaumaturge.client.model.entity;

import com.leclowndu93150.thaumaturge.client.entity.EldritchCrabRenderState;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

public final class EldritchCrabModel extends EntityModel<EldritchCrabRenderState> {
    private static final int TEX_WIDTH = 128;
    private static final int TEX_HEIGHT = 64;
    private static final float TAIL_TILT = 0.1047198F;
    private static final float LEG_SPLAY_Y = 0.2094395F;
    private static final float LEG_SPLAY_Z = 0.4363323F;
    private static final float CLAW_TILT = 0.3141593F;

    private final ModelPart tailHelm;
    private final ModelPart tailBare;
    private final ModelPart rClaw1;
    private final ModelPart rClaw2;
    private final ModelPart lClaw1;
    private final ModelPart lClaw2;
    private final ModelPart rfLeg0;
    private final ModelPart rfLeg1;
    private final ModelPart rrLeg0;
    private final ModelPart rrLeg1;
    private final ModelPart lfLeg0;
    private final ModelPart lfLeg1;
    private final ModelPart lrLeg0;
    private final ModelPart lrLeg1;

    public EldritchCrabModel(ModelPart root) {
        super(root);
        this.tailHelm = root.getChild("tail_helm");
        this.tailBare = root.getChild("tail_bare");
        this.rClaw1 = root.getChild("r_claw1");
        this.rClaw2 = root.getChild("r_claw2");
        this.lClaw1 = root.getChild("l_claw1");
        this.lClaw2 = root.getChild("l_claw2");
        this.rfLeg0 = root.getChild("rf_leg0");
        this.rfLeg1 = root.getChild("rf_leg1");
        this.rrLeg0 = root.getChild("rr_leg0");
        this.rrLeg1 = root.getChild("rr_leg1");
        this.lfLeg0 = root.getChild("lf_leg0");
        this.lfLeg1 = root.getChild("lf_leg1");
        this.lrLeg0 = root.getChild("lr_leg0");
        this.lrLeg1 = root.getChild("lr_leg1");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("tail_helm", CubeListBuilder.create().texOffs(0, 0).addBox(-4.5F, -4.5F, -0.4F, 9, 9, 9), PartPose.offsetAndRotation(0.0F, 18.0F, 0.0F, TAIL_TILT, 0.0F, 0.0F));
        root.addOrReplaceChild("tail_bare", CubeListBuilder.create().texOffs(64, 0).addBox(-4.0F, -4.0F, -0.4F, 8, 8, 8), PartPose.offsetAndRotation(0.0F, 18.0F, 0.0F, TAIL_TILT, 0.0F, 0.0F));
        root.addOrReplaceChild("r_claw1", CubeListBuilder.create().texOffs(0, 47).addBox(-2.0F, -1.0F, -5.066667F, 4, 3, 5), PartPose.offset(-6.0F, 15.5F, -10.0F));
        root.addOrReplaceChild("head1", CubeListBuilder.create().texOffs(0, 38).addBox(-2.0F, -1.5F, -9.066667F, 4, 4, 1), PartPose.offset(0.0F, 18.0F, 0.0F));
        root.addOrReplaceChild("r_claw0", CubeListBuilder.create().texOffs(0, 55).addBox(-2.0F, -2.5F, -3.066667F, 4, 5, 3), PartPose.offset(-6.0F, 17.0F, -7.0F));
        root.addOrReplaceChild("r_claw2", CubeListBuilder.create().texOffs(14, 54).addBox(-1.5F, -1.0F, -4.066667F, 3, 2, 5), PartPose.offsetAndRotation(-6.0F, 18.5F, -10.0F, CLAW_TILT, 0.0F, 0.0F));
        root.addOrReplaceChild("r_arm", CubeListBuilder.create().texOffs(44, 4).addBox(-1.0F, -1.0F, -5.066667F, 2, 2, 6), PartPose.offsetAndRotation(-3.0F, 17.0F, -4.0F, 0.0F, 0.7504916F, 0.0F));
        root.addOrReplaceChild("l_claw2", CubeListBuilder.create().texOffs(14, 54).addBox(-1.5F, -1.0F, -4.066667F, 3, 2, 5), PartPose.offsetAndRotation(6.0F, 18.5F, -10.0F, CLAW_TILT, 0.0F, 0.0F));
        root.addOrReplaceChild("l_claw1", CubeListBuilder.create().texOffs(0, 47).mirror().addBox(-2.0F, -1.0F, -5.066667F, 4, 3, 5), PartPose.offset(6.0F, 15.5F, -10.0F));
        root.addOrReplaceChild("l_claw0", CubeListBuilder.create().texOffs(0, 55).mirror().addBox(-2.0F, -2.5F, -3.066667F, 4, 5, 3), PartPose.offset(6.0F, 17.0F, -7.0F));
        root.addOrReplaceChild("l_arm", CubeListBuilder.create().texOffs(44, 4).addBox(-1.0F, -1.0F, -4.066667F, 2, 2, 6), PartPose.offsetAndRotation(4.0F, 17.0F, -5.0F, 0.0F, -0.7504916F, 0.0F));
        root.addOrReplaceChild("torso", CubeListBuilder.create().texOffs(0, 18).addBox(-3.5F, -3.5F, -6.066667F, 7, 7, 6), PartPose.offsetAndRotation(0.0F, 18.0F, 0.0F, 0.0523599F, 0.0F, 0.0F));
        root.addOrReplaceChild("head0", CubeListBuilder.create().texOffs(0, 31).addBox(-2.5F, -2.0F, -8.066667F, 5, 5, 2), PartPose.offset(0.0F, 18.0F, 0.0F));
        root.addOrReplaceChild("rr_leg1", CubeListBuilder.create().texOffs(36, 4).addBox(-4.5F, 1.0F, -0.9F, 2, 5, 2), PartPose.offset(-4.0F, 20.0F, -1.5F));
        root.addOrReplaceChild("rf_leg1", CubeListBuilder.create().texOffs(36, 4).addBox(-5.0F, 1.0F, -1.066667F, 2, 5, 2), PartPose.offset(-4.0F, 20.0F, -3.5F));
        root.addOrReplaceChild("lr_leg1", CubeListBuilder.create().texOffs(36, 4).addBox(2.5F, 1.0F, -0.9F, 2, 5, 2), PartPose.offset(4.0F, 20.0F, -1.5F));
        root.addOrReplaceChild("lf_leg1", CubeListBuilder.create().texOffs(36, 4).addBox(3.0F, 1.0F, -1.066667F, 2, 5, 2), PartPose.offset(4.0F, 20.0F, -3.5F));
        root.addOrReplaceChild("rr_leg0", CubeListBuilder.create().texOffs(36, 0).addBox(-4.5F, -1.0F, -0.9F, 6, 2, 2), PartPose.offset(-4.0F, 20.0F, -1.5F));
        root.addOrReplaceChild("rf_leg0", CubeListBuilder.create().texOffs(36, 0).addBox(-5.0F, -1.0F, -1.066667F, 6, 2, 2), PartPose.offset(-4.0F, 20.0F, -3.5F));
        root.addOrReplaceChild("lf_leg0", CubeListBuilder.create().texOffs(36, 0).addBox(-1.0F, -1.0F, -1.066667F, 6, 2, 2), PartPose.offset(4.0F, 20.0F, -3.5F));
        root.addOrReplaceChild("lr_leg0", CubeListBuilder.create().texOffs(36, 0).addBox(-1.5F, -1.0F, -0.9F, 6, 2, 2), PartPose.offset(4.0F, 20.0F, -1.5F));
        return LayerDefinition.create(mesh, TEX_WIDTH, TEX_HEIGHT);
    }

    @Override
    public void setupAnim(EldritchCrabRenderState state) {
        super.setupAnim(state);
        this.tailHelm.visible = state.helm;
        this.tailBare.visible = !state.helm;
        float limbSwing = state.walkAnimationPos;
        float limbSwingAmount = state.walkAnimationSpeed;
        setLeg(this.rrLeg0, this.rrLeg1, LEG_SPLAY_Y, LEG_SPLAY_Z);
        setLeg(this.rfLeg0, this.rfLeg1, -LEG_SPLAY_Y, LEG_SPLAY_Z);
        setLeg(this.lrLeg0, this.lrLeg1, -LEG_SPLAY_Y, -LEG_SPLAY_Z);
        setLeg(this.lfLeg0, this.lfLeg1, LEG_SPLAY_Y, -LEG_SPLAY_Z);
        float rear = -(Mth.cos(limbSwing * 0.6662F * 2.0F) * 0.4F) * limbSwingAmount;
        float front = -(Mth.cos(limbSwing * 0.6662F * 2.0F + (float) Math.PI) * 0.4F) * limbSwingAmount;
        swingLeg(this.rrLeg0, this.rrLeg1, rear);
        swingLeg(this.lrLeg0, this.lrLeg1, -rear);
        swingLeg(this.rfLeg0, this.rfLeg1, front);
        swingLeg(this.lfLeg0, this.lfLeg1, -front);
        float tailY = Mth.cos(limbSwing * 0.6662F) * 2.0F * limbSwingAmount * 0.125F;
        float tailZ = Mth.cos(limbSwing * 0.6662F) * limbSwingAmount * 0.125F;
        this.tailHelm.yRot = tailY;
        this.tailBare.yRot = tailY;
        this.tailHelm.zRot = tailZ;
        this.tailBare.zRot = tailZ;
        this.rClaw2.xRot = CLAW_TILT - Mth.sin(state.ageInTicks / 4.0F) * 0.25F;
        this.lClaw2.xRot = CLAW_TILT + Mth.sin(state.ageInTicks / 4.1F) * 0.25F;
        this.rClaw1.xRot = Mth.sin(state.ageInTicks / 4.0F) * 0.125F;
        this.lClaw1.xRot = -Mth.sin(state.ageInTicks / 4.1F) * 0.125F;
    }

    private static void setLeg(ModelPart leg0, ModelPart leg1, float yRot, float zRot) {
        leg0.yRot = yRot;
        leg0.zRot = zRot;
        leg1.yRot = yRot;
        leg1.zRot = zRot;
    }

    private static void swingLeg(ModelPart leg0, ModelPart leg1, float delta) {
        leg0.yRot += delta;
        leg0.zRot += delta;
        leg1.yRot += delta;
        leg1.zRot += delta;
    }
}
