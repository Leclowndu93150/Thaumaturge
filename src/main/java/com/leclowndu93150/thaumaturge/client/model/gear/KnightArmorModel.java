package com.leclowndu93150.thaumaturge.client.model.gear;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.util.Mth;

public final class KnightArmorModel extends AbstractTCArmorModel {
    private static final int TEX_WIDTH = 128;
    private static final int TEX_HEIGHT = 64;
    private static final float QUARTER_PI = (float) (Math.PI / 4);
    private static final float FRONTCLOTH_1_ROT = -0.1047198F;
    private static final float FRONTCLOTH_2_ROT = -0.3316126F;
    private static final float CLOAK_1_ROT = 0.1396263F;
    private static final float CLOAK_2_ROT = 0.3069452F;
    private static final float CLOAK_3_ROT = 0.4465716F;
    private static final float SIDEPANEL_ROT = 0.1396263F;

    private final ModelPart frontcloth1;
    private final ModelPart frontcloth2;
    private final ModelPart cloak1;
    private final ModelPart cloak2;
    private final ModelPart cloak3;

    public KnightArmorModel(ModelPart root) {
        super(root);
        this.frontcloth1 = this.body.getChild("frontcloth1");
        this.frontcloth2 = this.body.getChild("frontcloth2");
        this.cloak1 = this.body.getChild("cloak1");
        this.cloak2 = this.body.getChild("cloak2");
        this.cloak3 = this.body.getChild("cloak3");
    }

    public static LayerDefinition createHead() {
        MeshDefinition mesh = emptyMesh();
        PartDefinition root = mesh.getRoot();
        root.getChild("head").addOrReplaceChild("helmet", CubeListBuilder.create().texOffs(41, 8).addBox(-4.5F, -9.0F, -4.5F, 9, 9, 9), PartPose.ZERO);
        return LayerDefinition.create(mesh, TEX_WIDTH, TEX_HEIGHT);
    }

    public static LayerDefinition createChest() {
        MeshDefinition mesh = emptyMesh();
        PartDefinition root = mesh.getRoot();
        PartDefinition body = root.getChild("body");
        addBelts(body);
        body.addOrReplaceChild("chestplate", CubeListBuilder.create().texOffs(56, 45).addBox(-4.0F, 1.0F, -3.0F, 8, 7, 1), PartPose.ZERO);
        body.addOrReplaceChild("frontcloth1", CubeListBuilder.create().texOffs(120, 39).addBox(0.0F, 0.0F, 0.0F, 6, 8, 1),
                PartPose.offsetAndRotation(-3.0F, 11.0F, -3.5F, FRONTCLOTH_1_ROT, 0.0F, 0.0F));
        body.addOrReplaceChild("frontcloth2", CubeListBuilder.create().texOffs(100, 37).addBox(0.0F, 7.5F, 1.8F, 6, 3, 1),
                PartPose.offsetAndRotation(-3.0F, 11.0F, -3.5F, FRONTCLOTH_2_ROT, 0.0F, 0.0F));
        body.addOrReplaceChild("tabbard", CubeListBuilder.create().texOffs(114, 52).addBox(-3.0F, 1.2F, -3.5F, 6, 10, 1), PartPose.ZERO);
        body.addOrReplaceChild("backplate", CubeListBuilder.create().texOffs(36, 45).addBox(-4.0F, 1.0F, 2.0F, 8, 11, 2), PartPose.ZERO);
        body.addOrReplaceChild("cloak1", CubeListBuilder.create().texOffs(0, 47).addBox(0.0F, 0.0F, 0.0F, 9, 12, 1), PartPose.offsetAndRotation(-4.5F, 1.3F, 4.2F, CLOAK_1_ROT, 0.0F, 0.0F));
        body.addOrReplaceChild("cloak2", CubeListBuilder.create().texOffs(0, 59).addBox(0.0F, 11.7F, -2.0F, 9, 4, 1), PartPose.offsetAndRotation(-4.5F, 1.3F, 4.2F, CLOAK_2_ROT, 0.0F, 0.0F));
        body.addOrReplaceChild("cloak3", CubeListBuilder.create().texOffs(0, 59).addBox(0.0F, 15.2F, -4.2F, 9, 4, 1), PartPose.offsetAndRotation(-4.5F, 1.3F, 4.2F, CLOAK_3_ROT, 0.0F, 0.0F));
        body.addOrReplaceChild("cloak_at_l", CubeListBuilder.create().texOffs(0, 43).addBox(2.5F, 1.0F, 2.0F, 2, 1, 3), PartPose.rotation(CLOAK_1_ROT, 0.0F, 0.0F));
        body.addOrReplaceChild("cloak_at_r", CubeListBuilder.create().texOffs(0, 43).addBox(-4.5F, 1.0F, 2.0F, 2, 1, 3), PartPose.rotation(CLOAK_1_ROT, 0.0F, 0.0F));
        PartDefinition rightArm = root.getChild("right_arm");
        rightArm.addOrReplaceChild("shoulder_r", CubeListBuilder.create().texOffs(56, 35).addBox(-3.5F, -2.5F, -2.5F, 5, 5, 5), PartPose.ZERO);
        rightArm.addOrReplaceChild("shoulder_r0", CubeListBuilder.create().texOffs(0, 0).addBox(-4.3F, -1.5F, -3.0F, 3, 5, 6), PartPose.rotation(0.0F, 0.0F, QUARTER_PI));
        rightArm.addOrReplaceChild("shoulder_r1", CubeListBuilder.create().texOffs(0, 19).addBox(-3.3F, 3.5F, -2.5F, 1, 1, 5), PartPose.rotation(0.0F, 0.0F, QUARTER_PI));
        rightArm.addOrReplaceChild("shoulder_r2", CubeListBuilder.create().texOffs(0, 11).addBox(-2.3F, 3.5F, -3.0F, 1, 2, 6), PartPose.rotation(0.0F, 0.0F, QUARTER_PI));
        rightArm.addOrReplaceChild("gauntlet_r", CubeListBuilder.create().texOffs(100, 26).addBox(-3.5F, 3.5F, -2.5F, 2, 6, 5), PartPose.ZERO);
        rightArm.addOrReplaceChild("gauntlet_strap_r1", CubeListBuilder.create().texOffs(84, 31).addBox(-1.5F, 3.5F, -2.5F, 3, 1, 5), PartPose.ZERO);
        rightArm.addOrReplaceChild("gauntlet_strap_r2", CubeListBuilder.create().texOffs(84, 31).addBox(-1.5F, 6.5F, -2.5F, 3, 1, 5), PartPose.ZERO);
        PartDefinition leftArm = root.getChild("left_arm");
        leftArm.addOrReplaceChild("shoulder_l", CubeListBuilder.create().texOffs(56, 35).addBox(-1.5F, -2.5F, -2.5F, 5, 5, 5), PartPose.ZERO);
        leftArm.addOrReplaceChild("shoulder_l0", CubeListBuilder.create().texOffs(0, 0).addBox(1.3F, -1.5F, -3.0F, 3, 5, 6), PartPose.rotation(0.0F, 0.0F, -QUARTER_PI));
        leftArm.addOrReplaceChild("shoulder_l1", CubeListBuilder.create().texOffs(0, 19).mirror().addBox(2.3F, 3.5F, -2.5F, 1, 1, 5), PartPose.rotation(0.0F, 0.0F, -QUARTER_PI));
        leftArm.addOrReplaceChild("shoulder_l2", CubeListBuilder.create().texOffs(0, 11).addBox(1.3F, 3.5F, -3.0F, 1, 2, 6), PartPose.rotation(0.0F, 0.0F, -QUARTER_PI));
        leftArm.addOrReplaceChild("gauntlet_l", CubeListBuilder.create().texOffs(114, 26).addBox(1.5F, 3.5F, -2.5F, 2, 6, 5), PartPose.ZERO);
        leftArm.addOrReplaceChild("gauntlet_strap_l1", CubeListBuilder.create().texOffs(84, 31).mirror().addBox(-1.5F, 3.5F, -2.5F, 3, 1, 5), PartPose.ZERO);
        leftArm.addOrReplaceChild("gauntlet_strap_l2", CubeListBuilder.create().texOffs(84, 31).mirror().addBox(-1.5F, 6.5F, -2.5F, 3, 1, 5), PartPose.ZERO);
        return LayerDefinition.create(mesh, TEX_WIDTH, TEX_HEIGHT);
    }

    public static LayerDefinition createLegs() {
        MeshDefinition mesh = emptyMesh();
        PartDefinition root = mesh.getRoot();
        addBelts(root.getChild("body"));
        PartDefinition rightLeg = root.getChild("right_leg");
        rightLeg.addOrReplaceChild("sidepanel_r0", CubeListBuilder.create().texOffs(96, 14).addBox(-3.0F, -0.5F, -2.5F, 5, 3, 5), PartPose.rotation(0.0F, 0.0F, SIDEPANEL_ROT));
        rightLeg.addOrReplaceChild("sidepanel_r1", CubeListBuilder.create().texOffs(96, 7).mirror().addBox(0.0F, 2.5F, -2.5F, 2, 2, 5), PartPose.rotation(0.0F, 0.0F, SIDEPANEL_ROT));
        rightLeg.addOrReplaceChild("sidepanel_r2", CubeListBuilder.create().texOffs(114, 5).mirror().addBox(-2.0F, 2.5F, -2.5F, 2, 3, 5), PartPose.rotation(0.0F, 0.0F, SIDEPANEL_ROT));
        rightLeg.addOrReplaceChild("sidepanel_r3", CubeListBuilder.create().texOffs(116, 13).addBox(-3.0F, 2.5F, -2.5F, 1, 4, 5), PartPose.rotation(0.0F, 0.0F, SIDEPANEL_ROT));
        PartDefinition leftLeg = root.getChild("left_leg");
        leftLeg.addOrReplaceChild("sidepanel_l0", CubeListBuilder.create().texOffs(96, 14).addBox(-2.0F, -0.5F, -2.5F, 5, 3, 5), PartPose.rotation(0.0F, 0.0F, -SIDEPANEL_ROT));
        leftLeg.addOrReplaceChild("sidepanel_l1", CubeListBuilder.create().texOffs(96, 7).addBox(-2.0F, 2.5F, -2.5F, 2, 2, 5), PartPose.rotation(0.0F, 0.0F, -SIDEPANEL_ROT));
        leftLeg.addOrReplaceChild("sidepanel_l2", CubeListBuilder.create().texOffs(114, 5).addBox(0.0F, 2.5F, -2.5F, 2, 3, 5), PartPose.rotation(0.0F, 0.0F, -SIDEPANEL_ROT));
        leftLeg.addOrReplaceChild("sidepanel_l3", CubeListBuilder.create().texOffs(116, 13).addBox(2.0F, 2.5F, -2.5F, 1, 4, 5), PartPose.rotation(0.0F, 0.0F, -SIDEPANEL_ROT));
        return LayerDefinition.create(mesh, TEX_WIDTH, TEX_HEIGHT);
    }

    private static void addBelts(PartDefinition body) {
        body.addOrReplaceChild("mbelt", CubeListBuilder.create().texOffs(56, 55).addBox(-4.0F, 8.0F, -3.0F, 8, 4, 1), PartPose.ZERO);
        body.addOrReplaceChild("mbelt_l", CubeListBuilder.create().texOffs(76, 44).addBox(4.0F, 8.0F, -3.0F, 1, 3, 6), PartPose.ZERO);
        body.addOrReplaceChild("mbelt_r", CubeListBuilder.create().texOffs(76, 44).addBox(-5.0F, 8.0F, -3.0F, 1, 3, 6), PartPose.ZERO);
    }

    static MeshDefinition emptyMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.ZERO);
        head.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);
        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.ZERO);
        emptyChild(body, "frontcloth1");
        emptyChild(body, "frontcloth2");
        emptyChild(body, "cloak1");
        emptyChild(body, "cloak2");
        emptyChild(body, "cloak3");
        root.addOrReplaceChild("right_arm", CubeListBuilder.create(), PartPose.offset(-5.0F, 2.0F, 0.0F));
        root.addOrReplaceChild("left_arm", CubeListBuilder.create(), PartPose.offset(5.0F, 2.0F, 0.0F));
        root.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.offset(-1.9F, 12.0F, 0.0F));
        root.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.offset(1.9F, 12.0F, 0.0F));
        return mesh;
    }

    static PartDefinition emptyChild(PartDefinition parent, String name) {
        return parent.addOrReplaceChild(name, CubeListBuilder.create(), PartPose.ZERO);
    }

    @Override
    public void setupAnim(HumanoidRenderState state) {
        super.setupAnim(state);
        float a = Mth.cos(state.walkAnimationPos * 0.6662F) * 1.4F * state.walkAnimationSpeed;
        float b = Mth.cos(state.walkAnimationPos * 0.6662F + (float) Math.PI) * 1.4F * state.walkAnimationSpeed;
        float c = Math.min(a, b);
        this.frontcloth1.xRot = c + FRONTCLOTH_1_ROT;
        this.frontcloth2.xRot = c + FRONTCLOTH_2_ROT;
        this.cloak1.xRot = -c / 2.0F + CLOAK_1_ROT;
        this.cloak2.xRot = -c / 2.0F + CLOAK_2_ROT;
        this.cloak3.xRot = -c / 2.0F + CLOAK_3_ROT;
    }
}
