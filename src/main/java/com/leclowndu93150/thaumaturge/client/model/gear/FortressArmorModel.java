package com.leclowndu93150.thaumaturge.client.model.gear;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public final class FortressArmorModel extends AbstractTCArmorModel {
    private static final int TEX_WIDTH = 128;
    private static final int TEX_HEIGHT = 64;
    private static final float SIXTH_PI = (float) (Math.PI / 6);
    private static final float ORNAMENT_ROT = -0.1396263F;
    private static final float SHOULDERPLATE_ROT = 0.4363323F;
    private static final float LEGPANEL_ROT = -0.4363323F;
    private static final float BACKPANEL_ROT = 0.4363323F;

    private final ModelPart mask0;
    private final ModelPart mask1;
    private final ModelPart mask2;
    private final ModelPart goggles;

    public FortressArmorModel(ModelPart root) {
        super(root);
        this.mask0 = this.head.getChild("mask_0");
        this.mask1 = this.head.getChild("mask_1");
        this.mask2 = this.head.getChild("mask_2");
        this.goggles = this.head.getChild("goggles");
        this.setMask(-1);
        this.goggles.visible = false;
    }

    public void setMask(int mask) {
        this.mask0.visible = mask == 0;
        this.mask1.visible = mask == 1;
        this.mask2.visible = mask == 2;
    }

    public void setGogglesVisible(boolean visible) {
        this.goggles.visible = visible;
    }

    public static LayerDefinition createHead() {
        MeshDefinition mesh = KnightArmorModel.emptyMesh();
        PartDefinition head = mesh.getRoot().getChild("head");
        addMasks(head);
        head.addOrReplaceChild("goggles", CubeListBuilder.create().texOffs(100, 18).addBox(-4.5F, -5.0F, -4.25F, 9, 5, 1), PartPose.ZERO);
        head.addOrReplaceChild("ornament_l", CubeListBuilder.create().texOffs(78, 8).mirror().addBox(1.5F, -9.0F, -6.5F, 2, 2, 1), PartPose.rotation(ORNAMENT_ROT, 0.0F, 0.0F));
        head.addOrReplaceChild("ornament_l2", CubeListBuilder.create().texOffs(78, 8).mirror().addBox(3.5F, -10.0F, -6.5F, 1, 2, 1), PartPose.rotation(ORNAMENT_ROT, 0.0F, 0.0F));
        head.addOrReplaceChild("ornament_r", CubeListBuilder.create().texOffs(78, 8).addBox(-3.5F, -9.0F, -6.5F, 2, 2, 1), PartPose.rotation(ORNAMENT_ROT, 0.0F, 0.0F));
        head.addOrReplaceChild("ornament_r2", CubeListBuilder.create().texOffs(78, 8).addBox(-4.5F, -10.0F, -6.5F, 1, 2, 1), PartPose.rotation(ORNAMENT_ROT, 0.0F, 0.0F));
        head.addOrReplaceChild("helmet", CubeListBuilder.create().texOffs(41, 8).addBox(-4.5F, -9.0F, -4.5F, 9, 4, 9), PartPose.ZERO);
        head.addOrReplaceChild("helmet_r", CubeListBuilder.create().texOffs(21, 13).addBox(-6.5F, -3.0F, -4.5F, 1, 5, 9), PartPose.rotation(0.0F, 0.0F, SIXTH_PI));
        head.addOrReplaceChild("helmet_l", CubeListBuilder.create().texOffs(21, 13).mirror().addBox(5.5F, -3.0F, -4.5F, 1, 5, 9), PartPose.rotation(0.0F, 0.0F, -SIXTH_PI));
        head.addOrReplaceChild("helmet_b", CubeListBuilder.create().texOffs(41, 21).addBox(-4.5F, -3.0F, 5.5F, 9, 5, 1), PartPose.rotation(SIXTH_PI, 0.0F, 0.0F));
        head.addOrReplaceChild("capsthingy", CubeListBuilder.create().texOffs(21, 0).addBox(-4.5F, -6.0F, -6.5F, 9, 1, 2), PartPose.ZERO);
        head.addOrReplaceChild("flap_r", CubeListBuilder.create().texOffs(59, 10).addBox(-10.0F, -2.0F, -1.0F, 3, 3, 1), PartPose.rotation(0.0F, -SIXTH_PI, SIXTH_PI));
        head.addOrReplaceChild("flap_l", CubeListBuilder.create().texOffs(59, 10).mirror().addBox(7.0F, -2.0F, -1.0F, 3, 3, 1), PartPose.rotation(0.0F, SIXTH_PI, -SIXTH_PI));
        head.addOrReplaceChild("gemornament", CubeListBuilder.create().texOffs(68, 11).addBox(-1.5F, -9.0F, -7.0F, 3, 3, 2), PartPose.rotation(ORNAMENT_ROT, 0.0F, 0.0F));
        head.addOrReplaceChild("gem", CubeListBuilder.create().texOffs(72, 8).addBox(-1.0F, -8.5F, -7.5F, 2, 2, 1), PartPose.rotation(ORNAMENT_ROT, 0.0F, 0.0F));
        return LayerDefinition.create(mesh, TEX_WIDTH, TEX_HEIGHT);
    }

    public static LayerDefinition createChest() {
        MeshDefinition mesh = KnightArmorModel.emptyMesh();
        PartDefinition root = mesh.getRoot();
        addMasks(root.getChild("head"));
        root.getChild("head").addOrReplaceChild("goggles", CubeListBuilder.create(), PartPose.ZERO);
        PartDefinition body = root.getChild("body");
        body.addOrReplaceChild("belt_r", CubeListBuilder.create().texOffs(76, 44).addBox(-5.0F, 4.0F, -3.0F, 1, 3, 6), PartPose.ZERO);
        body.addOrReplaceChild("mbelt", CubeListBuilder.create().texOffs(56, 55).addBox(-4.0F, 8.0F, -3.0F, 8, 4, 1), PartPose.ZERO);
        body.addOrReplaceChild("mbelt_l", CubeListBuilder.create().texOffs(76, 44).addBox(4.0F, 8.0F, -3.0F, 1, 3, 6), PartPose.ZERO);
        body.addOrReplaceChild("mbelt_r", CubeListBuilder.create().texOffs(76, 44).addBox(-5.0F, 8.0F, -3.0F, 1, 3, 6), PartPose.ZERO);
        body.addOrReplaceChild("belt_l", CubeListBuilder.create().texOffs(76, 44).addBox(4.0F, 4.0F, -3.0F, 1, 3, 6), PartPose.ZERO);
        body.addOrReplaceChild("chestplate", CubeListBuilder.create().texOffs(56, 45).addBox(-4.0F, 1.0F, -4.0F, 8, 7, 2), PartPose.ZERO);
        body.addOrReplaceChild("scroll", CubeListBuilder.create().texOffs(34, 27).addBox(-2.0F, 9.5F, 4.0F, 8, 3, 3), PartPose.rotation(0.0F, 0.0F, 0.1919862F));
        body.addOrReplaceChild("backplate", CubeListBuilder.create().texOffs(36, 45).addBox(-4.0F, 1.0F, 2.0F, 8, 11, 2), PartPose.ZERO);
        body.addOrReplaceChild("book", CubeListBuilder.create().texOffs(100, 8).addBox(1.0F, -0.3F, 4.0F, 5, 7, 2), PartPose.rotation(0.0F, 0.0F, 0.7679449F));
        PartDefinition rightArm = root.getChild("right_arm");
        rightArm.addOrReplaceChild("shoulder_r", CubeListBuilder.create().texOffs(56, 35).addBox(-3.5F, -2.5F, -2.5F, 5, 5, 5), PartPose.ZERO);
        rightArm.addOrReplaceChild("gauntlet_r", CubeListBuilder.create().texOffs(100, 26).addBox(-3.5F, 3.5F, -2.5F, 2, 6, 5), PartPose.ZERO);
        rightArm.addOrReplaceChild("gauntlet_strap_r1", CubeListBuilder.create().texOffs(84, 31).addBox(-1.5F, 3.5F, -2.5F, 3, 1, 5), PartPose.ZERO);
        rightArm.addOrReplaceChild("gauntlet_strap_r2", CubeListBuilder.create().texOffs(84, 31).addBox(-1.5F, 6.5F, -2.5F, 3, 1, 5), PartPose.ZERO);
        rightArm.addOrReplaceChild("shoulderplate_top_r", CubeListBuilder.create().texOffs(110, 37).addBox(-5.5F, -2.5F, -3.5F, 2, 1, 7), PartPose.rotation(0.0F, 0.0F, SHOULDERPLATE_ROT));
        rightArm.addOrReplaceChild("shoulderplate_r1", CubeListBuilder.create().texOffs(110, 45).addBox(-4.5F, -1.5F, -3.5F, 1, 4, 7), PartPose.rotation(0.0F, 0.0F, SHOULDERPLATE_ROT));
        rightArm.addOrReplaceChild("shoulderplate_r2", CubeListBuilder.create().texOffs(94, 45).addBox(-3.5F, 1.5F, -3.5F, 1, 3, 7), PartPose.rotation(0.0F, 0.0F, SHOULDERPLATE_ROT));
        rightArm.addOrReplaceChild("shoulderplate_r3", CubeListBuilder.create().texOffs(94, 45).addBox(-2.5F, 3.5F, -3.5F, 1, 3, 7), PartPose.rotation(0.0F, 0.0F, SHOULDERPLATE_ROT));
        PartDefinition leftArm = root.getChild("left_arm");
        leftArm.addOrReplaceChild("shoulder_l", CubeListBuilder.create().texOffs(56, 35).mirror().addBox(-1.5F, -2.5F, -2.5F, 5, 5, 5), PartPose.ZERO);
        leftArm.addOrReplaceChild("gauntlet_l", CubeListBuilder.create().texOffs(114, 26).addBox(1.5F, 3.5F, -2.5F, 2, 6, 5), PartPose.ZERO);
        leftArm.addOrReplaceChild("gauntlet_strap_l1", CubeListBuilder.create().texOffs(84, 31).mirror().addBox(-1.5F, 3.5F, -2.5F, 3, 1, 5), PartPose.ZERO);
        leftArm.addOrReplaceChild("gauntlet_strap_l2", CubeListBuilder.create().texOffs(84, 31).mirror().addBox(-1.5F, 6.5F, -2.5F, 3, 1, 5), PartPose.ZERO);
        leftArm.addOrReplaceChild("shoulderplate_top_l", CubeListBuilder.create().texOffs(110, 37).mirror().addBox(3.5F, -2.5F, -3.5F, 2, 1, 7), PartPose.rotation(0.0F, 0.0F, -SHOULDERPLATE_ROT));
        leftArm.addOrReplaceChild("shoulderplate_l1", CubeListBuilder.create().texOffs(110, 45).mirror().addBox(3.5F, -1.5F, -3.5F, 1, 4, 7), PartPose.rotation(0.0F, 0.0F, -SHOULDERPLATE_ROT));
        leftArm.addOrReplaceChild("shoulderplate_l2", CubeListBuilder.create().texOffs(94, 45).mirror().addBox(2.5F, 1.5F, -3.5F, 1, 3, 7), PartPose.rotation(0.0F, 0.0F, -SHOULDERPLATE_ROT));
        leftArm.addOrReplaceChild("shoulderplate_l3", CubeListBuilder.create().texOffs(94, 45).mirror().addBox(1.5F, 3.5F, -3.5F, 1, 3, 7), PartPose.rotation(0.0F, 0.0F, -SHOULDERPLATE_ROT));
        return LayerDefinition.create(mesh, TEX_WIDTH, TEX_HEIGHT);
    }

    public static LayerDefinition createLegs() {
        MeshDefinition mesh = KnightArmorModel.emptyMesh();
        PartDefinition root = mesh.getRoot();
        addMasks(root.getChild("head"));
        root.getChild("head").addOrReplaceChild("goggles", CubeListBuilder.create(), PartPose.ZERO);
        PartDefinition rightLeg = root.getChild("right_leg");
        rightLeg.addOrReplaceChild("legpanel_r1", CubeListBuilder.create().texOffs(0, 51).addBox(-1.0F, 0.5F, -3.5F, 3, 4, 1), PartPose.rotation(LEGPANEL_ROT, 0.0F, 0.0F));
        rightLeg.addOrReplaceChild("legpanel_r2", CubeListBuilder.create().texOffs(8, 51).addBox(-1.0F, 3.5F, -2.5F, 3, 4, 1), PartPose.rotation(LEGPANEL_ROT, 0.0F, 0.0F));
        rightLeg.addOrReplaceChild("legpanel_r3", CubeListBuilder.create().texOffs(0, 56).addBox(-1.0F, 6.5F, -1.5F, 3, 3, 1), PartPose.rotation(LEGPANEL_ROT, 0.0F, 0.0F));
        rightLeg.addOrReplaceChild("legpanel_r4", CubeListBuilder.create().texOffs(0, 43).addBox(-3.0F, 0.5F, -3.5F, 2, 3, 1), PartPose.rotation(LEGPANEL_ROT, 0.0F, 0.0F));
        rightLeg.addOrReplaceChild("legpanel_r5", CubeListBuilder.create().texOffs(0, 47).addBox(-3.0F, 2.5F, -2.5F, 2, 3, 1), PartPose.rotation(LEGPANEL_ROT, 0.0F, 0.0F));
        rightLeg.addOrReplaceChild("legpanel_r6", CubeListBuilder.create().texOffs(6, 43).addBox(-3.0F, 4.5F, -1.5F, 2, 3, 1), PartPose.rotation(LEGPANEL_ROT, 0.0F, 0.0F));
        rightLeg.addOrReplaceChild("sidepanel_r1", CubeListBuilder.create().texOffs(0, 22).addBox(-2.5F, 0.5F, -2.5F, 1, 4, 5), PartPose.rotation(0.0F, 0.0F, SHOULDERPLATE_ROT));
        rightLeg.addOrReplaceChild("sidepanel_r2", CubeListBuilder.create().texOffs(0, 31).addBox(-1.5F, 3.5F, -2.5F, 1, 3, 5), PartPose.rotation(0.0F, 0.0F, SHOULDERPLATE_ROT));
        rightLeg.addOrReplaceChild("sidepanel_r3", CubeListBuilder.create().texOffs(12, 31).addBox(-0.5F, 5.5F, -2.5F, 1, 3, 5), PartPose.rotation(0.0F, 0.0F, SHOULDERPLATE_ROT));
        rightLeg.addOrReplaceChild("backpanel_r1", CubeListBuilder.create().texOffs(0, 18).addBox(-3.0F, 0.5F, 2.5F, 5, 3, 1), PartPose.rotation(BACKPANEL_ROT, 0.0F, 0.0F));
        rightLeg.addOrReplaceChild("backpanel_r2", CubeListBuilder.create().texOffs(0, 18).addBox(-3.0F, 2.5F, 1.5F, 5, 3, 1), PartPose.rotation(BACKPANEL_ROT, 0.0F, 0.0F));
        rightLeg.addOrReplaceChild("backpanel_r3", CubeListBuilder.create().texOffs(0, 18).addBox(-3.0F, 4.5F, 0.5F, 5, 3, 1), PartPose.rotation(BACKPANEL_ROT, 0.0F, 0.0F));
        PartDefinition leftLeg = root.getChild("left_leg");
        leftLeg.addOrReplaceChild("backpanel_l3", CubeListBuilder.create().texOffs(0, 18).mirror().addBox(-2.0F, 4.5F, 0.5F, 5, 3, 1), PartPose.rotation(BACKPANEL_ROT, 0.0F, 0.0F));
        leftLeg.addOrReplaceChild("legpanel_l1", CubeListBuilder.create().texOffs(0, 51).mirror().addBox(-2.0F, 0.5F, -3.5F, 3, 4, 1), PartPose.rotation(LEGPANEL_ROT, 0.0F, 0.0F));
        leftLeg.addOrReplaceChild("legpanel_l2", CubeListBuilder.create().texOffs(8, 51).mirror().addBox(-2.0F, 3.5F, -2.5F, 3, 4, 1), PartPose.rotation(LEGPANEL_ROT, 0.0F, 0.0F));
        leftLeg.addOrReplaceChild("legpanel_l3", CubeListBuilder.create().texOffs(0, 56).mirror().addBox(-2.0F, 6.5F, -1.5F, 3, 3, 1), PartPose.rotation(LEGPANEL_ROT, 0.0F, 0.0F));
        leftLeg.addOrReplaceChild("legpanel_l4", CubeListBuilder.create().texOffs(0, 43).mirror().addBox(1.0F, 0.5F, -3.5F, 2, 3, 1), PartPose.rotation(LEGPANEL_ROT, 0.0F, 0.0F));
        leftLeg.addOrReplaceChild("legpanel_l5", CubeListBuilder.create().texOffs(0, 47).mirror().addBox(1.0F, 2.5F, -2.5F, 2, 3, 1), PartPose.rotation(LEGPANEL_ROT, 0.0F, 0.0F));
        leftLeg.addOrReplaceChild("legpanel_l6", CubeListBuilder.create().texOffs(6, 43).mirror().addBox(1.0F, 4.5F, -1.5F, 2, 3, 1), PartPose.rotation(LEGPANEL_ROT, 0.0F, 0.0F));
        leftLeg.addOrReplaceChild("sidepanel_l1", CubeListBuilder.create().texOffs(0, 22).mirror().addBox(1.5F, 0.5F, -2.5F, 1, 4, 5), PartPose.rotation(0.0F, 0.0F, -SHOULDERPLATE_ROT));
        leftLeg.addOrReplaceChild("sidepanel_l2", CubeListBuilder.create().texOffs(0, 31).mirror().addBox(0.5F, 3.5F, -2.5F, 1, 3, 5), PartPose.rotation(0.0F, 0.0F, -SHOULDERPLATE_ROT));
        leftLeg.addOrReplaceChild("sidepanel_l3", CubeListBuilder.create().texOffs(12, 31).mirror().addBox(-0.5F, 5.5F, -2.5F, 1, 3, 5), PartPose.rotation(0.0F, 0.0F, -SHOULDERPLATE_ROT));
        leftLeg.addOrReplaceChild("backpanel_l1", CubeListBuilder.create().texOffs(0, 18).mirror().addBox(-2.0F, 0.5F, 2.5F, 5, 3, 1), PartPose.rotation(BACKPANEL_ROT, 0.0F, 0.0F));
        leftLeg.addOrReplaceChild("backpanel_l2", CubeListBuilder.create().texOffs(0, 18).mirror().addBox(-2.0F, 2.5F, 1.5F, 5, 3, 1), PartPose.rotation(BACKPANEL_ROT, 0.0F, 0.0F));
        return LayerDefinition.create(mesh, TEX_WIDTH, TEX_HEIGHT);
    }

    private static void addMasks(PartDefinition head) {
        for (int i = 0; i < 3; i++) {
            head.addOrReplaceChild("mask_" + i, CubeListBuilder.create().texOffs(52 + i * 24, 2).addBox(-4.5F, -5.0F, -4.6F, 9, 5, 1), PartPose.ZERO);
        }
    }
}
