package com.leclowndu93150.thaumaturge.client.model.entity;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public final class TCBannerModel {
    private static final int TEXTURE_WIDTH = 128;
    private static final int TEXTURE_HEIGHT = 64;

    public final ModelPart root;
    public final ModelPart pole;
    public final ModelPart beam;
    public final ModelPart tabLeft;
    public final ModelPart tabRight;
    public final ModelPart cloth;

    public TCBannerModel(ModelPart root) {
        this.root = root;
        this.pole = root.getChild("pole");
        this.beam = root.getChild("beam");
        this.tabLeft = root.getChild("tab_left");
        this.tabRight = root.getChild("tab_right");
        this.cloth = root.getChild("cloth");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("tab_left", CubeListBuilder.create().texOffs(0, 29).mirror().addBox(-5.0F, -7.5F, -1.5F, 2.0F, 3.0F, 3.0F), PartPose.ZERO);
        root.addOrReplaceChild("tab_right", CubeListBuilder.create().texOffs(0, 29).mirror().addBox(3.0F, -7.5F, -1.5F, 2.0F, 3.0F, 3.0F), PartPose.ZERO);
        root.addOrReplaceChild("beam", CubeListBuilder.create().texOffs(30, 0).mirror().addBox(-7.0F, -7.0F, -1.0F, 14.0F, 2.0F, 2.0F), PartPose.ZERO);
        root.addOrReplaceChild("cloth", CubeListBuilder.create().texOffs(0, 0).mirror().addBox(-7.0F, 0.0F, -0.5F, 14.0F, 28.0F, 1.0F), PartPose.offset(0.0F, -5.0F, 0.0F));
        root.addOrReplaceChild("pole", CubeListBuilder.create().texOffs(62, 0).mirror().addBox(0.0F, 0.0F, -1.0F, 2.0F, 31.0F, 2.0F), PartPose.offset(-1.0F, -7.0F, -2.0F));
        return LayerDefinition.create(mesh, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
