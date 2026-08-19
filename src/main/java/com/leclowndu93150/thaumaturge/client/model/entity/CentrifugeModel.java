package com.leclowndu93150.thaumaturge.client.model.entity;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public final class CentrifugeModel {
    private static final int TEXTURE_WIDTH = 64;
    private static final int TEXTURE_HEIGHT = 32;

    public final ModelPart root;
    public final ModelPart boxes;
    public final ModelPart spinner;

    public CentrifugeModel(ModelPart root) {
        this.root = root;
        this.boxes = root.getChild("boxes");
        this.spinner = root.getChild("spinner");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition boxes = root.addOrReplaceChild("boxes", CubeListBuilder.create(), PartPose.ZERO);
        boxes.addOrReplaceChild("top", CubeListBuilder.create().texOffs(20, 16).mirror().addBox(-4.0F, -8.0F, -4.0F, 8.0F, 4.0F, 8.0F), PartPose.ZERO);
        boxes.addOrReplaceChild("bottom", CubeListBuilder.create().texOffs(20, 16).mirror().addBox(-4.0F, 4.0F, -4.0F, 8.0F, 4.0F, 8.0F), PartPose.ZERO);
        PartDefinition spinner = root.addOrReplaceChild("spinner", CubeListBuilder.create(), PartPose.ZERO);
        spinner.addOrReplaceChild("crossbar", CubeListBuilder.create().texOffs(16, 0).mirror().addBox(-4.0F, -1.0F, -1.0F, 8.0F, 2.0F, 2.0F), PartPose.ZERO);
        spinner.addOrReplaceChild("dingus1", CubeListBuilder.create().texOffs(0, 16).mirror().addBox(4.0F, -3.0F, -2.0F, 4.0F, 6.0F, 4.0F), PartPose.ZERO);
        spinner.addOrReplaceChild("dingus2", CubeListBuilder.create().texOffs(0, 16).mirror().addBox(-8.0F, -3.0F, -2.0F, 4.0F, 6.0F, 4.0F), PartPose.ZERO);
        spinner.addOrReplaceChild("core", CubeListBuilder.create().texOffs(0, 0).mirror().addBox(-1.5F, -4.0F, -1.5F, 3.0F, 8.0F, 3.0F), PartPose.ZERO);
        return LayerDefinition.create(mesh, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
