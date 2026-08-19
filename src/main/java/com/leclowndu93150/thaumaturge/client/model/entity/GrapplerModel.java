package com.leclowndu93150.thaumaturge.client.model.entity;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public final class GrapplerModel {
    public final ModelPart root;

    public GrapplerModel(ModelPart root) {
        this.root = root;
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("core", CubeListBuilder.create().texOffs(0, 0).addBox(-1.5F, -1.5F, -1.5F, 3, 3, 3), PartPose.ZERO);
        root.addOrReplaceChild("prong1", CubeListBuilder.create().texOffs(0, 10).addBox(-0.5F, -0.5F, -2.5F, 1, 1, 5), PartPose.ZERO);
        root.addOrReplaceChild("prong2", CubeListBuilder.create().texOffs(0, 10).addBox(-0.5F, -0.5F, -2.5F, 1, 1, 5), PartPose.rotation(0.0F, (float) (Math.PI / 2), 0.0F));
        root.addOrReplaceChild("prong3", CubeListBuilder.create().texOffs(0, 10).addBox(-0.5F, -0.5F, -2.5F, 1, 1, 5), PartPose.rotation((float) (Math.PI / 2), (float) (Math.PI / 2), 0.0F));
        return LayerDefinition.create(mesh, 64, 32);
    }
}
