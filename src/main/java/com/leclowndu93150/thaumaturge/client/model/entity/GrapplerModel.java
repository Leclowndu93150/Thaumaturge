package com.leclowndu93150.thaumaturge.client.model.entity;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

public final class GrapplerModel {
    private static final int TEXTURE_WIDTH = 64;
    private static final int TEXTURE_HEIGHT = 32;
    private static final int TINES = 4;
    private static final float CROWN_FRONT = 2.0F;
    private static final float ROOT_BEND = 30.0F * Mth.DEG_TO_RAD;
    private static final float MID_BEND = 45.0F * Mth.DEG_TO_RAD;
    private static final float TIP_BEND = 55.0F * Mth.DEG_TO_RAD;

    public final ModelPart root;

    public GrapplerModel(ModelPart root) {
        this.root = root;
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("shank", CubeListBuilder.create().texOffs(28, 0).addBox(-4.0F, -1.0F, -1.0F, 5.0F, 2.0F, 2.0F).texOffs(12, 0).addBox(-2.0F, -1.5F, -1.5F, 1.0F, 3.0F, 3.0F),
                PartPose.ZERO);
        root.addOrReplaceChild("eye", CubeListBuilder.create().texOffs(20, 0).addBox(-4.0F, -2.0F, -0.5F, 1.0F, 4.0F, 1.0F).texOffs(56, 0).addBox(-3.0F, 1.0F, -0.5F, 2.0F, 1.0F, 1.0F).texOffs(56, 0)
                .addBox(-3.0F, -2.0F, -0.5F, 2.0F, 1.0F, 1.0F).texOffs(20, 0).addBox(-1.0F, -2.0F, -0.5F, 1.0F, 4.0F, 1.0F), PartPose.offset(-4.0F, 0.0F, 0.0F));
        root.addOrReplaceChild("crown", CubeListBuilder.create().texOffs(0, 0).addBox(1.0F, -2.0F, -2.0F, 2.0F, 4.0F, 4.0F).texOffs(42, 0).addBox(3.0F, -1.0F, -1.0F, 1.0F, 2.0F, 2.0F).texOffs(0, 8)
                .addBox(4.0F, -0.5F, -0.5F, 1.0F, 1.0F, 1.0F), PartPose.ZERO);
        for (int i = 0; i < TINES; i++) {
            String name = "tine" + i;
            PartDefinition tine = root.addOrReplaceChild(name, CubeListBuilder.create(), PartPose.offsetAndRotation(CROWN_FRONT, 0.0F, 0.0F, i * Mth.HALF_PI, 0.0F, 0.0F));
            PartDefinition tineRoot = tine.addOrReplaceChild(name + "_root", CubeListBuilder.create().texOffs(24, 0).addBox(-0.5F, -0.5F, -0.5F, 1.0F, 3.5F, 1.0F),
                    PartPose.offsetAndRotation(0.0F, 2.0F, 0.0F, 0.0F, 0.0F, ROOT_BEND));
            PartDefinition tineMid = tineRoot.addOrReplaceChild(name + "_mid",
                    CubeListBuilder.create().texOffs(4, 8).addBox(-0.5F, -0.5F, -0.5F, 1.0F, 1.0F, 1.0F).texOffs(48, 0).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 3.0F, 1.0F),
                    PartPose.offsetAndRotation(0.0F, 3.0F, 0.0F, 0.0F, 0.0F, MID_BEND));
            tineMid.addOrReplaceChild(name + "_tip", CubeListBuilder.create().texOffs(4, 8).addBox(-0.5F, -0.5F, -0.5F, 1.0F, 1.0F, 1.0F).texOffs(52, 0).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 2.0F, 1.0F),
                    PartPose.offsetAndRotation(0.0F, 3.0F, 0.0F, 0.0F, 0.0F, TIP_BEND));
        }
        return LayerDefinition.create(mesh, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
