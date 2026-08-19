package com.leclowndu93150.thaumaturge.client.model.entity;

import com.leclowndu93150.thaumaturge.client.entity.EldritchGolemRenderState;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

public final class EldritchGolemModel extends EntityModel<EldritchGolemRenderState> {
    private static final int TEX_WIDTH = 128;
    private static final int TEX_HEIGHT = 64;
    private static final float CLOAK1_TILT = 0.1396263F;
    private static final float CLOAK2_TILT = 0.3069452F;
    private static final float CLOAK3_TILT = 0.4465716F;
    private static final float HEAD_TILT = -0.1047198F;
    private static final float COLLAR_TILT = 0.837758F;
    private static final float FRONTCLOTH0_TILT = 0.1745329F;
    private static final float FRONTCLOTH1_TILT = -0.1047198F;
    private static final float FRONTCLOTH2_TILT = -0.3316126F;
    private static final float ARM_TILT = 0.1047198F;
    private static final float SHOULDER_TILT = 1.186824F;
    private static final float WAIST_TILT = 0.1396263F;
    private static final float SPAWN_HEAD_DIVISOR = 2.0F;
    private static final float ATTACK_SWING_BASE = -2.0F;
    private static final float ATTACK_SWING_SCALE = 1.5F;
    private static final float ATTACK_SWING_PERIOD = 10.0F;

    private final ModelPart head;
    private final ModelPart head2;
    private final ModelPart armL;
    private final ModelPart armR;
    private final ModelPart legL;
    private final ModelPart legR;
    private final ModelPart frontcloth1;
    private final ModelPart frontcloth2;
    private final ModelPart cloak1;
    private final ModelPart cloak2;
    private final ModelPart cloak3;

    public EldritchGolemModel(ModelPart root) {
        super(root);
        this.head = root.getChild("head");
        this.head2 = root.getChild("head2");
        this.armL = root.getChild("arm_l");
        this.armR = root.getChild("arm_r");
        this.legL = root.getChild("leg_l");
        this.legR = root.getChild("leg_r");
        this.frontcloth1 = root.getChild("frontcloth1");
        this.frontcloth2 = root.getChild("frontcloth2");
        this.cloak1 = root.getChild("cloak1");
        this.cloak2 = root.getChild("cloak2");
        this.cloak3 = root.getChild("cloak3");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("cloak1", CubeListBuilder.create().texOffs(0, 47).addBox(-5.0F, 1.5F, 4.0F, 10, 12, 1), PartPose.offsetAndRotation(0.0F, 0.0F, -2.5F, CLOAK1_TILT, 0.0F, 0.0F));
        root.addOrReplaceChild("cloak3", CubeListBuilder.create().texOffs(0, 37).addBox(-5.0F, 17.5F, -0.8F, 10, 4, 1), PartPose.offsetAndRotation(0.0F, 0.0F, -2.5F, CLOAK3_TILT, 0.0F, 0.0F));
        root.addOrReplaceChild("cloak2", CubeListBuilder.create().texOffs(0, 59).addBox(-5.0F, 13.5F, 1.7F, 10, 4, 1), PartPose.offsetAndRotation(0.0F, 0.0F, -2.5F, CLOAK2_TILT, 0.0F, 0.0F));
        root.addOrReplaceChild("cloak_cl", CubeListBuilder.create().texOffs(0, 43).addBox(3.0F, 0.5F, 2.0F, 2, 1, 3), PartPose.offsetAndRotation(0.0F, 0.0F, -2.5F, CLOAK1_TILT, 0.0F, 0.0F));
        root.addOrReplaceChild("cloak_cr", CubeListBuilder.create().texOffs(0, 43).addBox(-5.0F, 0.5F, 2.0F, 2, 1, 3), PartPose.offsetAndRotation(0.0F, 0.0F, -2.5F, CLOAK1_TILT, 0.0F, 0.0F));
        root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(47, 12).addBox(-3.5F, -6.0F, -2.5F, 7, 7, 5), PartPose.offsetAndRotation(0.0F, 4.5F, -3.8F, HEAD_TILT, 0.0F, 0.0F));
        root.addOrReplaceChild("head2", CubeListBuilder.create().texOffs(26, 16).addBox(-2.0F, -2.0F, -2.0F, 4, 4, 4), PartPose.offsetAndRotation(0.0F, 0.0F, -5.0F, HEAD_TILT, 0.0F, 0.0F));
        root.addOrReplaceChild("collar_l", CubeListBuilder.create().texOffs(75, 50).addBox(3.5F, -0.5F, -7.0F, 1, 4, 10), PartPose.offsetAndRotation(0.0F, 0.0F, -2.5F, COLLAR_TILT, 0.0F, 0.0F));
        root.addOrReplaceChild("collar_r", CubeListBuilder.create().texOffs(67, 50).addBox(-4.5F, -0.5F, -7.0F, 1, 4, 10), PartPose.offsetAndRotation(0.0F, 0.0F, -2.5F, COLLAR_TILT, 0.0F, 0.0F));
        root.addOrReplaceChild("collar_b", CubeListBuilder.create().texOffs(77, 59).addBox(-3.5F, -0.5F, 2.0F, 7, 4, 1), PartPose.offsetAndRotation(0.0F, 0.0F, -2.5F, COLLAR_TILT, 0.0F, 0.0F));
        root.addOrReplaceChild("collar_f", CubeListBuilder.create().texOffs(77, 59).addBox(-3.5F, -0.5F, -7.0F, 7, 4, 1), PartPose.offsetAndRotation(0.0F, 0.0F, -2.5F, COLLAR_TILT, 0.0F, 0.0F));
        root.addOrReplaceChild("collar_black", CubeListBuilder.create().texOffs(22, 0).addBox(-3.5F, 0.0F, -6.0F, 7, 1, 8), PartPose.offsetAndRotation(0.0F, 0.0F, -2.5F, COLLAR_TILT, 0.0F, 0.0F));
        root.addOrReplaceChild("frontcloth0", CubeListBuilder.create().texOffs(114, 52).addBox(-3.0F, 3.2F, -3.5F, 6, 10, 1),
                PartPose.offsetAndRotation(0.0F, 0.0F, -2.5F, FRONTCLOTH0_TILT, 0.0F, 0.0F));
        root.addOrReplaceChild("frontcloth1", CubeListBuilder.create().texOffs(114, 39).addBox(-1.0F, 1.5F, -3.5F, 6, 6, 1),
                PartPose.offsetAndRotation(-2.0F, 12.0F, 0.0F, FRONTCLOTH1_TILT, 0.0F, 0.0F));
        root.addOrReplaceChild("frontcloth2", CubeListBuilder.create().texOffs(114, 47).addBox(-1.0F, 8.5F, -1.5F, 6, 3, 1),
                PartPose.offsetAndRotation(-2.0F, 11.0F, 0.0F, FRONTCLOTH2_TILT, 0.0F, 0.0F));
        root.addOrReplaceChild("torso", CubeListBuilder.create().texOffs(34, 45).mirror().addBox(-5.0F, 2.5F, -3.0F, 10, 10, 6),
                PartPose.offsetAndRotation(0.0F, 0.0F, -2.5F, FRONTCLOTH0_TILT, 0.0F, 0.0F));
        PartDefinition armR = root.addOrReplaceChild("arm_r", CubeListBuilder.create().texOffs(78, 32).addBox(-3.5F, 1.5F, -2.0F, 4, 13, 5),
                PartPose.offsetAndRotation(-5.0F, 3.0F, -2.0F, 0.0F, 0.0F, ARM_TILT));
        armR.addOrReplaceChild("shoulder_r", CubeListBuilder.create().texOffs(0, 0).addBox(-4.3F, -1.0F, -3.0F, 4, 5, 7), PartPose.rotation(0.0F, 0.0F, SHOULDER_TILT));
        armR.addOrReplaceChild("shoulder_r0", CubeListBuilder.create().texOffs(56, 31).addBox(-4.5F, -1.5F, -2.5F, 5, 6, 6), PartPose.ZERO);
        armR.addOrReplaceChild("shoulder_r1", CubeListBuilder.create().texOffs(0, 23).addBox(-3.3F, 4.0F, -2.5F, 1, 2, 6), PartPose.rotation(0.0F, 0.0F, SHOULDER_TILT));
        armR.addOrReplaceChild("shoulder_r2", CubeListBuilder.create().texOffs(0, 12).addBox(-2.3F, 4.0F, -3.0F, 2, 3, 7), PartPose.rotation(0.0F, 0.0F, SHOULDER_TILT));
        PartDefinition armL = root.addOrReplaceChild("arm_l", CubeListBuilder.create().texOffs(78, 32).mirror().addBox(-0.5F, 1.5F, -2.0F, 4, 13, 5),
                PartPose.offsetAndRotation(5.0F, 3.0F, -2.0F, 0.0F, 0.0F, -ARM_TILT));
        armL.addOrReplaceChild("shoulder_l", CubeListBuilder.create().texOffs(0, 0).mirror().addBox(0.3F, -1.0F, -3.0F, 4, 5, 7), PartPose.rotation(0.0F, 0.0F, -SHOULDER_TILT));
        armL.addOrReplaceChild("shoulder_l0", CubeListBuilder.create().texOffs(56, 31).mirror().addBox(-0.5F, -1.5F, -2.5F, 5, 6, 6), PartPose.ZERO);
        armL.addOrReplaceChild("shoulder_l1", CubeListBuilder.create().texOffs(0, 23).mirror().addBox(2.3F, 4.0F, -2.5F, 1, 2, 6), PartPose.rotation(0.0F, 0.0F, -SHOULDER_TILT));
        armL.addOrReplaceChild("shoulder_l2", CubeListBuilder.create().texOffs(0, 12).mirror().addBox(0.3F, 4.0F, -3.0F, 2, 3, 7), PartPose.rotation(0.0F, 0.0F, -SHOULDER_TILT));
        root.addOrReplaceChild("backpanel_r1", CubeListBuilder.create().texOffs(96, 7).addBox(0.0F, 2.5F, -2.5F, 2, 2, 5), PartPose.offsetAndRotation(-2.0F, 12.0F, 0.0F, 0.0F, 0.0F, WAIST_TILT));
        root.addOrReplaceChild("waist_r1", CubeListBuilder.create().texOffs(96, 14).addBox(-3.0F, -0.5F, -2.5F, 5, 3, 5), PartPose.offsetAndRotation(-2.0F, 12.0F, 0.0F, 0.0F, 0.0F, WAIST_TILT));
        root.addOrReplaceChild("waist_r2", CubeListBuilder.create().texOffs(116, 13).addBox(-3.0F, 2.5F, -2.5F, 1, 4, 5), PartPose.offsetAndRotation(-2.0F, 12.0F, 0.0F, 0.0F, 0.0F, WAIST_TILT));
        root.addOrReplaceChild("waist_r3", CubeListBuilder.create().texOffs(114, 5).mirror().addBox(-2.0F, 2.5F, -2.5F, 2, 3, 5),
                PartPose.offsetAndRotation(-2.0F, 12.0F, 0.0F, 0.0F, 0.0F, WAIST_TILT));
        root.addOrReplaceChild("leg_r", CubeListBuilder.create().texOffs(79, 19).addBox(-2.5F, 2.5F, -2.0F, 4, 9, 4), PartPose.offset(-2.0F, 12.5F, 0.0F));
        root.addOrReplaceChild("waist_l1", CubeListBuilder.create().texOffs(96, 14).mirror().addBox(-2.0F, -0.5F, -2.5F, 5, 3, 5),
                PartPose.offsetAndRotation(2.0F, 12.0F, 0.0F, 0.0F, 0.0F, -WAIST_TILT));
        root.addOrReplaceChild("waist_l2", CubeListBuilder.create().texOffs(116, 13).mirror().addBox(2.0F, 2.5F, -2.5F, 1, 4, 5),
                PartPose.offsetAndRotation(2.0F, 12.0F, 0.0F, 0.0F, 0.0F, -WAIST_TILT));
        root.addOrReplaceChild("waist_l3", CubeListBuilder.create().texOffs(114, 5).mirror().addBox(0.0F, 2.5F, -2.5F, 2, 3, 5),
                PartPose.offsetAndRotation(2.0F, 12.0F, 0.0F, 0.0F, 0.0F, -WAIST_TILT));
        root.addOrReplaceChild("backpanel_l1", CubeListBuilder.create().texOffs(96, 7).mirror().addBox(-2.0F, 2.5F, -2.5F, 2, 2, 5),
                PartPose.offsetAndRotation(2.0F, 12.0F, 0.0F, 0.0F, 0.0F, -WAIST_TILT));
        root.addOrReplaceChild("leg_l", CubeListBuilder.create().texOffs(79, 19).mirror().addBox(-1.5F, 2.5F, -2.0F, 4, 9, 4), PartPose.offset(2.0F, 12.5F, 0.0F));
        return LayerDefinition.create(mesh, TEX_WIDTH, TEX_HEIGHT);
    }

    @Override
    public void setupAnim(EldritchGolemRenderState state) {
        super.setupAnim(state);
        this.head.visible = !state.headless;
        this.head2.visible = state.headless;
        if (state.spawnTimer > 0) {
            this.head.yRot = 0.0F;
            this.head.xRot = state.spawnTimer / SPAWN_HEAD_DIVISOR * Mth.DEG_TO_RAD;
        } else {
            this.head.yRot = state.yRot / 4.0F * Mth.DEG_TO_RAD;
            this.head.xRot = HEAD_TILT + state.xRot / 2.0F * Mth.DEG_TO_RAD;
            this.head2.yRot = state.yRot * Mth.DEG_TO_RAD;
            this.head2.xRot = HEAD_TILT + state.xRot * Mth.DEG_TO_RAD;
        }
        float limbPos = state.walkAnimationPos;
        float limbSpeed = state.walkAnimationSpeed;
        this.legR.xRot = Mth.cos(limbPos * 0.4662F) * 1.4F * limbSpeed;
        this.legL.xRot = Mth.cos(limbPos * 0.4662F + Mth.PI) * 1.4F * limbSpeed;
        float a = Mth.cos(limbPos * 0.44F) * 1.4F * limbSpeed;
        float b = Mth.cos(limbPos * 0.44F + Mth.PI) * 1.4F * limbSpeed;
        float c = Math.min(a, b);
        this.frontcloth1.xRot = c + FRONTCLOTH1_TILT;
        this.frontcloth2.xRot = c + FRONTCLOTH2_TILT;
        this.cloak1.xRot = -c / 3.0F + CLOAK1_TILT;
        this.cloak2.xRot = -c / 3.0F + CLOAK2_TILT;
        this.cloak3.xRot = -c / 3.0F + CLOAK3_TILT;
        if (state.attackTime > 0.0F) {
            float swing = ATTACK_SWING_BASE + ATTACK_SWING_SCALE * triangleWave(state.attackTime, ATTACK_SWING_PERIOD);
            this.armR.xRot = swing;
            this.armL.xRot = swing;
        } else {
            this.armR.xRot = Mth.cos(limbPos * 0.4F + Mth.PI) * 2.0F * limbSpeed * 0.5F;
            this.armL.xRot = Mth.cos(limbPos * 0.4F) * 2.0F * limbSpeed * 0.5F;
        }
    }

    private static float triangleWave(float progress, float period) {
        return (Math.abs(progress % period - period * 0.5F) - period * 0.25F) / (period * 0.25F);
    }
}
