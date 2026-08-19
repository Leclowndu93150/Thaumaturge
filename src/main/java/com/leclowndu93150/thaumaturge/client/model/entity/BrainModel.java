package com.leclowndu93150.thaumaturge.client.model.entity;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public final class BrainModel {
    private static final int TEXTURE_WIDTH = 128;
    private static final int TEXTURE_HEIGHT = 64;
    private static final float STEM_PITCH = 0.4089647F;

    public final ModelPart root;

    public BrainModel(ModelPart root) {
        this.root = root;
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("lobe", CubeListBuilder.create().texOffs(0, 0).mirror().addBox(0.0F, 0.0F, 0.0F, 12.0F, 10.0F, 16.0F), PartPose.offset(-6.0F, 8.0F, -8.0F));
        root.addOrReplaceChild("cerebellum", CubeListBuilder.create().texOffs(64, 0).mirror().addBox(0.0F, 0.0F, 0.0F, 8.0F, 3.0F, 7.0F), PartPose.offset(-4.0F, 18.0F, 0.0F));
        root.addOrReplaceChild("stem", CubeListBuilder.create().texOffs(0, 32).mirror().addBox(0.0F, 0.0F, 0.0F, 2.0F, 6.0F, 2.0F),
                PartPose.offsetAndRotation(-1.0F, 18.0F, -2.0F, STEM_PITCH, 0.0F, 0.0F));
        return LayerDefinition.create(mesh, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
