package com.leclowndu93150.thaumaturge.client.model.gear;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.util.Mth;

public final class RobeArmorModel extends AbstractTCArmorModel {
    private static final int TEX_WIDTH = 128;
    private static final int TEX_HEIGHT = 64;
    private static final float FRONTCLOTH_1_ROT = -0.1047198F;
    private static final float FRONTCLOTH_2_ROT = -0.3316126F;
    private static final float BACKCLOTH_1_ROT = 0.1047198F;
    private static final float BACKCLOTH_2_ROT = 0.2268928F;
    private static final float SHOULDERPLATE_ROT = 0.4363323F;
    private static final float SIDECLOTH_1_ROT = 0.122173F;
    private static final float SIDECLOTH_2_ROT = 0.296706F;
    private static final float SIDECLOTH_3_ROT = (float) (Math.PI / 6);

    private final ModelPart frontclothR1;
    private final ModelPart frontclothR2;
    private final ModelPart frontclothL1;
    private final ModelPart frontclothL2;
    private final ModelPart clothBackR1;
    private final ModelPart clothBackR2;
    private final ModelPart clothBackR3;
    private final ModelPart clothBackL1;
    private final ModelPart clothBackL2;
    private final ModelPart clothBackL3;

    public RobeArmorModel(ModelPart root) {
        super(root);
        this.frontclothR1 = this.body.getChild("frontcloth_r1");
        this.frontclothR2 = this.body.getChild("frontcloth_r2");
        this.frontclothL1 = this.body.getChild("frontcloth_l1");
        this.frontclothL2 = this.body.getChild("frontcloth_l2");
        this.clothBackR1 = this.body.getChild("clothback_r1");
        this.clothBackR2 = this.body.getChild("clothback_r2");
        this.clothBackR3 = this.body.getChild("clothback_r3");
        this.clothBackL1 = this.body.getChild("clothback_l1");
        this.clothBackL2 = this.body.getChild("clothback_l2");
        this.clothBackL3 = this.body.getChild("clothback_l3");
    }

    public static LayerDefinition createHead() {
        MeshDefinition mesh = emptyMesh();
        PartDefinition head = mesh.getRoot().getChild("head");
        head.addOrReplaceChild("hood1", CubeListBuilder.create().texOffs(16, 7).addBox(-4.5F, -9.0F, -4.6F, 9, 9, 9), PartPose.ZERO);
        head.addOrReplaceChild("hood2", CubeListBuilder.create().texOffs(52, 13).addBox(-4.0F, -9.7F, 2.0F, 8, 9, 3), PartPose.rotation(-0.2268928F, 0.0F, 0.0F));
        head.addOrReplaceChild("hood3", CubeListBuilder.create().texOffs(52, 14).addBox(-3.5F, -10.0F, 3.5F, 7, 8, 3), PartPose.rotation(-0.3490659F, 0.0F, 0.0F));
        head.addOrReplaceChild("hood4", CubeListBuilder.create().texOffs(53, 15).addBox(-3.0F, -10.7F, 3.5F, 6, 7, 3), PartPose.rotation(-0.5759587F, 0.0F, 0.0F));
        return LayerDefinition.create(mesh, TEX_WIDTH, TEX_HEIGHT);
    }

    public static LayerDefinition createChest() {
        MeshDefinition mesh = emptyMesh();
        PartDefinition root = mesh.getRoot();
        PartDefinition body = root.getChild("body");
        addBelts(body);
        body.addOrReplaceChild("chestplate", CubeListBuilder.create().texOffs(16, 25).addBox(-4.0F, 1.0F, -3.0F, 8, 6, 1), PartPose.ZERO);
        body.addOrReplaceChild("chestthing", CubeListBuilder.create().texOffs(56, 50).addBox(-2.5F, 1.0F, -4.0F, 5, 7, 1), PartPose.ZERO);
        body.addOrReplaceChild("scroll", CubeListBuilder.create().texOffs(78, 25).addBox(-2.0F, 9.5F, 4.0F, 8, 3, 3), PartPose.rotation(0.0F, 0.0F, 0.1919862F));
        body.addOrReplaceChild("backplate", CubeListBuilder.create().texOffs(36, 45).addBox(-4.0F, 1.0F, 1.9F, 8, 11, 2), PartPose.ZERO);
        body.addOrReplaceChild("book", CubeListBuilder.create().texOffs(81, 16).addBox(1.0F, 0.0F, 4.0F, 5, 7, 2), PartPose.rotation(0.0F, 0.0F, 0.7679449F));
        body.addOrReplaceChild("clothchest_l", CubeListBuilder.create().texOffs(108, 38).mirror().addBox(2.1F, 0.5F, -3.5F, 2, 8, 1), PartPose.ZERO);
        body.addOrReplaceChild("clothchest_r", CubeListBuilder.create().texOffs(108, 38).addBox(-4.1F, 0.5F, -3.5F, 2, 8, 1), PartPose.ZERO);
        PartDefinition rightArm = root.getChild("right_arm");
        rightArm.addOrReplaceChild("shoulder_r", CubeListBuilder.create().texOffs(16, 45).mirror().addBox(-3.5F, -2.5F, -2.5F, 5, 5, 5), PartPose.ZERO);
        rightArm.addOrReplaceChild("r_arm1", CubeListBuilder.create().texOffs(88, 39).addBox(-3.5F, 2.5F, -2.5F, 5, 7, 5), PartPose.ZERO);
        rightArm.addOrReplaceChild("r_arm2", CubeListBuilder.create().texOffs(76, 32).addBox(-3.0F, 5.5F, 2.5F, 4, 4, 2), PartPose.ZERO);
        rightArm.addOrReplaceChild("r_arm3", CubeListBuilder.create().texOffs(88, 32).addBox(-2.5F, 3.5F, 2.5F, 3, 2, 1), PartPose.ZERO);
        rightArm.addOrReplaceChild("shoulderplate_top_r", CubeListBuilder.create().texOffs(56, 25).addBox(-5.5F, -2.5F, -3.5F, 2, 1, 7), PartPose.rotation(0.0F, 0.0F, SHOULDERPLATE_ROT));
        rightArm.addOrReplaceChild("shoulderplate_r1", CubeListBuilder.create().texOffs(56, 33).addBox(-4.5F, -1.5F, -3.5F, 1, 4, 7), PartPose.rotation(0.0F, 0.0F, SHOULDERPLATE_ROT));
        rightArm.addOrReplaceChild("shoulderplate_r2", CubeListBuilder.create().texOffs(40, 33).addBox(-3.5F, 1.5F, -3.5F, 1, 3, 7), PartPose.rotation(0.0F, 0.0F, SHOULDERPLATE_ROT));
        rightArm.addOrReplaceChild("shoulderplate_r3", CubeListBuilder.create().texOffs(40, 33).addBox(-2.5F, 3.5F, -3.5F, 1, 3, 7), PartPose.rotation(0.0F, 0.0F, SHOULDERPLATE_ROT));
        PartDefinition leftArm = root.getChild("left_arm");
        leftArm.addOrReplaceChild("shoulder_l", CubeListBuilder.create().texOffs(16, 45).addBox(-1.5F, -2.5F, -2.5F, 5, 5, 5), PartPose.ZERO);
        leftArm.addOrReplaceChild("l_arm1", CubeListBuilder.create().texOffs(88, 39).mirror().addBox(-1.5F, 2.5F, -2.5F, 5, 7, 5), PartPose.ZERO);
        leftArm.addOrReplaceChild("l_arm2", CubeListBuilder.create().texOffs(76, 32).addBox(-1.0F, 5.5F, 2.5F, 4, 4, 2), PartPose.ZERO);
        leftArm.addOrReplaceChild("l_arm3", CubeListBuilder.create().texOffs(88, 32).addBox(-0.5F, 3.5F, 2.5F, 3, 2, 1), PartPose.ZERO);
        leftArm.addOrReplaceChild("shoulderplate_top_l", CubeListBuilder.create().texOffs(56, 25).addBox(3.5F, -2.5F, -3.5F, 2, 1, 7), PartPose.rotation(0.0F, 0.0F, -SHOULDERPLATE_ROT));
        leftArm.addOrReplaceChild("shoulderplate_l1", CubeListBuilder.create().texOffs(56, 33).addBox(3.5F, -1.5F, -3.5F, 1, 4, 7), PartPose.rotation(0.0F, 0.0F, -SHOULDERPLATE_ROT));
        leftArm.addOrReplaceChild("shoulderplate_l2", CubeListBuilder.create().texOffs(40, 33).addBox(2.5F, 1.5F, -3.5F, 1, 3, 7), PartPose.rotation(0.0F, 0.0F, -SHOULDERPLATE_ROT));
        leftArm.addOrReplaceChild("shoulderplate_l3", CubeListBuilder.create().texOffs(40, 33).addBox(1.5F, 3.5F, -3.5F, 1, 3, 7), PartPose.rotation(0.0F, 0.0F, -SHOULDERPLATE_ROT));
        return LayerDefinition.create(mesh, TEX_WIDTH, TEX_HEIGHT);
    }

    public static LayerDefinition createLegs() {
        MeshDefinition mesh = emptyMesh();
        PartDefinition root = mesh.getRoot();
        PartDefinition body = root.getChild("body");
        addBelts(body);
        body.addOrReplaceChild("frontcloth_r1", CubeListBuilder.create().texOffs(108, 38).addBox(0.0F, 0.0F, 0.0F, 3, 8, 1),
                PartPose.offsetAndRotation(-3.0F, 11.0F, -2.9F, FRONTCLOTH_1_ROT, 0.0F, 0.0F));
        body.addOrReplaceChild("frontcloth_r2", CubeListBuilder.create().texOffs(108, 47).addBox(0.0F, 7.5F, 1.7F, 3, 3, 1),
                PartPose.offsetAndRotation(-3.0F, 11.0F, -2.9F, FRONTCLOTH_2_ROT, 0.0F, 0.0F));
        body.addOrReplaceChild("frontcloth_l1", CubeListBuilder.create().texOffs(108, 38).mirror().addBox(0.0F, 0.0F, 0.0F, 3, 8, 1),
                PartPose.offsetAndRotation(0.0F, 11.0F, -2.9F, FRONTCLOTH_1_ROT, 0.0F, 0.0F));
        body.addOrReplaceChild("frontcloth_l2", CubeListBuilder.create().texOffs(108, 47).mirror().addBox(0.0F, 7.5F, 1.7F, 3, 3, 1),
                PartPose.offsetAndRotation(0.0F, 11.0F, -2.9F, FRONTCLOTH_2_ROT, 0.0F, 0.0F));
        body.addOrReplaceChild("clothback_r1", CubeListBuilder.create().texOffs(118, 16).mirror().addBox(0.0F, 0.0F, 0.0F, 4, 8, 1),
                PartPose.offsetAndRotation(-4.0F, 11.5F, 2.9F, BACKCLOTH_1_ROT, 0.0F, 0.0F));
        body.addOrReplaceChild("clothback_r2", CubeListBuilder.create().texOffs(123, 9).addBox(0.0F, 7.8F, -0.9F, 1, 2, 1),
                PartPose.offsetAndRotation(-4.0F, 11.5F, 2.9F, BACKCLOTH_2_ROT, 0.0F, 0.0F));
        body.addOrReplaceChild("clothback_r3", CubeListBuilder.create().texOffs(120, 12).mirror().addBox(1.0F, 7.8F, -0.9F, 3, 3, 1),
                PartPose.offsetAndRotation(-4.0F, 11.5F, 2.9F, BACKCLOTH_2_ROT, 0.0F, 0.0F));
        body.addOrReplaceChild("clothback_l1", CubeListBuilder.create().texOffs(118, 16).addBox(0.0F, 0.0F, 0.0F, 4, 8, 1), PartPose.offsetAndRotation(0.0F, 11.5F, 2.9F, BACKCLOTH_1_ROT, 0.0F, 0.0F));
        body.addOrReplaceChild("clothback_l2", CubeListBuilder.create().texOffs(123, 9).mirror().addBox(3.0F, 7.8F, -0.9F, 1, 2, 1),
                PartPose.offsetAndRotation(0.0F, 11.5F, 2.9F, BACKCLOTH_2_ROT, 0.0F, 0.0F));
        body.addOrReplaceChild("clothback_l3", CubeListBuilder.create().texOffs(120, 12).addBox(0.0F, 7.8F, -0.9F, 3, 3, 1),
                PartPose.offsetAndRotation(0.0F, 11.5F, 2.9F, BACKCLOTH_2_ROT, 0.0F, 0.0F));
        PartDefinition rightLeg = root.getChild("right_leg");
        rightLeg.addOrReplaceChild("legpanel_r4", CubeListBuilder.create().texOffs(76, 38).addBox(-3.0F, 0.5F, -3.5F, 2, 3, 1), PartPose.rotation(-SHOULDERPLATE_ROT, 0.0F, 0.0F));
        rightLeg.addOrReplaceChild("legpanel_r5", CubeListBuilder.create().texOffs(76, 42).addBox(-3.0F, 2.5F, -2.5F, 2, 3, 1), PartPose.rotation(-SHOULDERPLATE_ROT, 0.0F, 0.0F));
        rightLeg.addOrReplaceChild("legpanel_r6", CubeListBuilder.create().texOffs(82, 38).addBox(-3.0F, 4.5F, -1.5F, 2, 3, 1), PartPose.rotation(-SHOULDERPLATE_ROT, 0.0F, 0.0F));
        rightLeg.addOrReplaceChild("sidepanel_r1", CubeListBuilder.create().texOffs(116, 25).addBox(-2.5F, 0.5F, -2.5F, 1, 4, 5), PartPose.rotation(0.0F, 0.0F, SHOULDERPLATE_ROT));
        rightLeg.addOrReplaceChild("sidecloth_r1", CubeListBuilder.create().texOffs(116, 42).addBox(-2.5F, 0.5F, -2.5F, 1, 5, 5), PartPose.rotation(0.0F, 0.0F, SIDECLOTH_1_ROT));
        rightLeg.addOrReplaceChild("sidecloth_r2", CubeListBuilder.create().texOffs(116, 34).addBox(-1.5F, 5.5F, -2.5F, 1, 3, 5), PartPose.rotation(0.0F, 0.0F, SIDECLOTH_2_ROT));
        rightLeg.addOrReplaceChild("sidecloth_r3", CubeListBuilder.create().texOffs(116, 1).addBox(0.4F, 8.4F, -2.5F, 1, 3, 5), PartPose.rotation(0.0F, 0.0F, SIDECLOTH_3_ROT));
        PartDefinition leftLeg = root.getChild("left_leg");
        leftLeg.addOrReplaceChild("legpanel_l4", CubeListBuilder.create().texOffs(76, 38).mirror().addBox(1.0F, 0.5F, -3.5F, 2, 3, 1), PartPose.rotation(-SHOULDERPLATE_ROT, 0.0F, 0.0F));
        leftLeg.addOrReplaceChild("legpanel_l5", CubeListBuilder.create().texOffs(76, 42).mirror().addBox(1.0F, 2.5F, -2.5F, 2, 3, 1), PartPose.rotation(-SHOULDERPLATE_ROT, 0.0F, 0.0F));
        leftLeg.addOrReplaceChild("legpanel_l6", CubeListBuilder.create().texOffs(82, 38).mirror().addBox(1.0F, 4.5F, -1.5F, 2, 3, 1), PartPose.rotation(-SHOULDERPLATE_ROT, 0.0F, 0.0F));
        leftLeg.addOrReplaceChild("sidepanel_l1", CubeListBuilder.create().texOffs(116, 25).addBox(1.5F, 0.5F, -2.5F, 1, 4, 5), PartPose.rotation(0.0F, 0.0F, -SHOULDERPLATE_ROT));
        leftLeg.addOrReplaceChild("sidecloth_l1", CubeListBuilder.create().texOffs(116, 42).addBox(1.5F, 0.5F, -2.5F, 1, 5, 5), PartPose.rotation(0.0F, 0.0F, -SIDECLOTH_1_ROT));
        leftLeg.addOrReplaceChild("sidecloth_l2", CubeListBuilder.create().texOffs(116, 34).addBox(0.5F, 5.5F, -2.5F, 1, 3, 5), PartPose.rotation(0.0F, 0.0F, -SIDECLOTH_2_ROT));
        leftLeg.addOrReplaceChild("sidecloth_l3", CubeListBuilder.create().texOffs(116, 1).addBox(-1.4F, 8.4F, -2.5F, 1, 3, 5), PartPose.rotation(0.0F, 0.0F, -SIDECLOTH_3_ROT));
        leftLeg.addOrReplaceChild("focipouch", CubeListBuilder.create().texOffs(100, 20).addBox(3.5F, 0.5F, -2.5F, 3, 6, 5), PartPose.rotation(0.0F, 0.0F, -SIDECLOTH_1_ROT));
        return LayerDefinition.create(mesh, TEX_WIDTH, TEX_HEIGHT);
    }

    private static void addBelts(PartDefinition body) {
        body.addOrReplaceChild("mbelt", CubeListBuilder.create().texOffs(16, 55).addBox(-4.0F, 7.0F, -3.0F, 8, 5, 1), PartPose.ZERO);
        body.addOrReplaceChild("mbelt_b", CubeListBuilder.create().texOffs(16, 55).addBox(-4.0F, 7.0F, -4.0F, 8, 5, 1), PartPose.rotation(0.0F, (float) Math.PI, 0.0F));
        body.addOrReplaceChild("mbelt_l", CubeListBuilder.create().texOffs(16, 36).addBox(4.0F, 8.0F, -3.0F, 1, 3, 6), PartPose.ZERO);
        body.addOrReplaceChild("mbelt_r", CubeListBuilder.create().texOffs(16, 36).addBox(-5.0F, 8.0F, -3.0F, 1, 3, 6), PartPose.ZERO);
    }

    private static MeshDefinition emptyMesh() {
        MeshDefinition mesh = KnightArmorModel.emptyMesh();
        PartDefinition body = mesh.getRoot().getChild("body");
        KnightArmorModel.emptyChild(body, "frontcloth_r1");
        KnightArmorModel.emptyChild(body, "frontcloth_r2");
        KnightArmorModel.emptyChild(body, "frontcloth_l1");
        KnightArmorModel.emptyChild(body, "frontcloth_l2");
        KnightArmorModel.emptyChild(body, "clothback_r1");
        KnightArmorModel.emptyChild(body, "clothback_r2");
        KnightArmorModel.emptyChild(body, "clothback_r3");
        KnightArmorModel.emptyChild(body, "clothback_l1");
        KnightArmorModel.emptyChild(body, "clothback_l2");
        KnightArmorModel.emptyChild(body, "clothback_l3");
        return mesh;
    }

    @Override
    public void setupAnim(HumanoidRenderState state) {
        super.setupAnim(state);
        float a = Mth.cos(state.walkAnimationPos * 0.6662F) * 1.4F * state.walkAnimationSpeed;
        float b = Mth.cos(state.walkAnimationPos * 0.6662F + (float) Math.PI) * 1.4F * state.walkAnimationSpeed;
        float c = Math.min(a, b);
        this.frontclothR1.xRot = c + FRONTCLOTH_1_ROT;
        this.frontclothL1.xRot = c + FRONTCLOTH_1_ROT;
        this.frontclothR2.xRot = c + FRONTCLOTH_2_ROT;
        this.frontclothL2.xRot = c + FRONTCLOTH_2_ROT;
        this.clothBackR1.xRot = -c + BACKCLOTH_1_ROT;
        this.clothBackL1.xRot = -c + BACKCLOTH_1_ROT;
        this.clothBackR2.xRot = -c + BACKCLOTH_2_ROT;
        this.clothBackL2.xRot = -c + BACKCLOTH_2_ROT;
        this.clothBackR3.xRot = -c + BACKCLOTH_2_ROT;
        this.clothBackL3.xRot = -c + BACKCLOTH_2_ROT;
    }
}
