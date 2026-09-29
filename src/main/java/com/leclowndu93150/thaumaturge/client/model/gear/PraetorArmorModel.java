package com.leclowndu93150.thaumaturge.client.model.gear;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

public final class PraetorArmorModel extends AbstractTCArmorModel {
    private final ModelPart legClothR;
    private final ModelPart legClothL;
    private final ModelPart cloak1;
    private final ModelPart cloak2;
    private final ModelPart cloak3;

    public PraetorArmorModel(ModelPart root) {
        super(root);
        this.legClothR = this.body.getChild("LegClothR");
        this.legClothL = this.body.getChild("LegClothL");
        this.cloak1 = this.body.getChild("Cloak1");
        this.cloak2 = this.body.getChild("Cloak2");
        this.cloak3 = this.body.getChild("Cloak3");
    }

    public static LayerDefinition createHead() {
        MeshDefinition mesh = emptyMesh();
        PartDefinition root = mesh.getRoot();
        root.getChild("head")
                .addOrReplaceChild(
                        "Helmet",
                        CubeListBuilder.create().texOffs(41, 8).addBox(-4.5F, -9.0F, -4.5F, 9, 9, 9),
                        PartPose.ZERO);
        return LayerDefinition.create(mesh, 128, 64);
    }

    public static LayerDefinition createChest() {
        MeshDefinition mesh = emptyMesh();
        PartDefinition root = mesh.getRoot();
        root.getChild("body")
                .addOrReplaceChild(
                        "CollarF",
                        CubeListBuilder.create().texOffs(17, 31).addBox(-4.5F, -1.5F, -3.0F, 9, 4, 1),
                        PartPose.offsetAndRotation(0.0F, 0.0F, -2.5F, 0.2268928F, 0.0F, 0.0F));
        root.getChild("body")
                .addOrReplaceChild(
                        "CollarB",
                        CubeListBuilder.create().texOffs(17, 26).addBox(-4.5F, -1.5F, 7.0F, 9, 4, 1),
                        PartPose.offsetAndRotation(0.0F, 0.0F, -2.5F, 0.2268928F, 0.0F, 0.0F));
        root.getChild("body")
                .addOrReplaceChild(
                        "CollarR",
                        CubeListBuilder.create().texOffs(17, 11).addBox(-5.5F, -1.5F, -3.0F, 1, 4, 11),
                        PartPose.offsetAndRotation(0.0F, 0.0F, -2.5F, 0.2268928F, 0.0F, 0.0F));
        root.getChild("body")
                .addOrReplaceChild(
                        "CollarL",
                        CubeListBuilder.create().texOffs(17, 11).addBox(4.5F, -1.5F, -3.0F, 1, 4, 11),
                        PartPose.offsetAndRotation(0.0F, 0.0F, -2.5F, 0.2268928F, 0.0F, 0.0F));
        root.getChild("body")
                .addOrReplaceChild(
                        "BeltR",
                        CubeListBuilder.create().texOffs(76, 44).addBox(-5.0F, 4.0F, -3.0F, 1, 3, 6),
                        PartPose.ZERO);
        root.getChild("body")
                .addOrReplaceChild(
                        "BeltL",
                        CubeListBuilder.create().texOffs(76, 44).addBox(4.0F, 4.0F, -3.0F, 1, 3, 6),
                        PartPose.ZERO);
        root.getChild("body")
                .addOrReplaceChild(
                        "CloakTL",
                        CubeListBuilder.create().texOffs(0, 43).addBox(2.5F, 1.0F, -1.0F, 2, 1, 3),
                        PartPose.offsetAndRotation(0.0F, 0.0F, 3.0F, 0.1396263F, 0.0F, 0.0F));
        root.getChild("body")
                .addOrReplaceChild(
                        "Cloak3",
                        CubeListBuilder.create().texOffs(0, 59).addBox(-4.5F, 17.0F, -3.7F, 9, 4, 1),
                        PartPose.offsetAndRotation(0.0F, 0.0F, 3.0F, 0.4465716F, 0.0F, 0.0F));
        root.getChild("body")
                .addOrReplaceChild(
                        "CloakTR",
                        CubeListBuilder.create().texOffs(0, 43).addBox(-4.5F, 1.0F, -1.0F, 2, 1, 3),
                        PartPose.offsetAndRotation(0.0F, 0.0F, 3.0F, 0.1396263F, 0.0F, 0.0F));
        root.getChild("body")
                .addOrReplaceChild(
                        "Cloak1",
                        CubeListBuilder.create().texOffs(0, 47).addBox(-4.5F, 2.0F, 1.0F, 9, 12, 1),
                        PartPose.offsetAndRotation(0.0F, 0.0F, 3.0F, 0.1396263F, 0.0F, 0.0F));
        root.getChild("body")
                .addOrReplaceChild(
                        "Cloak2",
                        CubeListBuilder.create().texOffs(0, 59).addBox(-4.5F, 14.0F, -1.3F, 9, 4, 1),
                        PartPose.offsetAndRotation(0.0F, 0.0F, 3.0F, 0.3069452F, 0.0F, 0.0F));
        root.getChild("body")
                .addOrReplaceChild(
                        "Chestplate",
                        CubeListBuilder.create().texOffs(56, 45).addBox(-4.0F, 1.0F, -3.8F, 8, 7, 2),
                        PartPose.ZERO);
        root.getChild("body")
                .addOrReplaceChild(
                        "ChestOrnament",
                        CubeListBuilder.create().texOffs(76, 53).addBox(-2.5F, 3.0F, -4.8F, 5, 5, 1),
                        PartPose.ZERO);
        root.getChild("body")
                .addOrReplaceChild(
                        "ChestClothL",
                        CubeListBuilder.create().texOffs(20, 47).mirror().addBox(1.5F, 1.2F, -4.5F, 3, 9, 1),
                        PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0663225F, 0.0F, 0.0F));
        root.getChild("body")
                .addOrReplaceChild(
                        "ChestClothR",
                        CubeListBuilder.create().texOffs(20, 47).addBox(-4.5F, 1.2F, -4.5F, 3, 9, 1),
                        PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0663225F, 0.0F, 0.0F));
        root.getChild("body")
                .addOrReplaceChild(
                        "Backplate",
                        CubeListBuilder.create().texOffs(36, 45).addBox(-4.0F, 1.0F, 2.0F, 8, 11, 2),
                        PartPose.ZERO);
        root.getChild("right_arm")
                .addOrReplaceChild(
                        "GauntletR",
                        CubeListBuilder.create().texOffs(100, 26).addBox(-3.5F, 3.5F, -2.5F, 2, 6, 5),
                        PartPose.ZERO);
        root.getChild("left_arm")
                .addOrReplaceChild(
                        "GauntletL",
                        CubeListBuilder.create().texOffs(114, 26).addBox(1.5F, 3.5F, -2.5F, 2, 6, 5),
                        PartPose.ZERO);
        root.getChild("left_arm")
                .addOrReplaceChild(
                        "GauntletstrapL1",
                        CubeListBuilder.create().texOffs(84, 31).mirror().addBox(-1.5F, 3.5F, -2.5F, 3, 1, 5),
                        PartPose.ZERO);
        root.getChild("left_arm")
                .addOrReplaceChild(
                        "GauntletstrapL2",
                        CubeListBuilder.create().texOffs(84, 31).mirror().addBox(-1.5F, 6.5F, -2.5F, 3, 1, 5),
                        PartPose.ZERO);
        root.getChild("right_arm")
                .addOrReplaceChild(
                        "GauntletstrapR1",
                        CubeListBuilder.create().texOffs(84, 31).addBox(-1.5F, 3.5F, -2.5F, 3, 1, 5),
                        PartPose.ZERO);
        root.getChild("right_arm")
                .addOrReplaceChild(
                        "GauntletstrapR2",
                        CubeListBuilder.create().texOffs(84, 31).addBox(-1.5F, 6.5F, -2.5F, 3, 1, 5),
                        PartPose.ZERO);
        root.getChild("right_arm")
                .addOrReplaceChild(
                        "GauntletR2",
                        CubeListBuilder.create().texOffs(102, 37).addBox(-5.0F, 3.5F, -2.0F, 1, 5, 4),
                        PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.1675516F));
        root.getChild("left_arm")
                .addOrReplaceChild(
                        "GauntletL2",
                        CubeListBuilder.create().texOffs(102, 37).addBox(4.0F, 3.5F, -2.0F, 1, 5, 4),
                        PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.1675516F));
        root.getChild("right_arm")
                .addOrReplaceChild(
                        "ShoulderR",
                        CubeListBuilder.create().texOffs(56, 35).addBox(-3.5F, -2.5F, -2.5F, 5, 5, 5),
                        PartPose.ZERO);
        root.getChild("right_arm")
                .addOrReplaceChild(
                        "ShoulderR1",
                        CubeListBuilder.create().texOffs(0, 0).addBox(-4.3F, -1.5F, -3.0F, 3, 5, 6),
                        PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.7853982F));
        root.getChild("right_arm")
                .addOrReplaceChild(
                        "ShoulderR2",
                        CubeListBuilder.create().texOffs(0, 19).addBox(-3.3F, 3.5F, -2.5F, 1, 1, 5),
                        PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.7853982F));
        root.getChild("right_arm")
                .addOrReplaceChild(
                        "ShoulderR5",
                        CubeListBuilder.create().texOffs(18, 4).addBox(-2.3F, -1.5F, 3.0F, 1, 6, 1),
                        PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.7853982F));
        root.getChild("right_arm")
                .addOrReplaceChild(
                        "ShoulderR3",
                        CubeListBuilder.create().texOffs(0, 11).addBox(-2.3F, 3.5F, -3.0F, 1, 2, 6),
                        PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.7853982F));
        root.getChild("right_arm")
                .addOrReplaceChild(
                        "ShoulderR4",
                        CubeListBuilder.create().texOffs(18, 4).addBox(-2.3F, -1.5F, -4.0F, 1, 6, 1),
                        PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.7853982F));
        root.getChild("left_arm")
                .addOrReplaceChild(
                        "ShoulderL",
                        CubeListBuilder.create().texOffs(56, 35).addBox(-1.5F, -2.5F, -2.5F, 5, 5, 5),
                        PartPose.ZERO);
        root.getChild("left_arm")
                .addOrReplaceChild(
                        "ShoulderL1",
                        CubeListBuilder.create().texOffs(0, 0).addBox(1.3F, -1.5F, -3.0F, 3, 5, 6),
                        PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.7853982F));
        root.getChild("left_arm")
                .addOrReplaceChild(
                        "ShoulderL2",
                        CubeListBuilder.create().texOffs(0, 19).mirror().addBox(2.3F, 3.5F, -2.5F, 1, 1, 5),
                        PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.7853982F));
        root.getChild("left_arm")
                .addOrReplaceChild(
                        "ShoulderL3",
                        CubeListBuilder.create().texOffs(0, 11).addBox(1.3F, 3.5F, -3.0F, 1, 2, 6),
                        PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.7853982F));
        root.getChild("left_arm")
                .addOrReplaceChild(
                        "ShoulderL5",
                        CubeListBuilder.create().texOffs(18, 4).addBox(1.3F, -1.5F, 3.0F, 1, 6, 1),
                        PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.7853982F));
        root.getChild("left_arm")
                .addOrReplaceChild(
                        "ShoulderL4",
                        CubeListBuilder.create().texOffs(18, 4).addBox(1.3F, -1.5F, -4.0F, 1, 6, 1),
                        PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.7853982F));
        root.getChild("body")
                .addOrReplaceChild(
                        "LegClothR",
                        CubeListBuilder.create().texOffs(20, 55).addBox(0.0F, 0.0F, 0.0F, 3, 8, 1),
                        PartPose.offsetAndRotation(-4.5F, 10.4F, -3.9F, -0.0349066F, 0.0F, 0.0F));
        root.getChild("body")
                .addOrReplaceChild(
                        "LegClothL",
                        CubeListBuilder.create().texOffs(20, 55).mirror().addBox(0.0F, 0.0F, 0.0F, 3, 8, 1),
                        PartPose.offsetAndRotation(1.5F, 10.4F, -3.9F, -0.0349066F, 0.0F, 0.0F));
        return LayerDefinition.create(mesh, 128, 64);
    }

    public static LayerDefinition createLegs() {
        MeshDefinition mesh = emptyMesh();
        PartDefinition root = mesh.getRoot();
        root.getChild("body")
                .addOrReplaceChild(
                        "Mbelt",
                        CubeListBuilder.create().texOffs(56, 55).addBox(-4.0F, 8.0F, -3.0F, 8, 4, 1),
                        PartPose.ZERO);
        root.getChild("body")
                .addOrReplaceChild(
                        "MbeltL",
                        CubeListBuilder.create().texOffs(76, 44).addBox(4.0F, 8.0F, -3.0F, 1, 3, 6),
                        PartPose.ZERO);
        root.getChild("body")
                .addOrReplaceChild(
                        "MbeltR",
                        CubeListBuilder.create().texOffs(76, 44).addBox(-5.0F, 8.0F, -3.0F, 1, 3, 6),
                        PartPose.ZERO);
        root.getChild("right_leg")
                .addOrReplaceChild(
                        "BackpanelR1",
                        CubeListBuilder.create().texOffs(0, 25).addBox(-3.0F, -0.5F, 2.5F, 5, 7, 1),
                        PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0698132F, 0.0F, 0.0F));
        root.getChild("right_leg")
                .addOrReplaceChild(
                        "BackpanelR2",
                        CubeListBuilder.create().texOffs(96, 14).addBox(-3.0F, -0.5F, -2.5F, 5, 3, 5),
                        PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.1396263F));
        root.getChild("right_leg")
                .addOrReplaceChild(
                        "BackpanelR3",
                        CubeListBuilder.create().texOffs(116, 13).addBox(-3.0F, 2.5F, -2.5F, 1, 4, 5),
                        PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.1396263F));
        root.getChild("right_leg")
                .addOrReplaceChild(
                        "BackpanelR4",
                        CubeListBuilder.create().texOffs(0, 25).mirror().addBox(-3.0F, -0.5F, -3.5F, 5, 7, 1),
                        PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.0349066F, 0.0F, 0.0F));
        root.getChild("left_leg")
                .addOrReplaceChild(
                        "BackpanelL1",
                        CubeListBuilder.create().texOffs(0, 25).addBox(-2.0F, -0.5F, 2.5F, 5, 7, 1),
                        PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0698132F, 0.0F, 0.0F));
        root.getChild("left_leg")
                .addOrReplaceChild(
                        "BackpanelL4",
                        CubeListBuilder.create().texOffs(0, 25).addBox(-2.0F, -0.5F, -3.5F, 5, 7, 1),
                        PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.0349066F, 0.0F, 0.0F));
        root.getChild("left_leg")
                .addOrReplaceChild(
                        "BackpanelL2",
                        CubeListBuilder.create().texOffs(96, 14).addBox(-2.0F, -0.5F, -2.5F, 5, 3, 5),
                        PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.1396263F));
        root.getChild("left_leg")
                .addOrReplaceChild(
                        "BackpanelL3",
                        CubeListBuilder.create().texOffs(116, 13).addBox(2.0F, 2.5F, -2.5F, 1, 4, 5),
                        PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.1396263F));
        return LayerDefinition.create(mesh, 128, 64);
    }

    private static MeshDefinition emptyMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);
        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.ZERO);
        body.addOrReplaceChild("LegClothR", CubeListBuilder.create(), PartPose.ZERO);
        body.addOrReplaceChild("LegClothL", CubeListBuilder.create(), PartPose.ZERO);
        body.addOrReplaceChild("Cloak1", CubeListBuilder.create(), PartPose.ZERO);
        body.addOrReplaceChild("Cloak2", CubeListBuilder.create(), PartPose.ZERO);
        body.addOrReplaceChild("Cloak3", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("right_arm", CubeListBuilder.create(), PartPose.offset(-5.0F, 2.0F, 0.0F));
        root.addOrReplaceChild("left_arm", CubeListBuilder.create(), PartPose.offset(5.0F, 2.0F, 0.0F));
        root.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.offset(-1.9F, 12.0F, 0.0F));
        root.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.offset(1.9F, 12.0F, 0.0F));
        return mesh;
    }

    @Override
    public void setupAnim(
            LivingEntity entity,
            float limbSwing,
            float limbSwingAmount,
            float ageInTicks,
            float netHeadYaw,
            float headPitch) {
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        float a = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
        float b = Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * 1.4F * limbSwingAmount;
        float c = Math.min(a, b);
        this.legClothR.xRot = a - 0.1047198F;
        this.legClothL.xRot = b - 0.1047198F;
        this.cloak1.xRot = -c / 2.0F + 0.1396263F;
        this.cloak2.xRot = -c / 2.0F + 0.3069452F;
        this.cloak3.xRot = -c / 2.0F + 0.4465716F;
    }
}
