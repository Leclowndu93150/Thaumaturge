package com.leclowndu93150.thaumaturge.client.model.entity;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public final class ManaPodModel {
    private static final int TEXTURE_WIDTH = 32;
    private static final int TEXTURE_HEIGHT = 32;

    public final ModelPart core;
    public final ModelPart shell;

    public ManaPodModel(ModelPart root) {
        this.core = root.getChild("core");
        this.shell = root.getChild("shell");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("core", CubeListBuilder.create().texOffs(0, 0).mirror().addBox(-2.0F, 0.0F, -2.0F, 4.0F, 5.0F, 4.0F), PartPose.ZERO);
        root.addOrReplaceChild("shell", CubeListBuilder.create().texOffs(0, 0).mirror().addBox(-3.5F, 0.0F, -3.5F, 7.0F, 9.0F, 7.0F), PartPose.ZERO);
        return LayerDefinition.create(mesh, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
