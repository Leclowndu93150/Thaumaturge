package com.leclowndu93150.thaumaturge.client.model.entity;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public final class DeconTableModel {
    private static final int TEXTURE_WIDTH = 128;
    private static final int TEXTURE_HEIGHT = 64;

    public final ModelPart root;

    public DeconTableModel(ModelPart root) {
        this.root = root;
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("top", CubeListBuilder.create().texOffs(0, 0).mirror().addBox(0.0F, 0.0F, 0.0F, 16.0F, 8.0F, 16.0F), PartPose.offset(-8.0F, 0.0F, -8.0F));
        root.addOrReplaceChild("base", CubeListBuilder.create().texOffs(0, 32).mirror().addBox(0.0F, 0.0F, 0.0F, 16.0F, 4.0F, 16.0F), PartPose.offset(-8.0F, 12.0F, -8.0F));
        root.addOrReplaceChild("leg1", CubeListBuilder.create().texOffs(72, 0).mirror().addBox(0.0F, 0.0F, 0.0F, 4.0F, 4.0F, 4.0F), PartPose.offset(3.0F, 8.0F, -7.0F));
        root.addOrReplaceChild("leg2", CubeListBuilder.create().texOffs(72, 0).mirror().addBox(0.0F, 0.0F, 0.0F, 4.0F, 4.0F, 4.0F), PartPose.offset(-7.0F, 8.0F, 3.0F));
        root.addOrReplaceChild("leg3", CubeListBuilder.create().texOffs(72, 0).mirror().addBox(0.0F, 0.0F, 0.0F, 4.0F, 4.0F, 4.0F), PartPose.offset(3.0F, 8.0F, 3.0F));
        root.addOrReplaceChild("leg4", CubeListBuilder.create().texOffs(72, 0).mirror().addBox(0.0F, 0.0F, 0.0F, 4.0F, 4.0F, 4.0F), PartPose.offset(-7.0F, 8.0F, -7.0F));
        return LayerDefinition.create(mesh, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
