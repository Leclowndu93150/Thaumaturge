package com.leclowndu93150.thaumaturge.client.model.entity;

import com.leclowndu93150.thaumaturge.content.entity.EntityTaintSporeSwarmer;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;

/** Outer shell of TC4's Taint Spore Swarmer; its glowing inner core is rendered as a separate layer. */
public final class TaintSporeSwarmerModel extends HierarchicalModel<EntityTaintSporeSwarmer> {
    private static final int TEXTURE_SIZE = 64;
    private final ModelPart root;

    public TaintSporeSwarmerModel(ModelPart root) {
        super(RenderType::entityTranslucent);
        this.root = root;
    }

    public static LayerDefinition createShellLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild(
                "shell",
                CubeListBuilder.create().texOffs(0, 32).addBox(-8.0F, -8.0F, -8.0F, 16.0F, 16.0F, 16.0F),
                PartPose.offset(0.0F, 16.0F, 0.0F));
        return LayerDefinition.create(mesh, TEXTURE_SIZE, TEXTURE_SIZE);
    }

    public static LayerDefinition createCoreLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild(
                "core",
                CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, 0.0F, -8.0F, 16.0F, 16.0F, 16.0F),
                PartPose.ZERO);
        return LayerDefinition.create(mesh, TEXTURE_SIZE, TEXTURE_SIZE);
    }

    @Override
    public ModelPart root() {
        return root;
    }

    @Override
    public void setupAnim(
            EntityTaintSporeSwarmer entity,
            float limbSwing,
            float limbSwingAmount,
            float ageInTicks,
            float netHeadYaw,
            float headPitch) {
        root.getAllParts().forEach(ModelPart::resetPose);
    }
}
