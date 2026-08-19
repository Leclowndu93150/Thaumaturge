package com.leclowndu93150.thaumaturge.client.model.entity;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public final class ResearchTableModel {
    private static final int TEXTURE_WIDTH = 128;
    private static final int TEXTURE_HEIGHT = 64;

    public final ModelPart root;
    public final ModelPart table;
    public final ModelPart inkwell;
    public final ModelPart scrollTube;
    public final ModelPart scrollRibbon;

    public ResearchTableModel(ModelPart root) {
        this.root = root;
        this.table = root.getChild("table");
        this.inkwell = root.getChild("inkwell");
        this.scrollTube = root.getChild("scroll_tube");
        this.scrollRibbon = root.getChild("scroll_ribbon");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition table = root.addOrReplaceChild("table", CubeListBuilder.create(), PartPose.ZERO);
        table.addOrReplaceChild("top", CubeListBuilder.create().texOffs(0, 0).mirror().addBox(0.0F, 0.0F, 0.0F, 32.0F, 4.0F, 16.0F), PartPose.offset(-8.0F, 0.0F, -8.0F));
        table.addOrReplaceChild("leg1", CubeListBuilder.create().texOffs(0, 24).mirror().addBox(0.0F, 0.0F, 0.0F, 4.0F, 12.0F, 4.0F), PartPose.offset(-6.0F, 4.0F, -6.0F));
        table.addOrReplaceChild("leg2", CubeListBuilder.create().texOffs(0, 24).mirror().addBox(0.0F, 0.0F, 0.0F, 4.0F, 12.0F, 4.0F), PartPose.offset(-6.0F, 4.0F, 2.0F));
        table.addOrReplaceChild("leg3", CubeListBuilder.create().texOffs(0, 24).mirror().addBox(0.0F, 0.0F, 0.0F, 4.0F, 12.0F, 4.0F), PartPose.offset(18.0F, 4.0F, -6.0F));
        table.addOrReplaceChild("leg4", CubeListBuilder.create().texOffs(0, 24).mirror().addBox(0.0F, 0.0F, 0.0F, 4.0F, 12.0F, 4.0F), PartPose.offset(18.0F, 4.0F, 2.0F));
        table.addOrReplaceChild("crossbar", CubeListBuilder.create().texOffs(24, 24).mirror().addBox(0.0F, 0.0F, 0.0F, 24.0F, 4.0F, 4.0F), PartPose.offset(-4.0F, 10.0F, -2.0F));
        root.addOrReplaceChild("inkwell", CubeListBuilder.create().texOffs(0, 44).mirror().addBox(0.0F, 0.0F, 0.0F, 3.0F, 2.0F, 3.0F), PartPose.offset(-6.0F, -2.0F, 3.0F));
        root.addOrReplaceChild("scroll_tube", CubeListBuilder.create().texOffs(0, 0).mirror().addBox(-21.0F, -0.5F, -8.0F, 8.0F, 2.0F, 2.0F),
                PartPose.offsetAndRotation(-2.0F, -2.0F, 2.0F, 0.0F, 10.0F, 0.0F));
        root.addOrReplaceChild("scroll_ribbon", CubeListBuilder.create().texOffs(0, 4).mirror().addBox(-15.1F, -0.275F, -6.75F, 1.0F, 2.0F, 2.0F),
                PartPose.offsetAndRotation(-2.0F, -2.0F, 2.0F, 0.0F, 10.0F, 0.0F));
        return LayerDefinition.create(mesh, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
