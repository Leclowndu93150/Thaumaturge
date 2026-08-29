package com.leclowndu93150.thaumaturge.client.model.entity;

import com.leclowndu93150.thaumaturge.content.entity.EntityTaintSpore;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;

/** Faithful modern reconstruction of TC4's two-cube translucent Taint Spore model. */
public final class TaintSporeModel<T extends EntityTaintSpore> extends HierarchicalModel<T> {
    private static final int TEXTURE_SIZE = 64;
    private final ModelPart root;
    private final ModelPart cube;

    public TaintSporeModel(ModelPart root) {
        super(RenderType::entityTranslucent);
        this.root = root;
        this.cube = root.getChild("cube");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild(
                "cube",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-6.0F, 2.0F, -6.0F, 12.0F, 12.0F, 12.0F)
                        .texOffs(0, 0)
                        .addBox(-8.0F, 0.0F, -8.0F, 16.0F, 16.0F, 16.0F),
                PartPose.offset(0.0F, 24.0F, 0.0F));
        return LayerDefinition.create(mesh, TEXTURE_SIZE, TEXTURE_SIZE);
    }

    @Override
    public ModelPart root() {
        return root;
    }

    @Override
    public void setupAnim(
            T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        root.getAllParts().forEach(ModelPart::resetPose);
        float intensity = entity.hurtTime > 0 ? 0.04F : 0.02F;
        cube.xRot = intensity * Mth.sin(ageInTicks * 0.05F);
        cube.zRot = intensity * Mth.sin(ageInTicks * 0.10F);
    }
}
